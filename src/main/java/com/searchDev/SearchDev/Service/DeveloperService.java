package com.searchDev.SearchDev.Service;

import com.searchDev.SearchDev.DTO.UpdateProfileReqDTO;
import com.searchDev.SearchDev.DTO.UserDetailsDTO;
import com.searchDev.SearchDev.Model.Users;
import com.searchDev.SearchDev.Repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;


@Service
public class DeveloperService {

    @Autowired
    private UserRepo userRepo;

    public Page<UserDetailsDTO> getAllDevelopers(Pageable pageable) {
        Page<Users> page=userRepo.findAll(pageable);
        return page.map(this::mapToUserDetailsDto);
    }

    private UserDetailsDTO mapToUserDetailsDto(Users user){
        return new UserDetailsDTO(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getBio(),
                user.getSkills(),
                user.getLinks(),
                user.getRole(),
                user.getExperience(),
                user.getCompany()
        );
    }

    public UserDetailsDTO getDeveloperById(UUID userID) {
           Users user= userRepo.findById(userID)
                   .orElseThrow(()->new UsernameNotFoundException("Developer not found"));
           System.out.println(user);
           return mapToUserDetailsDto(user);
    }

    public Page<UserDetailsDTO> getDevelopersByUsername(String username, Pageable pageable){
       Page<Users> page=  userRepo.findByUsernameIgnoreCase(username, pageable);
       return page.map(this::mapToUserDetailsDto);
    }

    public UserDetailsDTO updateProfile(String email, UpdateProfileReqDTO request) {
        Users user = userRepo.findByEmail(email);
        if(user==null){
            throw new UsernameNotFoundException("User not found with email: " + email);
        }
        user.setUsername(request.getUsername());
        user.setBio(request.getBio());
        user.setSkills(request.getSkills());
        user.setLinks(request.getLinks());
        user.setRole(request.getRole());
        user.setExperience(request.getExperience());
        user.setCompany(request.getCompany());

        Users updatedUser = userRepo.save(user);
        return mapToUserDetailsDto(updatedUser);
    }
}
