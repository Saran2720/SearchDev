package com.searchDev.SearchDev.controller.Project.ProjectComment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import java.time.LocalDateTime;
import java.util.List;

import com.searchDev.SearchDev.DTO.ApiResDTO;
import com.searchDev.SearchDev.DTO.CommentReqDTO;
import com.searchDev.SearchDev.DTO.CommentResDTO;
import com.searchDev.SearchDev.Service.commentService.CommentService;

@RestController
@RequestMapping("project/projectComment")
public class ProjectCommentController {
    private CommentService commentService;

    @Autowired
    ProjectCommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    // get commnent
    @GetMapping("/{projectId}")
    public ResponseEntity<ApiResDTO<List<List<CommentResDTO>>>> getCommentsByProjectId(@PathVariable UUID projectId,
            @RequestParam(required = false) Instant lastrootCreatedAt,
            @RequestParam(defaultValue = "2") int limit) {

        try {
            List<List<CommentResDTO>> commentList = commentService.getCommentsByProjectId(projectId, lastrootCreatedAt,
                    limit);

            ApiResDTO<List<List<CommentResDTO>>> response = ApiResDTO.<List<List<CommentResDTO>>>builder()
                    .success(true)
                    .status(HttpStatus.OK.value())
                    .message("comment fetched successfully")
                    .data(commentList)
                    .timestamp(LocalDateTime.now())
                    .build();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            ApiResDTO<List<List<CommentResDTO>>> response = ApiResDTO.<List<List<CommentResDTO>>>builder()
                    .success(false)
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("failed to fetch comments")
                    .data(null)
                    .timestamp(LocalDateTime.now())
                    .build();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // root comment
    @PostMapping("/{projectId}")
    public ResponseEntity<ApiResDTO<Boolean>> postComment(@PathVariable UUID projectId,
            @RequestBody CommentReqDTO commentReqDTO,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        try {
            boolean isPosted = commentService.postRootComment(projectId, commentReqDTO.getComment(),
                    userPrincipal.getUsername());

            ApiResDTO<Boolean> response = ApiResDTO.<Boolean>builder()
                    .success(isPosted)
                    .status(HttpStatus.OK.value())
                    .message("comment posted")
                    .data(null)
                    .timestamp(LocalDateTime.now())
                    .build();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResDTO<Boolean> response = ApiResDTO.<Boolean>builder()
                    .success(false)
                    .status(HttpStatus.BAD_REQUEST.value())
                    .message("comment not posted")
                    .data(null)
                    .timestamp(LocalDateTime.now())
                    .build();

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    // reply comment
    @PostMapping("/{projectId}/reply/{parentId}")
    public ResponseEntity<ApiResDTO<Boolean>> postReplyComment(@PathVariable UUID projectId,
            @PathVariable UUID parentId,
            @RequestBody CommentReqDTO commentReqDTO,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        try {
            boolean isPosted = commentService.postReplyComment(projectId, parentId, commentReqDTO.getComment(),
                    userPrincipal.getUsername());
            ApiResDTO<Boolean> response = ApiResDTO.<Boolean>builder()
                    .success(isPosted)
                    .status(HttpStatus.OK.value())
                    .message("comment posted")
                    .data(isPosted)
                    .timestamp(LocalDateTime.now())
                    .build();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResDTO<Boolean> response = ApiResDTO.<Boolean>builder()
                    .success(false)
                    .status(HttpStatus.BAD_REQUEST.value())
                    .message("comment not posted")
                    .data(null)
                    .timestamp(LocalDateTime.now())
                    .build();

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
}
