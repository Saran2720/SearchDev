package com.searchDev.SearchDev.Service.S3service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;

@Service
public class S3Service {

    @Value("${aws.s3.bucket}")
    private String bucketName;

    private final AmazonS3 s3Client;

    @Autowired
    public S3Service(AmazonS3 s3Client) {
        this.s3Client = s3Client;
    }

    public String generatePresignedPutUrl(String fileKey) {
        java.util.Date expiration = new java.util.Date();
        long expTimeMillis = expiration.getTime();
        // Set presigned URL expiration to 15 minutes from now
        expTimeMillis += 1000 * 60 * 15;
        expiration.setTime(expTimeMillis);

        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucketName, fileKey)
                .withMethod(com.amazonaws.HttpMethod.PUT)
                .withExpiration(expiration);

        // request.addRequestParameter("x-amz-acl", "public-read"); //make the object
        // only public not the entire bucket
        // request.setContentType("image/*");

        return s3Client.generatePresignedUrl(request).toString();
    }

    // generate the presigned url for get
    public String generatePresignedGetUrl(String fileKey) {
        java.util.Date expiration = new java.util.Date();
        long expTimeMillis = expiration.getTime();
        // Set presigned URL expiration to 5 minutes from now
        expTimeMillis += 1000 * 60 * 5;
        expiration.setTime(expTimeMillis);

        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucketName, fileKey)
                                             .withMethod(HttpMethod.GET)
                                             .withExpiration(expiration);
        return s3Client.generatePresignedUrl(request).toString();
    }

    public void deleteFile(String key) {
        s3Client.deleteObject(bucketName, key);
    }
}

// Private Bucket but public objects -> IMPORTANT
