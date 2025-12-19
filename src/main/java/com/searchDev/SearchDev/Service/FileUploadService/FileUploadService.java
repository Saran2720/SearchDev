package com.searchDev.SearchDev.Service.FileUploadService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.searchDev.SearchDev.Service.Project.ProjectService;
import com.searchDev.SearchDev.Service.S3service.S3Service;
import com.searchDev.SearchDev.Service.UserService.DeveloperService;
import com.searchDev.SearchDev.DTO.ProjectResDTO;
import com.searchDev.SearchDev.DTO.UserDetailsDTO;
import com.searchDev.SearchDev.ExceptionHandler.ResourceNotFoundException;
import com.searchDev.SearchDev.Model.Projects;
import com.searchDev.SearchDev.Model.Users;
import com.searchDev.SearchDev.Repository.ProjectRepo;
import com.searchDev.SearchDev.Repository.UserRepo;
import org.springframework.http.HttpStatus;

import java.util.Map;
import java.util.UUID;

@Service
public class FileUploadService {

    private DeveloperService developerService;
    private S3Service s3Service;
    private UserRepo userRepo;
    private ProjectService projectService;
    private ProjectRepo projectRepo;

    @Autowired
    public FileUploadService(DeveloperService developerService, S3Service s3Service, UserRepo userRepo,
            ProjectService projectService, ProjectRepo projectRepo) {
        this.developerService = developerService;
        this.s3Service = s3Service;
        this.userRepo = userRepo;
        this.projectService = projectService;
        this.projectRepo = projectRepo;
    }

    public Map<String, String> generateProfilePresignedUrl(String email, String extension) {
        // get a key to make the path
        String fileKey = "profileImg/" + email + System.currentTimeMillis() + "." + extension;
        String presignedUrl = "";
        try {
            // get the presignedUrl
            presignedUrl = s3Service.generatePresignedPutUrl(fileKey);
        } catch (Exception e) {
            return Map.of("error", "Failed to generate presigned URL: " + e.getMessage());
        }
        return Map.of("presignedUrl", presignedUrl,
                "fileKey", fileKey);
    }

    public Map<String, String> generateProjectPresignedUrl(UUID id, String extension) {

        String fileKey = "projectImg/" + id + System.currentTimeMillis() + "." + extension;
        String presignedUrl = "";

        try {
            presignedUrl = s3Service.generatePresignedPutUrl(fileKey);
        } catch (Exception e) {
            return Map.of("error", "Failed to generate presigned URL: " + e.getMessage());
        }

        return Map.of("presignedUrl", presignedUrl,
                "fileKey", fileKey);
    }
}
