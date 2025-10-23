package com.searchDev.SearchDev.controller.User;

import com.searchDev.SearchDev.DTO.LoginReqDTO;
import com.searchDev.SearchDev.DTO.RegisterReqDTO;
import com.searchDev.SearchDev.Model.Users;
import com.searchDev.SearchDev.Service.AuthService.JWTservice;
import com.searchDev.SearchDev.Service.AuthService.UserAuthService;
import io.jsonwebtoken.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;
import java.util.Map;

@RestController
public class UserAuthController {

    @Autowired
    private UserAuthService userAuthService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private PasswordEncoder encoder;

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestBody RegisterReqDTO registerReqDTO
            ) throws IOException, java.io.IOException {
        Users user = new Users();
        user.setEmail(registerReqDTO.getEmail());
        user.setUsername(registerReqDTO.getUsername());
        user.setPassword(encoder.encode(registerReqDTO.getPassword()));
        return ResponseEntity.ok(userAuthService.register(user));
    }

    @Autowired
    JWTservice jwTservice;
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginReqDTO request){
        try{
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            String token = jwTservice.generateToken(authentication);
            return ResponseEntity.ok(Map.of("token",token));
        } catch (Exception e) {
            return ResponseEntity.status(401).body("invalid email or password");
        }

//        return ResponseEntity.ok(jwTservice.generateToken(user));
    }


}
