package com.searchDev.SearchDev.controller.User;

import com.searchDev.SearchDev.DTO.LoginReqDTO;
import com.searchDev.SearchDev.Model.Users;
import com.searchDev.SearchDev.Service.AuthService.JWTservice;
import com.searchDev.SearchDev.Service.AuthService.UserAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class UserController {

    @Autowired
    private UserAuthService userAuthService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Users user){
        System.out.println("begin");
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
