package com.searchDev.SearchDev.controller.User;

import com.searchDev.SearchDev.DTO.UpdateProfileReqDTO;
import com.searchDev.SearchDev.DTO.UserDetailsDTO;
import com.searchDev.SearchDev.Model.UserPrincipal;
import com.searchDev.SearchDev.Service.UserService.DeveloperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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
    @PutMapping()
    public ResponseEntity<UserDetailsDTO> updateProfile(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody UpdateProfileReqDTO request
    ){

        //userPrincipal.getUsername() will get the email
        UserDetailsDTO updatedProfile = developerService.updateProfile(userPrincipal.getUsername(),request);
        return ResponseEntity.ok(updatedProfile);
    }
}
