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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.UUID;

@Service
public class ProjectService {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private ProjectRepo projectRepo;

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
//        System.out.println(newProject);
        ProjectResDTO res =mapToProjectResDTO(newProject);
//        System.out.println("after map to project res dto");
        System.out.println(res);
        return res;
    }

    private ProjectResDTO mapToProjectResDTO(Projects project) {
//        System.out.println(project);
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

    public Page<ProjectResDTO> getAllProjects(Pageable pageable) {
        Page<Projects> projects=projectRepo.findAll(pageable);
        return projects.map(this::mapToProjectResDTO);
    }

    public ProjectResDTO getProjectById(UUID projectId) throws ResourceNotFoundException {
        Projects project =projectRepo.findById(projectId)
                .orElseThrow(()->new ResourceNotFoundException("Project not found with id: " + projectId));
        return mapToProjectResDTO(project);
    }

     @Autowired
     private DeveloperService developerService;
    public List<ProjectResDTO> getProfileProject(String email) {
       UserDetailsDTO user =  developerService.getProfile(email);
       if(user==null){
           throw new IllegalArgumentException("User not found with this email: "+ email);
       }
       List<Projects> projects = projectRepo.findByOwner_Id(user.getId());
       return projects.stream()
               .map(this::mapToProjectResDTO)
               .toList();
    }

    public ProjectResDTO updateProjectById(UUID projectId, ProjectReqDTO request,String email) throws ResourceNotFoundException, AccessDeniedException {
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

    public void deleteProjectById(UUID projectId, String email) throws AccessDeniedException, ResourceNotFoundException {
        Projects project=  projectRepo.findById(projectId).orElseThrow(()-> new ResourceNotFoundException("Project not found with id:"+ projectId));

        //check if the project owner id and email owner id is matching
        if(!project.getOwner().getEmail().equals(email)){
            throw new AccessDeniedException("You are not allowed to delete the project");
        }
        projectRepo.delete(project);
    }


}
