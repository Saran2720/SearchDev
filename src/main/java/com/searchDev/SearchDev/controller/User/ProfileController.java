package com.searchDev.SearchDev.controller.User;

import com.searchDev.SearchDev.DTO.UpdateProfileReqDTO;
import com.searchDev.SearchDev.DTO.UserDetailsDTO;
import com.searchDev.SearchDev.Model.UserPrincipal;
import com.searchDev.SearchDev.Service.RedisService.RedisService;
import com.searchDev.SearchDev.Service.UserService.DeveloperService;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/profile")
public class ProfileController {

    @Autowired
    private DeveloperService developerService;


    //get profile
    @GetMapping()
    public ResponseEntity<UserDetailsDTO> getProfile(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ){

        UserDetailsDTO profile = developerService.getProfile(userPrincipal.getUsername());
        return ResponseEntity.ok(profile);
    }


    //update profile
    @PutMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<UserDetailsDTO> updateProfile(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestPart("data") UpdateProfileReqDTO request,
            @RequestPart(value = "profileImg", required = false) MultipartFile profileImg
    ){
        UserDetailsDTO updatedProfile = developerService.updateProfile(userPrincipal.getUsername(),request,profileImg);
        return ResponseEntity.ok(updatedProfile);
    }
}
