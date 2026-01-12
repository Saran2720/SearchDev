package com.searchDev.SearchDev.Service.commentService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.searchDev.SearchDev.Repository.CommentRepo;
import com.searchDev.SearchDev.Model.Comment;
import java.util.List;
import java.time.Instant;
import java.util.ArrayList;
import java.util.UUID;

@Service
public class CommentService {
    private CommentRepo commentRepo;

    @Autowired
    CommentService(CommentRepo commentRepo) {
        this.commentRepo = commentRepo;
    }

    public List<List<Comment>> getCommentsByProjectId(UUID projectId, Instant lastrootCreatedAt, int limit) {
        
        List<Comment> comments = commentRepo.findCommentsByProjectId(projectId, lastrootCreatedAt, limit);
        
        List<List<Comment>> threads = new ArrayList<>();
        List<Comment> curr = null;
        UUID prevRootId = null;
        for (Comment c : comments) {
            if (!c.getRootId().equals(prevRootId)) {
                curr = new ArrayList<>();
                threads.add(curr);
                prevRootId = c.getRootId();
            }
            curr.add(c);
        }
        return threads;
    }

}
