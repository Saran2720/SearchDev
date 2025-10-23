package com.searchDev.SearchDev.controller.Project;


import com.searchDev.SearchDev.DTO.ProjectReqDTO;
import com.searchDev.SearchDev.DTO.ProjectResDTO;
import com.searchDev.SearchDev.ExceptionHandler.ResourceNotFoundException;
import com.searchDev.SearchDev.Model.UserPrincipal;
import com.searchDev.SearchDev.Service.Project.ProjectService;
import com.searchDev.SearchDev.Service.UserService.DeveloperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.searchDev.SearchDev.ExceptionHandler.AccessDeniedException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/profile")
public class ProjectController {
    @Autowired
    private ProjectService projectService;

    @Autowired
    private DeveloperService developerService;

    @PostMapping("/projects")
    public ResponseEntity<ProjectResDTO> createProject(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody ProjectReqDTO request
            ){
        String email = userPrincipal.getUsername();
        ProjectResDTO project =projectService.createProject(email, request);
        return ResponseEntity.ok(project);
    }


    @GetMapping("/projects")
    public ResponseEntity<List<ProjectResDTO>> getProfileProject(@AuthenticationPrincipal UserPrincipal userPrincipal){
        String email = userPrincipal.getUsername();
       List<ProjectResDTO> projects =  projectService.getProfileProject(email);
       return ResponseEntity.ok(projects);
    }

    @PutMapping("/projects/{id}")
    public ResponseEntity<ProjectResDTO> updateProjectById(@PathVariable UUID id, @RequestBody ProjectReqDTO request, @AuthenticationPrincipal UserPrincipal userPrincipal) throws ResourceNotFoundException, AccessDeniedException {
        String email = userPrincipal.getUsername();
        ProjectResDTO project= projectService.updateProjectById(id,request,email);
       return ResponseEntity.ok(project);
    }

    @DeleteMapping("/projects/{id}")
    public ResponseEntity<?> deleteProject(@PathVariable UUID id, @AuthenticationPrincipal UserPrincipal userPrincipal) throws AccessDeniedException, ResourceNotFoundException {
        String email = userPrincipal.getUsername();
       projectService.deleteProjectById(id,email);
       return ResponseEntity.noContent().build();// HTTP 204 No Content
    }
}
