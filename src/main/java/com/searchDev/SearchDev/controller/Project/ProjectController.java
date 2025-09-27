package com.searchDev.SearchDev.controller.Project;


import com.searchDev.SearchDev.DTO.ProjectReqDTO;
import com.searchDev.SearchDev.DTO.ProjectResDTO;
import com.searchDev.SearchDev.Model.UserPrincipal;
import com.searchDev.SearchDev.Service.Project.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/profile")
public class ProjectController {
    @Autowired
    private ProjectService projectService;

    @PostMapping("/projects")
    public ResponseEntity<ProjectResDTO> createProject(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody ProjectReqDTO request
            ){
        String email = userPrincipal.getUsername();
        ProjectResDTO project =projectService.createProject(email, request);
        return ResponseEntity.ok(project);
    }
}
