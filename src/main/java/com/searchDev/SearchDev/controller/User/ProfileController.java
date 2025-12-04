package com.searchDev.SearchDev.controller.User;

import com.searchDev.SearchDev.DTO.UpdateProfileReqDTO;
import com.searchDev.SearchDev.DTO.UserDetailsDTO;
import com.searchDev.SearchDev.Model.UserPrincipal;
import com.searchDev.SearchDev.Service.RedisService.RedisService;
import com.searchDev.SearchDev.Service.UserService.DeveloperService;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/profile")
public class ProfileController {

    @Autowired
    private DeveloperService developerService;

    private RedisService redisService;

    @Autowired
    public ProfileController(RedisService redisService){
        this.redisService = redisService;
    }

    //get profile
    @GetMapping()
    public ResponseEntity<UserDetailsDTO> getProfile(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ){
        String key = "profile:" + userPrincipal.getUsername();
        UserDetailsDTO cachedProfile = redisService.get(key, UserDetailsDTO.class);
        if(cachedProfile != null){
            return ResponseEntity.ok(cachedProfile);
        }
        System.out.println("DB hit → getProfile");
        UserDetailsDTO profile = developerService.getProfile(userPrincipal.getUsername());
        redisService.save(key, profile, Duration.ofHours(2));
        return ResponseEntity.ok(profile);
    }


    //update profile
    @PutMapping()
    public ResponseEntity<UserDetailsDTO> updateProfile(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody UpdateProfileReqDTO request
    ){

        String key ="profile:" + userPrincipal.getUsername();
        redisService.delete(key);
        System.out.println("DB hit → updateProfile");
        UserDetailsDTO updatedProfile = developerService.updateProfile(userPrincipal.getUsername(),request);
        redisService.save(key, updatedProfile, Duration.ofHours(2));
        return ResponseEntity.ok(updatedProfile);
    }
}
