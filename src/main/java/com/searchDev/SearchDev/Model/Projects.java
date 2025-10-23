package com.searchDev.SearchDev.Model;


import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name ="projects")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder


public class Projects {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name="UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "project_id",updatable = false,nullable = false)
    private UUID projectId;

    @Column(name = "project_name", nullable = false)
    private String projectName;

    @Column(name = "description")
    private String description;

    @Type(JsonBinaryType.class)
    @Column(name="tech_stack", columnDefinition = "jsonb")
    private List<String> techStack;

    @Type(JsonBinaryType.class)
    @Column(name = "links", columnDefinition = "jsonb")
    private Map<String,Object> links;

    @ManyToOne(fetch = FetchType.LAZY,optional = false) //many project belogns to single user
    @JoinColumn(name = "owner_id",nullable = false) // foreign key column in Project table
    //here we map the entire user object to the project, JPA will take that id from the user and
    // insert it into the owner_id column in the projects table behind the scenes
    private Users owner;


    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public UUID getProjectId() {
        return projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getTechStack() {
        return techStack;
    }

    public Map<String, Object> getLinks() {
        return links;
    }

    public Users getOwner() {
        return owner;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public String toString() {
        return "Projects{" +
                "projectId=" + projectId +
                ", projectName='" + projectName + '\'' +
                ", description='" + description + '\'' +
                ", techStack=" + techStack +
                ", links=" + links +
                ", owner=" + owner +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
