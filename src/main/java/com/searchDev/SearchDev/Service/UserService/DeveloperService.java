package com.searchDev.SearchDev.Service.UserService;

import com.searchDev.SearchDev.DTO.UpdateProfileReqDTO;
import com.searchDev.SearchDev.DTO.UserDetailsDTO;
import com.searchDev.SearchDev.Model.Users;
import com.searchDev.SearchDev.Repository.ProjectRepo;
import com.searchDev.SearchDev.Repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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
    private final CacheManager cacheManager;

    @Autowired
    public DeveloperService(UserRepo userRepo, ProjectRepo projectRepo, CacheManager cacheManager){
        this.userRepo=userRepo;
        this.cacheManager = cacheManager;
    }

    public Page<UserDetailsDTO> getAllDevelopers(Pageable pageable) {
        Page<Users> page=userRepo.findAll(pageable);
        return page.map(this::mapToUserDetailsDto);
    }
    
    @Cacheable(value = "userProfile" , key = "#userID")
    public UserDetailsDTO getDeveloperById(UUID userID)  {
        Users user = findUserByIdOrThrow(userID);
        return mapToUserDetailsDto(user);
    }
    
    // @Cacheable(value = "developerProfile", key = "#username")
    public Page<UserDetailsDTO> getDevelopersByUsername(String username, Pageable pageable) {
        Page<Users> usersPage = userRepo.findByUsernameIgnoreCase(username, pageable);
        if (usersPage.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No developers found with username: " + username);
        }
        return usersPage.map(this::mapToUserDetailsDto);
    }


    @CacheEvict(value = "userProfile", key = "#email")
    public UserDetailsDTO updateProfile(String email, UpdateProfileReqDTO request) {
        Users user = findUserByEmailOrThrow(email);
        UUID userId = user.getId(); // Get user ID before updating

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
        
        // Evict cache by user ID as well (for getDeveloperById)
        if (cacheManager != null) {
            var cache = cacheManager.getCache("userProfile");
            if (cache != null) {
                cache.evict(userId);
            }
        }
        
        return mapToUserDetailsDto(updatedUser);
    }

    @Cacheable(value = "userProfile" , key = "#email") // caching the user profile data
    public UserDetailsDTO getProfile(String email) {
        Users user = findUserByEmailOrThrow(email);
        return mapToUserDetailsDto(user);
    }


    //helper funcitons-------------------------------------------------------

    private Users findUserByIdOrThrow(UUID userId){
        // System.out.println("db hit for dev profile using ID");
        return userRepo.findById(userId).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND,"Developer not found"));
    }

    private Users findUserByEmailOrThrow(String email) {
        // System.out.println("profile db hit");
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
