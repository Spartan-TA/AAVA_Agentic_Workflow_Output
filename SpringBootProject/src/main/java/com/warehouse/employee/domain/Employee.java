package com.warehouse.employee.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Employee entity representing warehouse staff.
 */
@Entity
@Table(name = "employee", indexes = {
        @Index(name = "idx_employee_badge_id", columnList = "badge_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "badge_id", nullable = false, unique = true, length = 32)
    private String badgeId;

    @Column(nullable = false, length = 128)
    private String name;

    @Column(nullable = false, length = 32)
    private String role;

    @Column(length = 64)
    private String department;

    @Column(name = "shift_group", length = 64)
    private String shiftGroup;

    @Column(name = "hire_date", nullable = false)
    private LocalDate hireDate;

    @Column(nullable = false, length = 32)
    private String status;

    @Column(nullable = false)
    private Boolean deleted = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
