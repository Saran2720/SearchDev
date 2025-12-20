package com.searchDev.SearchDev.Service.Project;

import org.springframework.data.domain.PageImpl;
import com.searchDev.SearchDev.DTO.ProjectReqDTO;
import com.searchDev.SearchDev.DTO.ProjectResDTO;
import com.searchDev.SearchDev.DTO.UserDetailsDTO;
import com.searchDev.SearchDev.DTO.cacheWrapper.PageProjectsCache;
import com.searchDev.SearchDev.ExceptionHandler.AccessDeniedException;
import com.searchDev.SearchDev.ExceptionHandler.ResourceNotFoundException;
import com.searchDev.SearchDev.Model.Projects;
import com.searchDev.SearchDev.Model.Users;
import com.searchDev.SearchDev.Repository.ProjectRepo;
import com.searchDev.SearchDev.Repository.UserRepo;
import com.searchDev.SearchDev.Service.RedisService.RedisService;
import com.searchDev.SearchDev.Service.S3service.S3Service;
import com.searchDev.SearchDev.Service.UserService.DeveloperService;

import com.fasterxml.jackson.core.type.TypeReference;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Service
public class ProjectService {

    private RedisService redisService;
    private UserRepo userRepo;
    private ProjectRepo projectRepo;
    private S3Service s3Service;

    @Autowired
    public ProjectService(RedisService redisService, UserRepo userRepo, ProjectRepo projectRepo, S3Service s3Service) {
        this.redisService = redisService;
        this.projectRepo = projectRepo;
        this.userRepo = userRepo;
        this.s3Service = s3Service;
    }

    @Autowired
    private DeveloperService developerService;

    // create a own project
    public ProjectResDTO createProject(String email, ProjectReqDTO request) {

        // delete the profile project data if it is already in the cache
        String key = "project:profile:" + email;
        redisService.delete(key);

        Users user = userRepo.findByEmail(email);
        if (user == null) {
            throw new UsernameNotFoundException("user not found cannot add project");
        }
        Projects project = Projects.builder()
                .projectName(request.getProjectName())
                .description(request.getDescription())
                .links(request.getLinks())
                .techStack(request.getTechStack())
                .owner(user)
                .build();

        Projects newProject = projectRepo.save(project);
        ProjectResDTO res = mapToProjectResDTO(newProject);
        return res;
    }

