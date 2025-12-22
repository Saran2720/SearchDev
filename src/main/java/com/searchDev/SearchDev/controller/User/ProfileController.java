package com.searchDev.SearchDev.controller.User;

import com.searchDev.SearchDev.DTO.ConfirmImageReqDTO;
import com.searchDev.SearchDev.DTO.UpdateProfileReqDTO;
import com.searchDev.SearchDev.DTO.UserDetailsDTO;
import com.searchDev.SearchDev.Model.UserPrincipal;
import com.searchDev.SearchDev.Security.RateLimiter;
import com.searchDev.SearchDev.Service.FileUploadService.FileUploadService;
import com.searchDev.SearchDev.Service.UserService.DeveloperService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/profile")
public class ProfileController {

    private final DeveloperService developerService;

    private final FileUploadService fileUploadService;

    @Autowired
    public ProfileController(DeveloperService developerService, FileUploadService fileUploadService) {
        this.developerService = developerService;
        this.fileUploadService = fileUploadService;
    }


    // get profile
    @RateLimiter(request = 5,durationSeconds = 60)
    @GetMapping()
    public ResponseEntity<UserDetailsDTO> getProfile(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        UserDetailsDTO profile = developerService.getProfile(userPrincipal.getUsername());
        return ResponseEntity.ok(profile);
    }

    // update profile
    @RateLimiter(request = 10,durationSeconds = 60)
    @PutMapping("/update")
    public ResponseEntity<UserDetailsDTO> updateProfile(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody UpdateProfileReqDTO request) {
        UserDetailsDTO updatedProfile = developerService.updateProfile(userPrincipal.getUsername(), request);
        return ResponseEntity.ok(updatedProfile);
    }

    //get the presignedUrl and upload thourgh it via frontend
    // for profile image
    @RateLimiter(request = 3, durationSeconds = 60)
    @PutMapping("/image/presign")
    public ResponseEntity<Map<String, String>> getPresignedUrl(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam String extension
        ) {

        Map<String, String> urlFileKeyMap = fileUploadService.generateProfilePresignedUrl(userPrincipal.getUsername(),
                extension);
        return ResponseEntity.ok(urlFileKeyMap);
    }

    @PutMapping("/image/confirm")
    public ResponseEntity<Void> confirmUpdate(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody @Valid ConfirmImageReqDTO request
    ) {
        developerService.confirmUpdate(
            userPrincipal.getUsername(),
            request.getFileKey()
        );
        return ResponseEntity.noContent().build(); // 204
    }
}
