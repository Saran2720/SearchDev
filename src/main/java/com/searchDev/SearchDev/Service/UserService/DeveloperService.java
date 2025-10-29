package com.searchDev.SearchDev.Service.UserService;

import com.searchDev.SearchDev.DTO.UpdateProfileReqDTO;
import com.searchDev.SearchDev.DTO.UserDetailsDTO;
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

    private final UserRepo userRepo;

    @Autowired
    public DeveloperService(UserRepo userRepo, ProjectRepo projectRepo){
        this.userRepo=userRepo;
    }

    public Page<UserDetailsDTO> getAllDevelopers(Pageable pageable) {
        Page<Users> page=userRepo.findAll(pageable);
        return page.map(this::mapToUserDetailsDto);
    }

    public UserDetailsDTO getDeveloperById(UUID userID)  {
        Users user = findUserByIdOrThrow(userID);
        return mapToUserDetailsDto(user);
    }

    public Page<UserDetailsDTO> getDevelopersByUsername(String username, Pageable pageable) {
        Page<Users> usersPage = userRepo.findByUsernameIgnoreCase(username, pageable);
        if (usersPage.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No developers found with username: " + username);
        }
        return usersPage.map(this::mapToUserDetailsDto);
    }

    public UserDetailsDTO updateProfile(String email, UpdateProfileReqDTO request) {
        Users user = findUserByEmailOrThrow(email);

        // Update user details only if non-null
        if (request.getUsername() != null) user.setUsername(request.getUsername());
        if (request.getBio() != null) user.setBio(request.getBio());
        if (request.getSkills() != null) user.setSkills(request.getSkills());
        if (request.getLinks() != null) user.setLinks(request.getLinks());
        if (request.getRole() != null) user.setRole(request.getRole());
        if (request.getExperience() != null) user.setExperience(request.getExperience());
        if (request.getCompany() != null) user.setCompany(request.getCompany());
        if (request.getProfileImg() != null) user.setProfileImg(request.getProfileImg());

        Users updatedUser = userRepo.save(user);
        return mapToUserDetailsDto(updatedUser);
    }

    public UserDetailsDTO getProfile(String email) {
        Users user = findUserByEmailOrThrow(email);
        return mapToUserDetailsDto(user);
    }

    private Users findUserByIdOrThrow(UUID userId){
        return userRepo.findById(userId).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND,"Developer not found"));
    }
    private Users findUserByEmailOrThrow(String email) {
        Users user = userRepo.findByEmail(email);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with email: " + email);
        }
        return user;
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
}
