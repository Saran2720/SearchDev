package com.searchDev.SearchDev.Service.S3service;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;


@Service
public class S3Service {

    @Value("${aws.s3.bucket}")
    private String bucketName;

    private final AmazonS3 s3Client;

    @Autowired
    public S3Service(AmazonS3 s3Client){
        this.s3Client= s3Client;
    }

    public String upload(MultipartFile file, String folder){
        String extension = Objects.requireNonNull(file.getOriginalFilename()).substring(file.getOriginalFilename().lastIndexOf("."));
        String fileName = UUID.randomUUID()+ extension;
        String key = folder + "/" + fileName;

        try{
            ObjectMetadata meta = new ObjectMetadata();
            meta.setContentType(file.getContentType());
            meta.setContentLength(file.getSize());

            s3Client.putObject(
                new PutObjectRequest(bucketName, key, file.getInputStream(),meta)
                    .withCannedAcl(CannedAccessControlList.PublicRead)
            );
        }catch(IOException e){
            throw new RuntimeException("Failed to upload file",e);
        }
        return s3Client.getUrl(bucketName, key).toString();
    }
}
