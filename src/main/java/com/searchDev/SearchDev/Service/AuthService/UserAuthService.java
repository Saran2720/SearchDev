package com.searchDev.SearchDev.Service.AuthService;

import com.searchDev.SearchDev.ExceptionHandler.UserAlreadyExistException;
import com.searchDev.SearchDev.Model.PasswordResetToken;
import com.searchDev.SearchDev.Model.Users;
import com.searchDev.SearchDev.Repository.TokenRepo;
import com.searchDev.SearchDev.Repository.UserRepo;
import com.searchDev.SearchDev.Service.AuthService.JWTservice;
import com.searchDev.SearchDev.Service.MailService.EmailService;
import com.searchDev.SearchDev.Service.AuthService.MyUserDetailsService;
import com.searchDev.SearchDev.Service.UserService.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.searchDev.SearchDev.Service.AuthService.TokenBlacklistService;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Service;
import org.springframework.http.ResponseCookie;

import java.net.URI;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class UserAuthService {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private JWTservice jwTservice;

    @Autowired
    private MyUserDetailsService userDetailsService;

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
        // blacklist the tokens
        if (accessToken != null) {
            tokenBlacklistService.blackListToken(accessToken);
        }
        if (refreshToken != null) {
            tokenBlacklistService.blackListToken(refreshToken);
        }
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

    public void forgetPassword(String email) {
        Users isExistUser = userRepo.findByEmail(email);
        if (isExistUser == null) {
            throw new UserNotFoundException("User not found");
        }

        PasswordResetToken prt = PasswordResetToken.builder()
                .email(email)
                .time(LocalDateTime.now().plusMinutes(15))
                .build();
        tokenRepo.save(prt);
        UUID token = prt.getResetTokenId();

        String resetLink = "http://localhost:8080/reset-password?token=" + token;
        emailService.sendMail(email, resetLink);
        System.out.println("Reset link for " + email + ": " + resetLink);
    }

    // validating the reset token when the user click the link in the email
    public URI verifyResetLink(String token) {
        PasswordResetToken prt = tokenRepo.findByResetTokenId(UUID.fromString(token));
        if (prt == null || prt.getTime().isBefore(LocalDateTime.now())) {
            if (prt != null) {
                tokenRepo.delete(prt);
            }
            throw new DateTimeException("Invalid or reset link is expired");
        }

        // valid token -> redirect user to frontend page
        return URI.create("valid");
    }

    // reseting the user password
    @Autowired
    private PasswordEncoder passwordEncoder;

    public void resetPassword(String token, String newPassword) {
        PasswordResetToken prt = tokenRepo.findByResetTokenId(UUID.fromString(token));

        Users user = userRepo.findByEmail(prt.getEmail());
        if (user == null) {
            throw new UserNotFoundException("User not found");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepo.save(user);
        tokenRepo.delete(prt);
    }
}
