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
            FROM Comment c
            JOIN( SELECT root_id, root_created_at
            FROM Comment
            WHERE project_id=:project_id
            AND (:lastrootCreatedAt IS NULL OR
                 root_created_at< : lastrootCreatedAt)
            ORDER BY root_created_at DESC
            LIMIT :limit
            ) r ON c.root_id = r.root_id
            ORDER BY r.root_created_at DESC , c.created_at ASC
            """, nativeQuery = true)

    List<Comment> findCommentsByProjectId(@Param("projectId") UUID projectId,
            @Param("lastrootCreatedAt") Instant lastrootCreatedAt, 
            @Param("limit") int limit);
}
