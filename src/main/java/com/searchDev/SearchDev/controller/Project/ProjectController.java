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
    ) {
        String email = userPrincipal.getUsername();
        ProjectResDTO project = projectService.createProject(email, request);

        ApiResDTO<ProjectResDTO> apiResponse = ApiResDTO.<ProjectResDTO>builder()
                .success(true)
                .status(HttpStatus.CREATED.value())
                .message("Project created successfully")
                .data(project)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }

   // getting the profile projects
    @GetMapping("/projects")
    public ResponseEntity<ApiResDTO<List<ProjectResDTO>>> getProfileProject(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        String email = userPrincipal.getUsername();
        List<ProjectResDTO> projects = projectService.getProfileProject(email);

        ApiResDTO<List<ProjectResDTO>> response = ApiResDTO.<List<ProjectResDTO>>builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .message("Profile projects fetched successfully")
                .data(projects)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(response);
    }


   // updating the profile project 
    @PutMapping("/projects/{id}")
    public ResponseEntity<ApiResDTO<ProjectResDTO>> updateProjectById(
            @PathVariable UUID id,
            @RequestBody ProjectReqDTO request,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) throws ResourceNotFoundException, AccessDeniedException {
        String email = userPrincipal.getUsername();
        ProjectResDTO updated = projectService.updateProjectById(id, request, email);

        ApiResDTO<ProjectResDTO> apiResponse = ApiResDTO.<ProjectResDTO>builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .message("Project updated successfully")
                .data(updated)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(apiResponse);
    }

    // Delete the profile projects
    @DeleteMapping("/projects/{id}")
    public ResponseEntity<ApiResDTO<Void>> deleteProject(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) throws AccessDeniedException, ResourceNotFoundException {
        String email = userPrincipal.getUsername();
        projectService.deleteProjectById(id, email);

        ApiResDTO<Void> apiResponse = ApiResDTO.<Void>builder()
                .success(true)
                .status(HttpStatus.NO_CONTENT.value())
                .message("Project deleted successfully")
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(apiResponse);
    }
}
