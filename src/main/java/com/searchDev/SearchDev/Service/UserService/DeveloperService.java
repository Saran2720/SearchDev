package com.searchDev.SearchDev.Service.UserService;

import com.searchDev.SearchDev.DTO.UpdateProfileReqDTO;
import com.searchDev.SearchDev.DTO.UserDetailsDTO;
import com.searchDev.SearchDev.DTO.cacheWrapper.PageUsersCache;
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
    public DeveloperService(UserRepo userRepo, RedisService redisService, S3Service s3Service) {
        this.userRepo = userRepo;
        this.redisService = redisService;
        this.s3Service = s3Service;
    }
     //--------------------------------------page of users-------------------------------------

    // get all the users
    public Page<UserDetailsDTO> getAllUsers(Pageable pageable) {
        String key = "users:all" + pageable.getPageNumber() + ":" + pageable.getPageSize();
        // get the data if it is cached
        PageUsersCache cache = redisService.get(key, PageUsersCache.class);

        List<UserDetailsDTO> dtoList;
        long totalElements;

        if (cache == null) {
            Page<Users> page = userRepo.findAll(pageable);
            dtoList = page.map(this::mapToUserDetailsDto).getContent();
            totalElements = page.getTotalElements();

            // save data to cache
            redisService.save(
                    key,
                    new PageUsersCache(dtoList, totalElements),
                    Duration.ofMinutes(20));
        } else {
            dtoList = cache.getUsers();
            totalElements = cache.getTotalElements();
        }

        // get the profileIMgUrl for all users
        if (dtoList.size() > 0) {
            for (UserDetailsDTO user : dtoList) {
                String fileKey = user.getFileKey();
                if (fileKey != null) {
                    user.setProfileImgUrl(presignedGetUrl(fileKey));
                }
            }
        }

        return new PageImpl<>(dtoList, pageable, totalElements);
    }

    // get the users by username
    public Page<UserDetailsDTO> getUserByUsername(String username, Pageable pageable) {
        
        String key = "seacrh:username:" + username.toLowerCase() + "page:" + pageable.getPageNumber() + "size:"
                + pageable.getPageSize();
        PageUsersCache cache = redisService.get(key, PageUsersCache.class);

        List<UserDetailsDTO> dtoList;
        long totalElements;

        // get from DB and cache the data
        if (cache == null) {
            Page<Users> page = userRepo.findByUsernameIgnoreCase(username, pageable);

            dtoList = page.map(this::mapToUserDetailsDto).getContent();
            totalElements = page.getTotalElements();

            redisService.save(
                    key,
                    new PageUsersCache(dtoList, totalElements),
                    Duration.ofMinutes(30));
            System.out.println("db hit");
        } else {
            dtoList = cache.getUsers();
            totalElements = cache.getTotalElements();
        }

        // get the profileIMgUrl for all users
        if (dtoList.size() > 0) {
            for (UserDetailsDTO user : dtoList) {
                String fileKey = user.getFileKey();
                if (fileKey != null) {
                    user.setProfileImgUrl(presignedGetUrl(fileKey));
                }
            }
        }
        // return users
        return new PageImpl<>(dtoList, pageable, totalElements);
    }

    // -------------------------------single profile get and put methods by email or ID-------------------------------

    public UserDetailsDTO updateProfile(String email, UpdateProfileReqDTO request) {
        Users user = findUserByEmailOrThrow(email);

        // Update user details only if non-null
        if (request.getUsername() != null)
            user.setUsername(request.getUsername());
        if (request.getBio() != null)
            user.setBio(request.getBio());
        if (request.getSkills() != null)
            user.setSkills(request.getSkills());
        if (request.getLinks() != null)
            user.setLinks(request.getLinks());
        if (request.getRole() != null)
            user.setRole(request.getRole());
        if (request.getExperience() != null)
            user.setExperience(request.getExperience());
        if (request.getCompany() != null)
            user.setCompany(request.getCompany());

        // save to db
        Users updatedUser = userRepo.save(user);

        // clear old cache and save newone
        String key = "user:" + updatedUser.getId();
        redisService.delete(key);
        // Map and cache
        UserDetailsDTO userDetails = mapToUserDetailsDto(updatedUser);
        redisService.save(key, userDetails, Duration.ofHours(2));

        return userDetails;
    }

    // get the user by email
    public UserDetailsDTO getProfile(String email) {
        Users ownUser = findUserByEmailOrThrow(email);
        // get the data if present in cache and generate the presigned get url
        String key = "user:" + ownUser.getId();
        UserDetailsDTO dto = redisService.get(key, UserDetailsDTO.class);

        if (dto == null) {
            Users user = findUserByEmailOrThrow(email);
            dto = mapToUserDetailsDto(user);
            redisService.save(key, dto, Duration.ofHours(2));
        }

        if (dto.getFileKey() != null) {
            dto.setProfileImgUrl(
                    presignedGetUrl(dto.getFileKey()));
        }
        return dto;
    }

    // get the user by id
    public UserDetailsDTO getDeveloperById(UUID userID) {
        String key = "user:" + userID;
        UserDetailsDTO dto = redisService.get(key, UserDetailsDTO.class);
        if (dto == null) {
            Users user = findUserByIdOrThrow(userID);
            dto = mapToUserDetailsDto(user);
            redisService.save(key, dto, Duration.ofHours(2));
        }

        if (dto.getFileKey() != null) {
            dto.setProfileImgUrl(
                    presignedGetUrl(dto.getFileKey()));
        }
        return dto;
    }
    // ----------------------------------helper
    // funcitons-------------------------------------------------------

    // get the presigned get url
    private String presignedGetUrl(String fileKey) {
        return s3Service.generatePresignedGetUrl(fileKey);
    }

    private Users findUserByIdOrThrow(UUID userId) {
        // System.out.println("db hit for dev profile using ID");
        return userRepo.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private Users findUserByEmailOrThrow(String email) {
        // System.out.println("profile db hit");
        Users user = userRepo.findByEmail(email);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with email: " + email);
        }
        return user;
    }

    private UserDetailsDTO mapToUserDetailsDto(Users user) {
        return new UserDetailsDTO(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getBio(),
                null, // profileImgUrl (generated later)
                user.getFileKey(), // fileKey
                user.getSkills(),
                user.getLinks(),
                user.getRole(),
                user.getExperience(),
                user.getCompany());
    }

}
