package com.searchDev.SearchDev.controller.Project;


import com.searchDev.SearchDev.DTO.ProjectResDTO;
import com.searchDev.SearchDev.Service.Project.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/projects")
public class AllProjectController {

    @Autowired
    private ProjectService projectService;

    //returning the list of projects in page
    @GetMapping()
    public ResponseEntity<Page<ProjectResDTO>> getProjects(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "2") int size
    ){
        if(page<0 || size <=0) {
            throw new IllegalArgumentException("Page index must be grater that 0");
        }
        Page<ProjectResDTO> projects =projectService.getAllProjects(PageRequest.of(page, size));
        return ResponseEntity.ok(projects);
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<ProjectResDTO> getProjectById(@PathVariable UUID projectId) {
        ProjectResDTO project = projectService.getProjectById(projectId);
        return ResponseEntity.ok(project);
    }
}