    // getting a project by projectId
    public ProjectResDTO getProjectById(UUID projectId) throws ResourceNotFoundException {
        String key = "project:" + projectId;
        ProjectResDTO dto = redisService.get(key, new TypeReference<ProjectResDTO>() {
        });
        if (dto == null) {
            Projects project = projectRepo.findById(projectId)
                    .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));
            dto = mapToProjectResDTO(project);
            redisService.save(key, dto, Duration.ofHours(2));
        }

        if (dto.getFileKey() != null) {
            dto.setProjectImgUrl(presignedGetUrl(dto.getFileKey()));
        }
        return dto;
    }

    // updating the profile projects only by the projects owner
    public ProjectResDTO updateProjectById(UUID projectId, ProjectReqDTO request, String email)
            throws ResourceNotFoundException, AccessDeniedException {

        Projects project = projectRepo.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id : " + projectId));

        // check if the user is owner of this project
        if (!project.getOwner().getEmail().equals(email)) {
            throw new AccessDeniedException("You are not allowed to update the project");
        }

        // delete the data if it is already in the cache
        String key = "project:" + projectId;
        redisService.delete(key);
        String profileProjectsKey = "project:profile:" + email;
        redisService.delete(profileProjectsKey);

        project.setProjectName(request.getProjectName());
        project.setDescription(request.getDescription());
        project.setTechStack(request.getTechStack());
        project.setLinks(request.getLinks());

        Projects updatedProject = projectRepo.save(project);
        ProjectResDTO dto = mapToProjectResDTO(updatedProject);
        return dto;
    }

    // update the project img after image hosted in s3 bucket
    public void confirmUpdate(String fileKey, UUID projectId, String email) {
        Projects project = projectRepo.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id : " + projectId));

        // detete from cache
        String key = "project:" + projectId;
        redisService.delete(key);
        String profileProjectsKey = "project:profile:" + email;
        redisService.delete(profileProjectsKey);

        // delete oldFilekey from s3 bucket
        String oldFileKey = project.getFileKey();
        if (oldFileKey != null) {
            s3Service.deleteFile(oldFileKey);
            System.out.println("projectimg delted from s3");
        }

        // update and save
        project.setFileKey(fileKey);
        projectRepo.save(project);
    }

    // deleting the profile projects only by the projects owner
    public void deleteProjectById(UUID projectId, String email)
            throws AccessDeniedException, ResourceNotFoundException {

        // delte the data if it is already in the cache
        String key = "project:" + projectId;
        redisService.delete(key);
        String profileProjectsKey = "project:profile:" + email;
        redisService.delete(profileProjectsKey);

        System.out.println("db hit for delted");
        Projects project = projectRepo.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id:" + projectId));

        // check if the project owner id and email owner id is matching
        if (!project.getOwner().getEmail().equals(email)) {
            throw new AccessDeniedException("You are not allowed to delete the project");
        }

        // delete the image form the s3bucket
        String fileKey = project.getFileKey();
        if (fileKey != null) {
            s3Service.deleteFile(fileKey);
        }
        // delete from db
        projectRepo.delete(project);
    }

    // getting the user profile projects
    public List<ProjectResDTO> getProfileProject(String email) {
        // check if the user is a valid user or not
        UserDetailsDTO user = developerService.getProfile(email);
        if (user == null) {
            throw new IllegalArgumentException("User not found with this email: " + email);
        }
        // get from cache
        String key = "project:profile:" + email;
        List<ProjectResDTO> dtoList = redisService.get(key, new TypeReference<List<ProjectResDTO>>() {
        });

        if (dtoList == null) {
            List<Projects> projects = projectRepo.findByOwnerIdOrderByCreatedAtDesc(user.getId());
            // map
            dtoList = projects.stream()
                    .map(this::mapToProjectResDTO)
                    .toList();
            /// save to cache
            redisService.save(key, dtoList, Duration.ofHours(2));
        }

        for (ProjectResDTO dto : dtoList) {
            if (dto.getFileKey() != null) {
                String projectImgUrl = presignedGetUrl(dto.getFileKey());
                dto.setProjectImgUrl(projectImgUrl);
            }
        }
        return dtoList;
    }

    // getting all the projects
    public Page<ProjectResDTO> getAllProjects(Pageable pageable) {

        // key for cache
        String key = "project:all:" + pageable.getPageNumber() + ":" + pageable.getPageSize();

        // page wrapper
        PageProjectsCache cache = redisService.get(key, new TypeReference<PageProjectsCache>() {
        });
        List<ProjectResDTO> dtoList;
        long totalElements;

        // get from db
        if (cache == null) {
            Page<Projects> page = projectRepo.findAll(pageable);
            totalElements = page.getTotalElements();

            dtoList = page
                    .map(this::mapToProjectResDTO)
                    .getContent();

            // save to cache
            redisService.save(
                    key, new PageProjectsCache(dtoList, totalElements), Duration.ofMinutes(20));

        } else { // get from cache (page wrapper)
            dtoList = cache.getProjects();
            totalElements = cache.getTotalElements();
        }

        // get and set the image
        if (!dtoList.isEmpty()) {
            for (ProjectResDTO dto : dtoList) {
                if (dto.getFileKey() != null) {
                    dto.setProjectImgUrl(presignedGetUrl(dto.getFileKey()));
                }
            }
        }
        return new PageImpl<>(dtoList, pageable, totalElements);
    }

    // helper function to map the project to the project response dto
    private ProjectResDTO mapToProjectResDTO(Projects project) {
        return new ProjectResDTO(
                project.getProjectId(),
                project.getFileKey(),
                null,
                project.getOwner().getId(),
                project.getProjectName(),
                project.getDescription(),
                project.getTechStack(),
                project.getLinks(),
                project.getCreatedAt(),
                project.getUpdatedAt());
    }

    // get the presigned get url
    private String presignedGetUrl(String fileKey) {
        return s3Service.generatePresignedGetUrl(fileKey);
    }
}
