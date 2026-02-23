package com.searchDev.SearchDev.Repository;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import com.searchDev.SearchDev.Model.Comment;

@Repository
public interface CommentRepo extends JpaRepository<Comment, UUID> {
    @Query(value = """
        SELECT c.*
        FROM comments c
        JOIN (
            SELECT DISTINCT
                root_comment_id,
                root_created_at
            FROM comments
            WHERE project_id = :projectId
              AND root_created_at < :cursor
            ORDER BY root_created_at DESC
            LIMIT :limit
        ) r ON c.root_comment_id = r.root_comment_id
        ORDER BY r.root_created_at DESC, c.created_at ASC
        """, nativeQuery = true)
    List<Comment> findCommentsByProjectId(@Param("projectId") UUID projectId,
            @Param("cursor") Instant cursor, 
            @Param("limit") int limit);


            
    @Query(value = """
        SELECT c.*
        FROM comments c
        JOIN (
            SELECT DISTINCT
                root_comment_id,
                root_created_at
            FROM comments
            WHERE project_id = :projectId
            ORDER BY root_created_at DESC
            LIMIT :limit
        ) r ON c.root_comment_id = r.root_comment_id
        ORDER BY r.root_created_at DESC, c.created_at ASC
        """, nativeQuery = true)
    List<Comment> findCommentsByProjectIdFirstPage(@Param("projectId") UUID projectId,
            @Param("limit") int limit);


    Comment findCommentByCommentId(UUID comment_id);
}
