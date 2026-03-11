package com.example.app.entity;

import lombok.*;
import javax.persistence.*;
import javax.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.Set;

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

    @NotBlank
    @Column(unique = true, nullable = false)
    private String username;

    @NotBlank
    @Email
    @Column(unique = true, nullable = false)
    private String email;

    @NotBlank
    private String password;

    private boolean enabled;

    private boolean mfaEnabled;

    private String mfaSecret;

    private String profilePictureUrl;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private UserPreferences preferences;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private Set<AccountActivity> activities;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private Set<Notification> notifications;
}
