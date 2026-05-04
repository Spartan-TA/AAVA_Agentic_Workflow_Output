package com.warehouse.management.security.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * Entity representing an application user for authentication and RBAC.
 */
@Entity
@Table(name = "app_users")
@Schema(description = "Application user entity for authentication and RBAC")
public class AppUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "User ID", example = "1")
    private Long id;

    @NotBlank
    @Column(unique = true, nullable = false)
    @Schema(description = "Username", example = "john.doe")
    private String username;

    @NotBlank
    @Column(nullable = false)
    @Schema(description = "Password (hashed)")
    private String password;

    @Email
    @Column(unique = true, nullable = false)
    @Schema(description = "Email address", example = "john.doe@example.com")
    private String email;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    @Schema(description = "List of roles", example = "["ROLE_USER", "ROLE_ADMIN"]")
    private List<String> roles;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles; }
}
