package com.searchDev.SearchDev.controller.User;

import com.searchDev.SearchDev.DTO.ApiResDTO;
import com.searchDev.SearchDev.DTO.AuthResDTO;
import com.searchDev.SearchDev.DTO.LoginReqDTO;
import com.searchDev.SearchDev.DTO.RegisterReqDTO;
import com.searchDev.SearchDev.Model.Users;
import com.searchDev.SearchDev.Service.AuthService.JWTservice;
import com.searchDev.SearchDev.Service.AuthService.MyUserDetailsService;
import com.searchDev.SearchDev.Service.AuthService.TokenBlacklistService;
import com.searchDev.SearchDev.Service.AuthService.UserAuthService;
import io.jsonwebtoken.io.IOException;
import jakarta.servlet.http.HttpServletRequest;
import org.antlr.v4.runtime.Token;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
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
            @RequestBody RegisterReqDTO registerReqDTO
            ) throws IOException {

            Users user = new Users();
            user.setEmail(registerReqDTO.getEmail());
            user.setUsername(registerReqDTO.getUsername());
            user.setPassword(encoder.encode(registerReqDTO.getPassword()));
            return ResponseEntity.ok(userAuthService.register(user));
    }

    @Autowired
    JWTservice jwTservice;
    @PostMapping("/login")
    public ResponseEntity<ApiResDTO<AuthResDTO>> login(@RequestBody LoginReqDTO request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            String accessToken = jwTservice.generateAccessToken(authentication);
            String refreshToken = jwTservice.generateRefreshToken(authentication);

            AuthResDTO tokenResponse = AuthResDTO.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .build();

            ApiResDTO<AuthResDTO> response = ApiResDTO.<AuthResDTO>builder()
                    .success(true)
                    .status(HttpStatus.OK.value())
                    .message("Login successful")
                    .data(tokenResponse)
                    .timestamp(LocalDateTime.now())
                    .build();

            return ResponseEntity.ok(response);

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


    private final TokenBlacklistService tokenBlacklistService;
    @Autowired
    UserAuthController(TokenBlacklistService tokenBlacklistService){
        this.tokenBlacklistService=tokenBlacklistService;
    }
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody Map<String, String> request){
        String refreshToken = request.get("refreshToken");

        if(refreshToken==null || refreshToken.isEmpty()){
            return ResponseEntity.badRequest().body("Refresh token is missing");
        }

        if(tokenBlacklistService.istokenBlackListed(refreshToken)){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Refresh token validity is over");
        }
        try{
            String type = jwTservice.extractTokenType(refreshToken);
            if(!type.equals("Refresh")){
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid token type"));
            }
            //check validity of the token
            String email= jwTservice.extractUserEmail(refreshToken);
            UserDetails userDetails = myUserDetailsService.loadUserByUsername(email);

            if(!jwTservice.validateToken(refreshToken,userDetails,false)){ //sending false because of we use refresh token
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid or expired refresh token"));
            }
//             Generate new access token
            Authentication authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            String newAccessToken = jwTservice.generateAccessToken(authentication);
            return ResponseEntity.ok(Map.of(
                    "accessToken", newAccessToken,
                    "message", "Access token refreshed successfully"
            ));
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody AuthResDTO tokens) {
        String accessToken = tokens.getAccessToken();
        String refreshToken = tokens.getRefreshToken();

        if (accessToken != null) {
            tokenBlacklistService.blackListToken(accessToken);
        }
        if (refreshToken != null) {
            tokenBlacklistService.blackListToken(refreshToken);
        }
        return ResponseEntity.ok(Map.of("message", "Logout successful"));
    }
}
