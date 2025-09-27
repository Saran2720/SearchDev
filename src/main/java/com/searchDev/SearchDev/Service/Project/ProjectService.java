package com.searchDev.SearchDev.Service.Project;


import com.searchDev.SearchDev.DTO.ProjectReqDTO;
import com.searchDev.SearchDev.DTO.ProjectResDTO;
import com.searchDev.SearchDev.Model.Projects;
import com.searchDev.SearchDev.Model.Users;
import com.searchDev.SearchDev.Repository.ProjectRepo;
import com.searchDev.SearchDev.Repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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

    public ProjectResDTO getProjectById(UUID projectId) {
        Projects project =projectRepo.findById(projectId)
                .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
        return mapToProjectResDTO(project);
    }
}
