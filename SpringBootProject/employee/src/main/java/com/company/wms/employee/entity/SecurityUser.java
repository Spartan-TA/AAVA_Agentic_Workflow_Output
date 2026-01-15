package com.company.wms.security.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

/**
 * Entity representing a security user for authentication and authorization.
 */
@Entity
@Table(name = "security_users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SecurityUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Username is required")
    @Column(nullable = false, unique = true)
    private String username;

    @NotBlank(message = "Password is required")
    @Column(nullable = false)
    private String password;

    @NotBlank(message = "Role is required")
    @Column(nullable = false)
    private String role;

    @NotNull(message = "Active status is required")
    @Column(nullable = false)
    private Boolean active;
}
