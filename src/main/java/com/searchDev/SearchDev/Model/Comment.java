package com.searchDev.SearchDev.Model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PrePersist;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;

@Entity
@Table(name="comments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comment {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "comment_id", updatable = false, nullable = false)
    private UUID commentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Projects project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id", nullable = false)
    private Users user;

    @Column(name ="username", nullable = false)
    private String username;

    @Column(name = "parent_comment_id")
    private UUID parrentCommentID;  

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_user")
    private Users parentUser;
    

    @Column(name="root_comment_id", nullable = false)
    private UUID rootCommentId;

    @Column(nullable = false, length = 2000)
    private String content;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name="root_created_at", updatable = false)
    private LocalDateTime rootCreatedAt;
    
    @PrePersist 
    protected void onCreate(){
        this.createdAt = LocalDateTime.now();
        if(this.parrentCommentID == null){
            this.rootCreatedAt = this.createdAt;
        }
    }
    
    @PostPersist
    protected void onPostPersist(){
        // For root comments, set rootCommentId to commentId after ID is generated
        if(this.parrentCommentID == null && this.rootCommentId == null){
            this.rootCommentId = this.commentId;
        }
    }
    
}
