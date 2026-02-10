package com.searchDev.SearchDev.Service.commentService;

import org.springframework.beans.factory.annotation.Autowired;
// import com.searchDev.SearchDev.Service.commentService.TimeFormatterUtil;
import org.springframework.stereotype.Service;
import com.searchDev.SearchDev.Repository.CommentRepo;
import com.searchDev.SearchDev.Repository.ProjectRepo;
import com.searchDev.SearchDev.Repository.UserRepo;
import com.searchDev.SearchDev.Model.Comment;
import com.searchDev.SearchDev.Model.Projects;
import com.searchDev.SearchDev.Model.Users;
import com.searchDev.SearchDev.ExceptionHandler.ResourceNotFoundException;
import java.util.List;
import java.time.Instant;
import java.util.ArrayList;
import java.util.UUID;

@Service
public class CommentService {
    private CommentRepo commentRepo;
    private UserRepo userRepo;
    private ProjectRepo projectRepo;
    // private TimeFormatterUtil timeFormatterUtil;

    @Autowired
    CommentService(CommentRepo commentRepo, UserRepo userRepo, ProjectRepo projectRepo) {
        this.commentRepo = commentRepo;
        this.userRepo = userRepo;
        this.projectRepo = projectRepo;
    }

    public List<List<Comment>> getCommentsByProjectId(UUID projectId, Instant lastrootCreatedAt, int limit) {

        List<Comment> comments;
        if (lastrootCreatedAt == null) {
            comments = commentRepo.findCommentsByProjectIdFirstPage(projectId, limit);
        } else {
            comments = commentRepo.findCommentsByProjectId(projectId, lastrootCreatedAt, limit);
        }
        
        List<List<Comment>> threads = new ArrayList<>();
        List<Comment> curr = null;
        UUID prevRootId = null;

        for (Comment c : comments) {
            if (prevRootId == null || !c.getRootId().equals(prevRootId)) {
                curr = new ArrayList<>();
                threads.add(curr);
                prevRootId = c.getRootId();
            }
            curr.add(c);
        }
        return threads;
    }

    //root comment 
    public Comment postRootComment(UUID projectId, String comment, String email){
        Users user = userRepo.findByEmail(email);
        Projects project = getProjectById(projectId);


        Comment newRootComment = Comment.builder()
                                .parentId(null)
                                .rootId(user.getId())
                                .project(project)
                                .content(comment)
                                .user(user)
                                .username(user.getUsername())
                                .build();
        
       return commentRepo.save(newRootComment);
    }

    //reply comment
    public Comment postReplyComment(UUID projectId, UUID parentId, String comment, String email){
        
        Users user = userRepo.findByEmail(email);
        Projects project = getProjectById(projectId);
        Comment parentComment = commentRepo.findCommentByCommentId(parentId);

        Comment newReplyComment = Comment.builder()
                                  .parentId(parentId)
                                  .rootId(parentComment.getRootId())
                                  .project(project)
                                  .content(comment)
                                  .user(user)
                                  .username(user.getUsername())
                                  .build();
        return commentRepo.save(newReplyComment);
    }


    private Projects getProjectById(UUID projectId){
        return projectRepo.findById(projectId)
        .orElseThrow(() -> new ResourceNotFoundException("Project not found with id : " + projectId));
    }
}
