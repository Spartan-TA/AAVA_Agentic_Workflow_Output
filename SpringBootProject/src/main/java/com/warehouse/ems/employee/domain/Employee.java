package com.warehouse.ems.employee.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Employee entity representing warehouse employees.
 */
@Entity
@Table(name = "employees", uniqueConstraints = @UniqueConstraint(columnNames = "badge_id"))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE employees SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "badge_id", nullable = false, unique = true)
    private String badgeId;

    @Column(name = "role", nullable = false)
    private String role;

    @Column(name = "department", nullable = false)
    private String department;

    @Column(name = "shift_group")
    private String shiftGroup;

    @Column(name = "hire_date")
    private LocalDate hireDate;

    @Column(name = "status", nullable = false)
    private String status;

    // Audit fields
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", updatable = false)
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        // TODO: Set createdBy from security context
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
        // TODO: Set updatedBy from security context
    }
}
