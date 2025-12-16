package com.searchDev.SearchDev.controller.User;


import com.searchDev.SearchDev.DTO.PageResponseDTO;
import com.searchDev.SearchDev.DTO.UserDetailsDTO;
import com.searchDev.SearchDev.ExceptionHandler.ResourceNotFoundException;
import com.searchDev.SearchDev.Service.UserService.DeveloperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/developers")
public class UserController {

    @Autowired
    private DeveloperService developerService;

    @GetMapping()
    public ResponseEntity<PageResponseDTO<UserDetailsDTO>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "2") int size
    ){
        if(page<0 || size<=0 ) {
            throw new IllegalArgumentException("Page index must be >= 0 and size must be > 0");
        }
        Page<UserDetailsDTO> users= developerService.getAllUsers(PageRequest.of(page,size));
        PageResponseDTO<UserDetailsDTO>  response= new PageResponseDTO<>(
                users.getContent(),
                users.getSize(),
                users.getNumber(),
                users.getTotalElements(),
                users.getTotalPages(),
                users.isLast()
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{userID}")
    public ResponseEntity<UserDetailsDTO> userById(@PathVariable UUID userID) throws ResourceNotFoundException {
        UserDetailsDTO user =developerService.getDeveloperById(userID);
        // System.out.println(user);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<PageResponseDTO<UserDetailsDTO>> userByUsername(
            @PathVariable String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "2") int size
    ){
        if(page<0 || size<=0 ) {
            throw new IllegalArgumentException("Page index must be >= 0 and size must be > 0");
        }

       Page<UserDetailsDTO> users = developerService.getUserByUsername(username,PageRequest.of(page,size));
       PageResponseDTO<UserDetailsDTO> response = new PageResponseDTO<>(
               users.getContent(),
               users.getSize(),
               users.getNumber(),
               users.getTotalElements(),
               users.getTotalPages(),
               users.isLast()
       );
       return ResponseEntity.ok(response);
    }
}
