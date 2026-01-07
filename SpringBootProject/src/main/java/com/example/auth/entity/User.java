package com.example.auth.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

/**
 * User entity representing application users.
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false)
    private boolean emailVerified;

    @Column(nullable = false)
    private boolean locked;

    @Column(nullable = false)
    private int failedLoginAttempts;

    @Column
    private LocalDateTime lockoutTime;

    @Column
    private String profileImageUrl;

    @Column
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    @Column
    private boolean deleted;

    @Column
    private LocalDateTime deletedAt;

    // Additional fields for GDPR, notifications, etc. can be added here
}
