package com.searchDev.SearchDev.controller.Project;


import com.searchDev.SearchDev.DTO.ApiResDTO;
import com.searchDev.SearchDev.DTO.ProjectReqDTO;
import com.searchDev.SearchDev.DTO.ProjectResDTO;
import com.searchDev.SearchDev.ExceptionHandler.ResourceNotFoundException;
import com.searchDev.SearchDev.Model.UserPrincipal;
import com.searchDev.SearchDev.Service.Project.ProjectService;
import com.searchDev.SearchDev.Service.UserService.DeveloperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.searchDev.SearchDev.ExceptionHandler.AccessDeniedException;

import java.time.LocalDateTime;
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
    public ResponseEntity<ApiResDTO<ProjectResDTO>> createProject(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody ProjectReqDTO request
            ){
        try{
            String email = userPrincipal.getUsername();
            ProjectResDTO project =projectService.createProject(email, request);
            ApiResDTO<ProjectResDTO> apiProjectRes = ApiResDTO.<ProjectResDTO>builder()
                    .success(true)
                    .status(HttpStatus.OK.value())
                    .message("Project posted successfully")
                    .data(project)
                    .build();
            return ResponseEntity.ok(apiProjectRes);
        } catch (Exception e) {
            ApiResDTO<ProjectResDTO> errorRes = ApiResDTO.<ProjectResDTO>builder()
                    .success(false)
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message(e.getMessage())
                    .data(null)
                    .timestamp(LocalDateTime.now())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorRes);
        }
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
