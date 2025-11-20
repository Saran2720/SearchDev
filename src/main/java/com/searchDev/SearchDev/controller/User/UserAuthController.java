package com.searchDev.SearchDev.controller.User;

import com.searchDev.SearchDev.DTO.*;
import com.searchDev.SearchDev.Model.Users;
import com.searchDev.SearchDev.Service.AuthService.JWTservice;
import com.searchDev.SearchDev.Service.AuthService.MyUserDetailsService;
import com.searchDev.SearchDev.Service.AuthService.UserAuthService;
import com.searchDev.SearchDev.Service.UserService.UserNotFoundException;
import io.jsonwebtoken.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import org.springframework.http.HttpHeaders;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
public class UserAuthController {

    @Autowired
    private UserAuthService userAuthService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private MyUserDetailsService myUserDetailsService;

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestBody RegisterReqDTO registerReqDTO) throws IOException {

        Users user = new Users();
        user.setEmail(registerReqDTO.getEmail());
        user.setUsername(registerReqDTO.getUsername());
        user.setPassword(encoder.encode(registerReqDTO.getPassword()));
        return ResponseEntity.ok(userAuthService.register(user));
    }

    // login and generate the tokens
    @Autowired
    JWTservice jwTservice;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginReqDTO request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()));

            String accessToken = jwTservice.generateAccessToken(authentication);
            String refreshToken = jwTservice.generateRefreshToken(authentication);

            ResponseCookie accessCookie = ResponseCookie.from("access_Token", accessToken)
                    .httpOnly(true)
                    .secure(true)
                    .path("/")
                    .sameSite("Strict")
                    .maxAge(60 * 15) // valid for 15 mins
                    .build();
            ResponseCookie refreshCookie = ResponseCookie.from("refresh_Token", refreshToken)
                    .httpOnly(true)
                    .secure(true)
                    .path("/")
                    .sameSite("Strict")
                    .maxAge(60 * 60 * 24 * 7) // valid for 7 days
                    .build();

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.SET_COOKIE, accessCookie.toString());
            headers.add(HttpHeaders.SET_COOKIE, refreshCookie.toString());

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(Map.of("message", "Login successful"));

        } catch (Exception e) {
            ApiResDTO<AuthResDTO> errorResponse = ApiResDTO.<AuthResDTO>builder()
                    .success(false)
                    .status(HttpStatus.UNAUTHORIZED.value())
                    .message("Invalid email or password")
                    .data(null)
                    .timestamp(LocalDateTime.now())
                    .build();

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }

    // refresh token
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@CookieValue(value = "refresh_Token", required = false) String refreshToken) {
        try {
            HttpHeaders headers = userAuthService.refresh(refreshToken);
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(Map.of("message", "access token generated"));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        }
    }

    // logout the user by using tokens from http cookie
    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            @CookieValue(value = "refresh_Token", required = false) String refreshToken,
            @CookieValue(value = "access_Token", required = false) String accessToken) {

        HttpHeaders headers = userAuthService.logout(accessToken, refreshToken);
        return ResponseEntity.ok()
                .headers(headers)
                .body(Map.of("message", "Logout successful"));
    }

    @PostMapping("/forget-password")
    public ResponseEntity<?> forgetPassword(@RequestBody ForgetPasswordReqDTO req) {
        String genericMsg = "If an account with that email exists, a password reset link has been sent.";
        System.out.println(genericMsg);
        try {
            userAuthService.forgetPassword(req.getEmail());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
        return ResponseEntity.ok(Map.of("message", genericMsg));
    }

    // validating the reset token when the user click the link in the email
    @GetMapping("/reset-password")
    public ResponseEntity<?> verifyResetLink(@RequestParam("token") String token) {
        try {
            URI frontendURI = userAuthService.verifyResetLink(token);
            return ResponseEntity.status(HttpStatus.FOUND).location(frontendURI).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    // after validating the reset token , the user can change the password
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestParam("token") String token,
            @RequestBody Map<String, String> request) {
        String newPassword = request.get("newPassword");

        try {
            userAuthService.resetPassword(token, newPassword);
            return ResponseEntity.ok(Map.of("message", "Password reset successful"));
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Something went wrong"));
        }
    }

}
