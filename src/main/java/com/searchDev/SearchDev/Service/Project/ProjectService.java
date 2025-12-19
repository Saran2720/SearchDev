package com.searchDev.SearchDev.Service.Project;

import org.springframework.data.domain.PageImpl;
import com.searchDev.SearchDev.DTO.ProjectReqDTO;
import com.searchDev.SearchDev.DTO.ProjectResDTO;
import com.searchDev.SearchDev.DTO.UserDetailsDTO;
import com.searchDev.SearchDev.ExceptionHandler.AccessDeniedException;
import com.searchDev.SearchDev.ExceptionHandler.ResourceNotFoundException;
import com.searchDev.SearchDev.Model.Projects;
import com.searchDev.SearchDev.Model.Users;
import com.searchDev.SearchDev.Repository.ProjectRepo;
import com.searchDev.SearchDev.Repository.UserRepo;
import com.searchDev.SearchDev.Service.RedisService.RedisService;
import com.searchDev.SearchDev.Service.S3service.S3Service;
import com.searchDev.SearchDev.Service.UserService.DeveloperService;
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
        ProjectResDTO dto = redisService.get(key, ProjectResDTO.class);
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


    // Fix: Use correct type in Redis call and add type safety

    public List<ProjectResDTO> getProfileProject(String email) {
        // getting the project from the cache
        String key = "project:profile:" + email;
        List<ProjectResDTO> cached = redisService.get(key,List.class);
        if (cached != null) {
            System.out.println("cache hit for profileProjectList");
            for (ProjectResDTO dto : cached) {
                if (dto.getFileKey() != null) {
                    String projectImgUrl = presignedGetUrl(dto.getFileKey());
                    dto.setProjectImgUrl(projectImgUrl);
                }
            }
            return cached;
        }

        System.out.println("db hit for profileProjectList");
        UserDetailsDTO user = developerService.getProfile(email);
        if (user == null) {
            throw new IllegalArgumentException("User not found with this email: " + email);
        }
        List<Projects> projects = projectRepo.findByOwnerIdOrderByCreatedAtDesc(user.getId());
        // map and save to cache
        List<ProjectResDTO> dtoProjects = projects.stream()
                .map(this::mapToProjectResDTO)
                .toList();
        redisService.save(key, dtoProjects, Duration.ofHours(2));

        // assign projectGETUrl for each projects
        for (ProjectResDTO dto : dtoProjects) {
            if (dto.getFileKey() != null) {
                String projectImgUrl = presignedGetUrl(dto.getFileKey());
                dto.setProjectImgUrl(projectImgUrl);
            }
        }
        return dtoProjects;
    }
    // getting the user profile projects
    // public List<ProjectResDTO> getProfileProject(String email) {

    //     // getting the project from the cache
    //     String key = "project:profile:" + email;
    //     List<ProjectResDTO> cached = redisService.get(key, List.class);
    //     if (cached != null) {
    //         System.out.println("cache hit for profileProjectList");
    //         for (ProjectResDTO dto : cached) {
    //             if (dto.getFileKey() != null) {
    //                 String projectImgUrl = presignedGetUrl(dto.getFileKey());
    //                 dto.setProjectImgUrl(projectImgUrl);
    //             }
    //         }
    //         return cached;
    //     }

    //     System.out.println("db hit for profileProjectList");
    //     UserDetailsDTO user = developerService.getProfile(email);
    //     if (user == null) {
    //         throw new IllegalArgumentException("User not found with this email: " + email);
    //     }
    //     List<Projects> projects = projectRepo.findByOwnerIdOrderByCreatedAtDesc(user.getId());
    //     // map and save to cache
    //     List<ProjectResDTO> dtoProjects = projects.stream()
    //             .map(this::mapToProjectResDTO)
    //             .toList();
    //     redisService.save(key, dtoProjects, Duration.ofHours(2));

    //     // assign projectGETUrl for each projects
    //     for (ProjectResDTO dto : dtoProjects) {
    //         if (dto.getFileKey() != null) {
    //             String projectImgUrl = presignedGetUrl(dto.getFileKey());
    //             dto.setProjectImgUrl(projectImgUrl);
    //         }
    //     }
    //     return dtoProjects;
    // }

    // getting all the projects
    public Page<ProjectResDTO> getAllProjects(Pageable pageable) {
        String key = "project:all" + pageable.getPageNumber() + ":" + pageable.getPageSize();

        // Try to retrieve the list of ProjectResDTO from Redis (use array for type-safe
        // cast)
        List<ProjectResDTO> cachedList = redisService.get(key, List.class);
        if (cachedList != null) {
            System.out.println("cache hit");
            int totalElements = cachedList.size();
            return new PageImpl<>(cachedList, pageable, totalElements);
        }

        System.out.println("DB hit → getAllProjects");
        Page<Projects> projectsPage = projectRepo.findAll(pageable);
        List<ProjectResDTO> dtoList = projectsPage.map(this::mapToProjectResDTO).getContent();

        // Store dtoList as value in redis using list for safe deserialization
        redisService.save(key, dtoList, Duration.ofMinutes(30));

        for (ProjectResDTO dto : dtoList) {
            if (dto.getFileKey() != null) {
                String projectImgUrl = presignedGetUrl(dto.getFileKey());
                dto.setProjectImgUrl(projectImgUrl);
            }
        }

        // Return a new PageImpl, using the actual paging info from the DB query
        return new PageImpl<>(dtoList, pageable, projectsPage.getTotalElements());
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
