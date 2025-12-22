package com.searchDev.SearchDev.Service.FileUploadService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.searchDev.SearchDev.Service.S3service.S3Service;

import java.util.Map;
import java.util.UUID;

@Service
public class FileUploadService {

    private S3Service s3Service;

    @Autowired
    public FileUploadService( S3Service s3Service) {
        this.s3Service = s3Service;
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
