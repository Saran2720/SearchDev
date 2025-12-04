package com.searchDev.SearchDev.Service.UserService;

import com.searchDev.SearchDev.DTO.UpdateProfileReqDTO;
import com.searchDev.SearchDev.DTO.UserDetailsDTO;
import com.searchDev.SearchDev.Model.Users;
import com.searchDev.SearchDev.Repository.UserRepo;
import com.searchDev.SearchDev.Service.RedisService.RedisService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.Base64;
import java.util.UUID;
import java.util.List;

@Service
public class DeveloperService {

    private final UserRepo userRepo;
    private RedisService redisService;


    @Autowired
    public DeveloperService(UserRepo userRepo,RedisService redisService){
        this.userRepo=userRepo;
        this.redisService=redisService;
    }
    
    //get all the developers
    public Page<UserDetailsDTO> getAllDevelopers(Pageable pageable) {
        String key = "developers:all"+ pageable.getPageNumber()+":"+pageable.getPageSize();
        //get the data if it is cached
        List<UserDetailsDTO> cachedList = redisService.get(key,List.class);
        if(cachedList!=null){
            int totalElements = cachedList.size();
            return new PageImpl<>(cachedList,pageable,totalElements);
        }

        //if not cached get it from db
        Page<Users> page=userRepo.findAll(pageable);
        List<UserDetailsDTO> dtoList = page.map(this::mapToUserDetailsDto).getContent();
        //store it on redis 
        redisService.save(key, dtoList, Duration.ofHours(1));
        return new PageImpl<>(dtoList,pageable,page.getTotalElements());
    }
    
    //get the developer by id
    public UserDetailsDTO getDeveloperById(UUID userID)  {
        String key = "developer:"+userID;
        UserDetailsDTO cachedUser = redisService.get(key,UserDetailsDTO.class);
        if(cachedUser!=null){
            return cachedUser;
        }
        System.out.println("db hit for dev profile using ID");
        Users user = findUserByIdOrThrow(userID);
        UserDetailsDTO userDetails = mapToUserDetailsDto(user);
        redisService.save(key,userDetails,Duration.ofHours(1));
        return userDetails;
    }
    
   //get the developers by username
    public Page<UserDetailsDTO> getDevelopersByUsername(String username, Pageable pageable) {
        String key = "developers:username:"+username+":"+pageable.getPageNumber()+":"+pageable.getPageSize();
        List<UserDetailsDTO> cachedList = redisService.get(key,List.class);
        if(cachedList!=null){
            return new PageImpl<>(cachedList,pageable,cachedList.size());
        }
        System.out.println("db hit for dev profile using username");
        Page<Users> page=userRepo.findByUsernameIgnoreCase(username, pageable);
        List<UserDetailsDTO> dtoList = page.map(this::mapToUserDetailsDto).getContent();
        redisService.save(key,dtoList,Duration.ofHours(1));
        return new PageImpl<>(dtoList,pageable,page.getTotalElements());
    }


    public UserDetailsDTO updateProfile(String email, UpdateProfileReqDTO request) {
        Users user = findUserByEmailOrThrow(email);
        UUID userId = user.getId(); // Get user ID before updating
        String key = "developer:"+userId;
        redisService.delete(key);
        System.out.println("db hit for update dev profile");
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
        UserDetailsDTO userDetails = mapToUserDetailsDto(updatedUser);
        redisService.save(key,userDetails,Duration.ofHours(1));
        return mapToUserDetailsDto(updatedUser);
    }

    public UserDetailsDTO getProfile(String email) {
        String key = "profile:"+email;
        UserDetailsDTO cachedProfile = redisService.get(key,UserDetailsDTO.class);
        if(cachedProfile!=null){
            return cachedProfile;
        }
        System.out.println("db hit for get dev profile");
        Users user = findUserByEmailOrThrow(email);
        return mapToUserDetailsDto(user);
    }


    //helper funcitons-------------------------------------------------------

    private Users findUserByIdOrThrow(UUID userId){
        // System.out.println("db hit for dev profile using ID");
        return userRepo.findById(userId).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND,"User not found"));
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
