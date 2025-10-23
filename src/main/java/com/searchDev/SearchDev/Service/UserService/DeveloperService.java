package com.searchDev.SearchDev.Service.UserService;

import com.searchDev.SearchDev.DTO.UpdateProfileReqDTO;
import com.searchDev.SearchDev.DTO.UserDetailsDTO;
import com.searchDev.SearchDev.ExceptionHandler.ResourceNotFoundException;
import com.searchDev.SearchDev.Model.Users;
import com.searchDev.SearchDev.Repository.ProjectRepo;
import com.searchDev.SearchDev.Repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Base64;
import java.util.UUID;


@Service
public class DeveloperService {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private ProjectRepo projectRepo;

    public Page<UserDetailsDTO> getAllDevelopers(Pageable pageable) {
        Page<Users> page=userRepo.findAll(pageable);
        return page.map(this::mapToUserDetailsDto);
    }

    private UserDetailsDTO mapToUserDetailsDto(Users user){

        String profileImgBase64 =null;
        if(user.getProfileImg()!=null){
            profileImgBase64= Base64.getEncoder().encodeToString(user.getProfileImg());
        }
        return new UserDetailsDTO(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getBio(),
                user.getSkills(),
                user.getLinks(),
                user.getRole(),
                user.getExperience(),
                user.getCompany(),
                profileImgBase64
        );
    }

    public UserDetailsDTO getDeveloperById(UUID userID)  {
           Users user= userRepo.findById(userID)
                   .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Developer not found"));
           return mapToUserDetailsDto(user);
    }

    public Page<UserDetailsDTO> getDevelopersByUsername(String username, Pageable pageable){
       Page<Users> page=  userRepo.findByUsernameIgnoreCase(username, pageable);
       return page.map(this::mapToUserDetailsDto);
    }

    public UserDetailsDTO updateProfile(String email, UpdateProfileReqDTO request) {
        Users user = userRepo.findByEmail(email);
        if(user==null){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"User not found with email: " + email);
        }
        user.setUsername(request.getUsername());
        user.setBio(request.getBio());
        user.setSkills(request.getSkills());
        user.setLinks(request.getLinks());
        user.setRole(request.getRole());
        user.setExperience(request.getExperience());
        user.setCompany(request.getCompany());

        if(request.getProfileImg()!=null){
            user.setProfileImg(request.getProfileImg());
        }

        Users updatedUser = userRepo.save(user);
        return mapToUserDetailsDto(updatedUser);
    }

    public UserDetailsDTO getProfile(String email) {
        Users user = userRepo.findByEmail(email);
        if(user == null){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"user not found");
        }
        return mapToUserDetailsDto(user);
    }

}
