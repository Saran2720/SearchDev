package com.searchDev.SearchDev.Service.AuthService;

import com.searchDev.SearchDev.ExceptionHandler.UserAlreadyExistException;
import com.searchDev.SearchDev.Model.PasswordResetToken;
import com.searchDev.SearchDev.Model.Users;
import com.searchDev.SearchDev.Repository.TokenRepo;
import com.searchDev.SearchDev.Repository.UserRepo;
import com.searchDev.SearchDev.Service.MailService.EmailService;
import com.searchDev.SearchDev.Service.UserService.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class UserAuthService {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private JWTservice jwTservice;

    @Autowired
    private MyUserDetailsService userDetailsService;

    @Value("${app.auth.reset-link-base-url:http://localhost:8080/reset-password}")
    private String backendResetLinkBaseUrl;

    @Value("${app.frontend.reset-form-url:http://localhost:3000/reset-password}")
    private String frontendResetFormUrl;

    public Users register(Users user) {
        Users isExistUser = userRepo.findByEmail(user.getEmail());
        if (isExistUser != null) {
            throw new UserAlreadyExistException("Email already exist");
        }
        return userRepo.save(user);
    }

    // logout by blacklist and removing the tokens from cookie
    private final TokenBlacklistService tokenBlacklistService;

    @Autowired
    UserAuthService(TokenBlacklistService tokenBlacklistService) {
        this.tokenBlacklistService = tokenBlacklistService;
    }

    public HttpHeaders logout(String accessToken, String refreshToken) {
        try {
            // Normalize empty strings to null
            if (accessToken != null && accessToken.trim().isEmpty()) {
                accessToken = null;
            }
            if (refreshToken != null && refreshToken.trim().isEmpty()) {
                refreshToken = null;
            }

            // blacklist the tokens if they exist
            if (accessToken != null) {
                tokenBlacklistService.blackListToken(accessToken);
            }
            if (refreshToken != null) {
                tokenBlacklistService.blackListToken(refreshToken);
            }

            // Clear cookies regardless of token presence
            ResponseCookie clearAccess = ResponseCookie.from("access_Token", "")
                    .path("/")
                    .maxAge(0)
                    .httpOnly(true)
                    .sameSite("Strict")
                    .build();

            ResponseCookie clearRefresh = ResponseCookie.from("refresh_Token", "")
                    .path("/")
                    .maxAge(0)
                    .httpOnly(true)
                    .sameSite("Strict")
                    .build();

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.SET_COOKIE, clearAccess.toString());
            headers.add(HttpHeaders.SET_COOKIE, clearRefresh.toString());

            return headers;
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid token format: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to process logout: " + e.getMessage(), e);
        }
    }


    // refresh token
    public HttpHeaders refresh(String refreshToken) {
        if (refreshToken == null || refreshToken.isEmpty()) {
            throw new SecurityException("Refresh token is missing");
        }

        if (tokenBlacklistService.istokenBlackListed(refreshToken)) {
            throw new SecurityException("Refresh token is expired or revoked");
        }

        // Validate token type
        String type = jwTservice.extractTokenType(refreshToken);
        if (!type.equals("Refresh")) {
            throw new SecurityException("Invalid token type");
        }

        // Extract email and validate token
        String email = jwTservice.extractUserEmail(refreshToken);
        UserDetails user = userDetailsService.loadUserByUsername(email);

        if (!jwTservice.validateToken(refreshToken, user, false)) {
            throw new SecurityException("Refresh token invalid or expired");
        }

        // Generate new access token
        Authentication authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        String newAccessToken = jwTservice.generateAccessToken(authentication);

        if (newAccessToken == null || newAccessToken.isEmpty()) {
            throw new RuntimeException("Failed to generate access token");
        }

        ResponseCookie newAccessCookie = ResponseCookie.from("access_Token", newAccessToken)
                .httpOnly(true)
                .path("/")
                .secure(true)
                .sameSite("Strict")
                .maxAge(60 * 15)
                .build();
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, newAccessCookie.toString());
        return headers;
    }

    @Autowired
    private TokenRepo tokenRepo;

    @Autowired
    private EmailService emailService;

    @SuppressWarnings("null")
    public void forgetPassword(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }

        Users isExistUser = userRepo.findByEmail(email);
        if (isExistUser == null) {
            throw new UserNotFoundException("User not found");
        }

        List<PasswordResetToken> oldTokens = tokenRepo.findAllByEmail(email);
        if (!oldTokens.isEmpty()) {
            tokenRepo.deleteAll(oldTokens);
        }

        PasswordResetToken prt = PasswordResetToken.builder()
                .email(email)
                .time(LocalDateTime.now().plusMinutes(15))
                .build();
        tokenRepo.save(prt);

        String resetLink = buildBackendResetLink(prt.getResetTokenId());
        emailService.sendMail(email, resetLink);
    }

    // validating the reset token when the user click the link in the email
    public URI verifyResetLink(String token) {
        UUID tokenId = parseToken(token);
        PasswordResetToken prt = tokenRepo.findByResetTokenId(tokenId)
                .orElseThrow(() -> new DateTimeException("Invalid reset token"));

        if (prt.getTime().isBefore(LocalDateTime.now())) {
            tokenRepo.delete(prt);
            throw new DateTimeException("Invalid or reset link is expired");
        }

        return URI.create(buildFrontendResetUrl(token));
    }

    // reseting the user password
    @Autowired
    private PasswordEncoder passwordEncoder;

    public void resetPassword(String token, String newPassword) {
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("New password is required");
        }

        UUID tokenId = parseToken(token);
        PasswordResetToken prt = tokenRepo.findByResetTokenId(tokenId)
                .orElseThrow(() -> new DateTimeException("Invalid or expired reset token"));

        if (prt.getTime().isBefore(LocalDateTime.now())) {
            tokenRepo.delete(prt);
            throw new DateTimeException("Invalid or expired reset token");
        }

        Users user = userRepo.findByEmail(prt.getEmail());
        if (user == null) {
            tokenRepo.delete(prt);
            throw new UserNotFoundException("User not found");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepo.save(user);
        tokenRepo.delete(prt);
    }

    private UUID parseToken(String token) {
        try {
            return UUID.fromString(token);
        } catch (IllegalArgumentException e) {
            throw new DateTimeException("Invalid reset token");
        }
    }

    private String buildBackendResetLink(UUID tokenId) {
        return backendResetLinkBaseUrl + "?token=" + tokenId;
    }

    private String buildFrontendResetUrl(String token) {
        return frontendResetFormUrl + "?token=" + token;
    }
}
