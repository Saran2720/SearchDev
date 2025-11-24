package com.searchDev.SearchDev.Service.Project;

import com.searchDev.SearchDev.DTO.ProjectReqDTO;
import com.searchDev.SearchDev.DTO.ProjectResDTO;
import com.searchDev.SearchDev.DTO.UserDetailsDTO;
import com.searchDev.SearchDev.ExceptionHandler.AccessDeniedException;
import com.searchDev.SearchDev.ExceptionHandler.ResourceNotFoundException;
import com.searchDev.SearchDev.Model.Projects;
import com.searchDev.SearchDev.Model.Users;
import com.searchDev.SearchDev.Repository.ProjectRepo;
import com.searchDev.SearchDev.Repository.UserRepo;
import com.searchDev.SearchDev.Service.UserService.DeveloperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.UUID;

@Service
public class ProjectService {

    private final CacheManager cacheManager;
    @Autowired
    public ProjectService(CacheManager cacheManager){
        this.cacheManager = cacheManager;
    }

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private ProjectRepo projectRepo;

    //create a own project
    public ProjectResDTO createProject(String email, ProjectReqDTO request) {
        Users user = userRepo.findByEmail(email);
        if(user==null){
            throw new UsernameNotFoundException("user not found cannot add project");
        }
        Projects project = Projects.builder()
                .projectName(request.getProjectName())
                .description(request.getDescription())
                .links(request.getLinks())
                .techStack(request.getTechStack())
                .owner(user)
                .build();

        Projects newProject=projectRepo.save(project);
        ProjectResDTO res =mapToProjectResDTO(newProject);
        System.out.println(res);
        return res;
    }

    private ProjectResDTO mapToProjectResDTO(Projects project) {
        return new ProjectResDTO(
                project.getProjectId(),
                project.getOwner().getId(),
                project.getProjectName(),
                project.getDescription(),
                project.getTechStack(),
                project.getLinks(),
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }
    
    //getting all the projects
    public Page<ProjectResDTO> getAllProjects(Pageable pageable) {
        Page<Projects> projects=projectRepo.findAll(pageable);
        return projects.map(this::mapToProjectResDTO);
    }

    //getting a prject by projectId
    @Cacheable(value = "project", key = "#projectId")
    public ProjectResDTO getProjectById(UUID projectId) throws ResourceNotFoundException {
        System.out.println("db hit get");
        Projects project =projectRepo.findById(projectId)
                .orElseThrow(()->new ResourceNotFoundException("Project not found with id: " + projectId));
        return mapToProjectResDTO(project);
    }

     @Autowired
     private DeveloperService developerService;
     //getting the user profile projects
    public List<ProjectResDTO> getProfileProject(String email) {
       UserDetailsDTO user =  developerService.getProfile(email);
       if(user==null){
           throw new IllegalArgumentException("User not found with this email: "+ email);
       }
       List<Projects> projects = projectRepo.findByOwnerIdOrderByCreatedAtDesc(user.getId());
       return projects.stream()
               .map(this::mapToProjectResDTO)
               .toList();
    }
    
    //updating the profile projects only by the projects owner
    @CacheEvict(value = "project", key = "#projectId")
    public ProjectResDTO updateProjectById(UUID projectId, ProjectReqDTO request,String email) throws ResourceNotFoundException, AccessDeniedException {
        System.out.println("db hit put");
        Projects project = projectRepo.findById(projectId).orElseThrow(()-> new ResourceNotFoundException("Project not found with id : "+projectId ));

        //check if the user is owner of this project
        if(!project.getOwner().getEmail().equals(email)){
            throw new AccessDeniedException("You are not allowed to update the project");
        }

        project.setProjectName(request.getProjectName());
        project.setDescription(request.getDescription());
        project.setTechStack(request.getTechStack());
        project.setLinks(request.getLinks());

        Projects updatedProject = projectRepo.save(project);
        return mapToProjectResDTO(updatedProject);
    }
    

    // deleting the profile projects only by the projects owner
    public void deleteProjectById(UUID projectId, String email) throws AccessDeniedException, ResourceNotFoundException {
        Projects project=  projectRepo.findById(projectId).orElseThrow(()-> new ResourceNotFoundException("Project not found with id:"+ projectId));

        //check if the project owner id and email owner id is matching
        if(!project.getOwner().getEmail().equals(email)){
            throw new AccessDeniedException("You are not allowed to delete the project");
        }
        projectRepo.delete(project);
    }
}
