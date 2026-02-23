package com.searchDev.SearchDev.controller.Project.ProjectComment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.UUID;
import com.searchDev.SearchDev.Model.UserPrincipal;
import java.time.Instant;
import java.util.List;

import com.searchDev.SearchDev.DTO.CommentReqDTO;
import com.searchDev.SearchDev.DTO.CommentResDTO;
import com.searchDev.SearchDev.Service.commentService.CommentService;

@RestController
@RequestMapping("project/projectComment")
public class ProjectCommentController {
    private CommentService commentService;

    @Autowired
    ProjectCommentController(CommentService commentService){
        this.commentService = commentService;
    }
     
    //get commnent
    @GetMapping("/{projectId}")
    public List<List<CommentResDTO>> getCommentsByProjectId(@PathVariable UUID projectId,
            @RequestParam(required = false) Instant lastrootCreatedAt,
            @RequestParam(defaultValue = "2") int limit){
        return commentService.getCommentsByProjectId(projectId, lastrootCreatedAt, limit);
        // return "Hello";
    }


    //root comment
    @PostMapping("/{projectId}")
    public CommentResDTO postComment(@PathVariable UUID projectId , @RequestBody CommentReqDTO commentReqDTO,
        @AuthenticationPrincipal UserPrincipal userPrincipal
    ){
        return commentService.postRootComment(projectId, commentReqDTO.getComment(), userPrincipal.getUsername());
    }


    //reply comment
    @PostMapping("/{projectId}/reply/{parentId}")
    public CommentResDTO postReplyComment(@PathVariable UUID projectId, @PathVariable UUID parentId, @RequestBody CommentReqDTO commentReqDTO,
        @AuthenticationPrincipal UserPrincipal userPrincipal
    ){
        return commentService.postReplyComment(projectId, parentId, commentReqDTO.getComment(), userPrincipal.getUsername());
    }
}

