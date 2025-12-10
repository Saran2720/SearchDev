package com.searchDev.SearchDev.Service.UserService;

import com.searchDev.SearchDev.DTO.UpdateProfileReqDTO;
import com.searchDev.SearchDev.DTO.UserDetailsDTO;
import com.searchDev.SearchDev.Model.Users;
import com.searchDev.SearchDev.Repository.UserRepo;
import com.searchDev.SearchDev.Service.RedisService.RedisService;
import com.searchDev.SearchDev.Service.S3service.S3Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.UUID;
import java.util.List;

@Service
public class DeveloperService {

    private final UserRepo userRepo;
    private RedisService redisService;
    private S3Service s3Service;


    @Autowired
    public DeveloperService(UserRepo userRepo,RedisService redisService, S3Service s3Service){
        this.userRepo=userRepo;
        this.redisService=redisService;
        this.s3Service=s3Service;
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
        @SuppressWarnings("unchecked")
        List<UserDetailsDTO> cachedList = redisService.get(key,List.class);

        if(cachedList!=null){
            return new PageImpl<>(cachedList,pageable,cachedList.size());
        }

        System.out.println("db hit for dev profile using username");

        Page<Users> page=userRepo.findByUsernameIgnoreCase(username, pageable);
        List<UserDetailsDTO> dtoList = page.map(this::mapToUserDetailsDto).getContent();
        //cache the data
        redisService.save(key,dtoList,Duration.ofHours(1));
        return new PageImpl<>(dtoList,pageable,page.getTotalElements());
    }

    // Fix: In updateProfile, use correct method references for profileImg; update argument to email for find by email; fix setProfile call.
    public UserDetailsDTO updateProfile(String email, UpdateProfileReqDTO request, MultipartFile profileImg) {
        Users user = findUserByEmailOrThrow(email);

        //clear cache
        String key = "profile:"+email;
        UserDetailsDTO cacheDto = redisService.get(key, UserDetailsDTO.class);
        if(cacheDto!=null){
            redisService.delete(key);
        }

        //upload img to AWS s3
        if(profileImg!=null && !profileImg.isEmpty()){
            String imgUrl = s3Service.upload(profileImg, "profileImg");
            user.setProfileImgUrl(imgUrl);
        }

        // Update user details only if non-null
        if (request.getUsername() != null) user.setUsername(request.getUsername());
        if (request.getBio() != null) user.setBio(request.getBio());
        if (request.getSkills() != null) user.setSkills(request.getSkills());
        if (request.getLinks() != null) user.setLinks(request.getLinks());
        if (request.getRole() != null) user.setRole(request.getRole());
        if (request.getExperience() != null) user.setExperience(request.getExperience());
        if (request.getCompany() != null) user.setCompany(request.getCompany());
        
        //save to db
        Users updatedUser = userRepo.save(user);
        UserDetailsDTO userDetails = mapToUserDetailsDto(updatedUser);
        
        //cache the data
        redisService.save(key, userDetails, Duration.ofHours(2));

        return mapToUserDetailsDto(updatedUser);
    }


    public UserDetailsDTO getProfile(String email) {
        //get the data if present in cache
        String key = "profile:"+email;
        UserDetailsDTO cachedProfile = redisService.get(key,UserDetailsDTO.class);
        if(cachedProfile!=null){
            return cachedProfile;
        }
        //get the use details
        Users user = findUserByEmailOrThrow(email);
        UserDetailsDTO userDetailsDTO= mapToUserDetailsDto(user);
        //cache the data
        redisService.save(key, userDetailsDTO, Duration.ofHours(2));
        return userDetailsDTO;
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
                user.getProfileImgUrl()       
        );
    }
}
