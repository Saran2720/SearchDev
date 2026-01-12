package com.searchDev.SearchDev.controller.Project;


import com.searchDev.SearchDev.DTO.ApiResDTO;
import com.searchDev.SearchDev.DTO.ProjectResDTO;
import com.searchDev.SearchDev.ExceptionHandler.ResourceNotFoundException;
import com.searchDev.SearchDev.Security.RateLimiter;
import com.searchDev.SearchDev.Service.Project.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/projects")
public class AllProjectController {

    @Autowired
    private ProjectService projectService;

    //returning the list of projects in page
    @RateLimiter(request = 15, durationSeconds = 60)
    @GetMapping()
    public ResponseEntity<ApiResDTO<Page<ProjectResDTO>>> getProjects(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "2") int size
    ){
        try{
            if(page<0 || size <=0) {
                throw new IllegalArgumentException("Page index must be grater that 0");
            }
            Page<ProjectResDTO> projects =projectService.getAllProjects(PageRequest.of(page, size));
            ApiResDTO<Page<ProjectResDTO>> pageApiResDTO = ApiResDTO.<Page<ProjectResDTO>>builder()
                    .success(true)
                    .status(HttpStatus.OK.value())
                    .message("Projects fetched successfully")
                    .data(projects)
                    .timestamp(LocalDateTime.now())
                    .build();
            return ResponseEntity.ok(pageApiResDTO);
        }catch (Exception e){
            ApiResDTO<Page<ProjectResDTO>> errorResponse = ApiResDTO.<Page<ProjectResDTO>>builder()
                    .success(false)
                    .status(HttpStatus.BAD_REQUEST.value())
                    .message(e.getMessage())
                    .data(null)
                    .timestamp(LocalDateTime.now())
                    .build();

            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @RateLimiter(request = 5, durationSeconds = 60)
    @GetMapping("/{projectId}")
    public ResponseEntity<ApiResDTO<ProjectResDTO>> getProjectById(@PathVariable UUID projectId) throws ResourceNotFoundException {
        try{
            ProjectResDTO project = projectService.getProjectById(projectId);
            ApiResDTO<ProjectResDTO> apiProjectRes = ApiResDTO.<ProjectResDTO>builder()
                    .success(true)
                    .status(HttpStatus.OK.value())
                    .message("Project fetched successfully")
                    .data(project)
                    .timestamp(LocalDateTime.now())
                    .build();

            return ResponseEntity.ok(apiProjectRes);
        }catch(Exception e){
            ApiResDTO<ProjectResDTO> errorRes = ApiResDTO.<ProjectResDTO>builder()
                    .success(false)
                    .status(HttpStatus.NOT_FOUND.value())
                    .message(e.getMessage())
                    .data(null)
                    .timestamp(LocalDateTime.now())
                    .build();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorRes);
        }
    }
}
