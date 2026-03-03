package com.searchDev.SearchDev.Service.commentService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.searchDev.SearchDev.Repository.CommentRepo;
import com.searchDev.SearchDev.Repository.ProjectRepo;
import com.searchDev.SearchDev.Repository.UserRepo;
import com.searchDev.SearchDev.Model.Comment;
import com.searchDev.SearchDev.Model.Projects;
import com.searchDev.SearchDev.Model.Users;
import com.searchDev.SearchDev.DTO.CommentResDTO;
import com.searchDev.SearchDev.ExceptionHandler.ResourceNotFoundException;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;
import java.time.Instant;
import java.util.stream.Collectors;

@Service
public class CommentService {
    private final CommentRepo commentRepo;
    private final UserRepo userRepo;
    private final ProjectRepo projectRepo;

    @Autowired
    public CommentService(CommentRepo commentRepo, UserRepo userRepo, ProjectRepo projectRepo) {
        this.commentRepo = commentRepo;
        this.userRepo = userRepo;
        this.projectRepo = projectRepo;
    }
    

    //get comment for a project
    @Transactional(readOnly = true)
    public List<List<CommentResDTO>> getCommentsByProjectId(UUID projectId, Instant lastrootCreatedAt, int limit) {
        List<Comment> comments = (lastrootCreatedAt == null) 
            ? commentRepo.findCommentsByProjectIdFirstPage(projectId, limit)
            : commentRepo.findCommentsByProjectId(projectId, lastrootCreatedAt, limit);
        
        List<List<Comment>> threads = groupCommentsByThread(comments);

        return threads.stream()
            .map(thread -> thread.stream().map(this::toResDTO).collect(Collectors.toList()))
            .collect(Collectors.toList());
    }
    
    //group the projects
    private List<List<Comment>> groupCommentsByThread(List<Comment> comments) {
        List<List<Comment>> threads = new ArrayList<>();
        UUID currentRootId = null;
        
        for (Comment comment : comments) {
            if (currentRootId == null || !comment.getRootCommentId().equals(currentRootId)) {
                threads.add(new ArrayList<>());
                currentRootId = comment.getRootCommentId();
            }
            threads.get(threads.size() - 1).add(comment);
        }
        return threads;
    }


    //root comment
    @Transactional
    public boolean postRootComment(UUID projectId, String content, String email) {
        Users user = userRepo.findByEmail(email);
        Projects project = getProjectById(projectId);
        
        Comment rootComment = Comment.builder()
            .parrentCommentID(null)
            .parentUser(null)
            .rootCommentId(UUID.randomUUID()) // Temporary, will be updated after save
            .project(project)
            .content(content)
            .user(user)
            .username(user.getUsername())
            .build();
        
        Comment saved = commentRepo.save(rootComment);
        saved.setRootCommentId(saved.getCommentId());
        Comment updated = commentRepo.save(saved);
        if(updated!=null){
            return true;
        }
        return false;
    }



    //reply comment
    @Transactional
    public boolean postReplyComment(UUID projectId, UUID parentCommentId, String content, String email) {
        Users user = userRepo.findByEmail(email);
        Projects project = getProjectById(projectId);
        Comment parentComment = commentRepo.findCommentByCommentId(parentCommentId);

        Comment reply = Comment.builder()
            .parrentCommentID(parentCommentId)
            .parentUser(parentComment.getUser())
            .rootCommentId(parentComment.getRootCommentId())
            .rootCreatedAt(parentComment.getRootCreatedAt())
            .project(project)
            .content(content)
            .user(user)
            .username(user.getUsername())
            .build();
            
        commentRepo.save(reply);

        if(reply!=null) return true;
        return false;
    }


    private Projects getProjectById(UUID projectId) {
        return projectRepo.findById(projectId)
            .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));
    }

    private CommentResDTO toResDTO(Comment comment) {
        Users author = comment.getUser();
        Users repliedTo = comment.getParentUser();

        return CommentResDTO.builder()
            .commentId(comment.getCommentId())
            .userId(author != null ? author.getId() : null)
            .username(comment.getUsername() != null ? comment.getUsername() : (author != null ? author.getUsername() : null))
            .parentCommentId(comment.getParrentCommentID())
            .repliedToUserId(repliedTo != null ? repliedTo.getId() : null)
            .repliedToUsername(repliedTo != null ? repliedTo.getUsername() : null)
            .content(comment.getContent())
            .createdAt(comment.getCreatedAt() != null ? comment.getCreatedAt().toString() : null)
            .rootCommentId(comment.getRootCommentId())
            .build();
    }
}
