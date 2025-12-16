package com.searchDev.SearchDev.Service.FileUploadService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.searchDev.SearchDev.Service.S3service.S3Service;
import com.searchDev.SearchDev.Service.UserService.DeveloperService;
import com.searchDev.SearchDev.DTO.UserDetailsDTO;
import com.searchDev.SearchDev.Model.Users;
import com.searchDev.SearchDev.Repository.UserRepo;
import org.springframework.http.HttpStatus;


import java.util.Map;
import java.util.UUID;

@Service
public class FileUploadService {

    private DeveloperService developerService;
    private S3Service s3Service;
    private UserRepo userRepo;

    @Autowired
    public FileUploadService(DeveloperService developerService,S3Service s3Service, UserRepo userRepo){
        this.developerService =developerService;
        this.s3Service = s3Service;
        this.userRepo=userRepo;
    }
    

    public Map<String,String> generateProfilePresignedUrl(String email, String extension){
        //get a key to make the path
        String fileKey = "profileImg/" + email + System.currentTimeMillis() + "." + extension;
        String presignedUrl="";
        try{
            //get the presignedUrl and publicUrl
            presignedUrl = s3Service.generatePresignedPutUrl(fileKey);
        }catch(Exception e){
            return Map.of("error", "Failed to generate presigned URL: " + e.getMessage());
        }
        
        //save the key to db
        Users user = findUserByEmailOrThrow(email);
        String oldKey = user.getFileKey();
        if(oldKey!=null){
            s3Service.deleteFile(oldKey);
        }
        user.setFileKey(fileKey);
        userRepo.save(user);

        return Map.of("presignedUrl",presignedUrl,
                      "fileKey",fileKey
        );
    }


    //helper functions
        // System.out.println("profile db hit");
        private Users findUserByEmailOrThrow(String email) {
            // System.out.println("profile db hit");
            Users user = userRepo.findByEmail(email);
            if (user == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with email: " + email);
            }
            return user;
        }
}
