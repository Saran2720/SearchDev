package com.searchDev.SearchDev.Model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "PasswordReset")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PasswordResetToken {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name="UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "resetTokenId" , updatable = false, nullable = false)
    private UUID resetTokenId;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name="time",nullable = false)
    private LocalDateTime time;
}
