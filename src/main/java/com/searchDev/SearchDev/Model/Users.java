package com.searchDev.SearchDev.Model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;


import java.time.LocalDateTime;
import java.util.UUID;


@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Users {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false,unique = true)
    private String email;

    @Column(nullable = false,name = "username")
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(name = "bio" , length = 1000)
    private String bio;

    @Column(name = "pofile_pic")
    private String profilePic;

//    @Column(name ="skills",columnDefinition = "jsonb", nullable = true)
//    private String skills;

//    @Column(name ="links",columnDefinition = "jsonb",nullable = true)
//    private String links;

    @Column (name="role")
    private String role;

    @Column(name = "experience")
    private String experience;

    @Column (name ="company")
    private String company;

    @Column(name="created_at",updatable = false)
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

    @Override
    public String toString() {
        return "Users{" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", id=" + id +
                ", email='" + email + '\'' +
                '}';
    }

//    private List<Project> projects;




}
