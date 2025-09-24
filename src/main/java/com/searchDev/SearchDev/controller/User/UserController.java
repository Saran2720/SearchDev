package com.searchDev.SearchDev.controller.User;


import com.searchDev.SearchDev.DTO.PageResponseDTO;
import com.searchDev.SearchDev.DTO.UpdateProfileReqDTO;
import com.searchDev.SearchDev.DTO.UserDetailsDTO;
import com.searchDev.SearchDev.Model.UserPrincipal;
import com.searchDev.SearchDev.Service.DeveloperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
public class UserController {

    @Autowired
    private DeveloperService developerService;

    @RequestMapping("/developers")
    public ResponseEntity<PageResponseDTO<UserDetailsDTO>> developers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "2") int size
    ){
        if(page<0 || size<=0 ) {
            throw new IllegalArgumentException("Page index must be >= 0 and size must be > 0");
        }
        Page<UserDetailsDTO> developers= developerService.getAllDevelopers(PageRequest.of(page,size));
        PageResponseDTO<UserDetailsDTO>  response= new PageResponseDTO<>(
                developers.getContent(),
                developers.getSize(),
                developers.getNumber(),
                developers.getTotalElements(),
                developers.getTotalPages(),
                developers.isLast()
        );
        return ResponseEntity.ok(response);
    }

    @RequestMapping("/developers/{userID}")
    public ResponseEntity<UserDetailsDTO> developersById(@PathVariable UUID userID){
        UserDetailsDTO developer =developerService.getDeveloperById(userID);
        System.out.println(developer);
        return ResponseEntity.ok(developer);
    }

    @RequestMapping("/developers/username/{username}")
    public ResponseEntity<PageResponseDTO<UserDetailsDTO>> developersByUsername(
            @PathVariable String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "2") int size
    ){
        if(page<0 || size<=0 ) {
            throw new IllegalArgumentException("Page index must be >= 0 and size must be > 0");
        }

       Page<UserDetailsDTO> developers = developerService.getDevelopersByUsername(username,PageRequest.of(page,size));
       PageResponseDTO<UserDetailsDTO> response = new PageResponseDTO<>(
               developers.getContent(),
               developers.getSize(),
               developers.getNumber(),
               developers.getTotalElements(),
               developers.getTotalPages(),
               developers.isLast()
       );
       return ResponseEntity.ok(response);
    }


    //update profile
    @PutMapping("/profile")
    public ResponseEntity<UserDetailsDTO> updateProfile(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody UpdateProfileReqDTO request
            ){

        //userPrincipal.getUsername() will get the email
        UserDetailsDTO updatedProfile = developerService.updateProfile(userPrincipal.getUsername(),request);
        return ResponseEntity.ok(updatedProfile);
    }
}
