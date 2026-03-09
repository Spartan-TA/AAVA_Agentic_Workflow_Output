package com.company.wems.security.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Entity representing an application user for authentication and authorization.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user", indexes = {
        @Index(name = "idx_user_username", columnList = "username"),
        @Index(name = "idx_user_email", columnList = "email")
})
@EntityListeners(AuditingEntityListener.class)
public class User implements UserDetails {
    /**
     * Primary key for User.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Unique username for login.
     */
    @Column(name = "username", nullable = false, unique = true, length = 64)
    private String username;

    /**
     * Encrypted password.
     */
    @Column(name = "password", nullable = false, length = 128)
    private String password;

    /**
     * User's email address.
     */
    @Column(name = "email", nullable = false, unique = true, length = 128)
    private String email;

    /**
     * Roles assigned to the user.
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role", length = 32)
    private Set<String> roles;

    /**
     * Whether the user is enabled.
     */
    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;

    /**
     * Whether the account is non-expired.
     */
    @Column(name = "account_non_expired", nullable = false)
    private boolean accountNonExpired = true;

    /**
     * Whether the account is non-locked.
     */
    @Column(name = "account_non_locked", nullable = false)
    private boolean accountNonLocked = true;

    /**
     * Whether the credentials are non-expired.
     */
    @Column(name = "credentials_non_expired", nullable = false)
    private boolean credentialsNonExpired = true;

    /**
     * Creation timestamp.
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Last update timestamp.
     */
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Returns authorities granted to the user.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles == null ? Set.of() : roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toSet());
    }

    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
