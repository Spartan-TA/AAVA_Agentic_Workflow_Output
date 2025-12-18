package com.warehouse.ems.employee.entity;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * Employee entity representing warehouse employees.
 */
@Entity
@Table(name = "employees")
@EntityListeners(AuditingEntityListener.class)
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 50)
    @Column(unique = true, nullable = false)
    private String username;

    @NotBlank
    @Size(max = 100)
    private String password;

    @NotBlank
    @Size(max = 100)
    private String fullName;

    @Email
    @NotBlank
    @Size(max = 100)
    private String email;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Role role;

    @Column(nullable = false)
    private boolean active = true;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @CreatedBy
    private String createdBy;

    @LastModifiedBy
    private String updatedBy;

    @Column(nullable = false)
    private boolean deleted = false;

    /**
     * Soft delete method.
     */
    public void softDelete() {
        this.deleted = true;
        this.active = false;
    }

    // Getters and setters omitted for brevity

    public enum Role {
        ADMIN, MANAGER, EMPLOYEE
    }
}
