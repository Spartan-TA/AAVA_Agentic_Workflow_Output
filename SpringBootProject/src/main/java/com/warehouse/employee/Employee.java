package com.warehouse.employee;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Employee entity representing warehouse staff.
 */
@Entity
@Table(name = "employees", uniqueConstraints = {@UniqueConstraint(columnNames = "badge_id")})
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

    @NotBlank
    @Column(nullable = false)
    private String name;

    @NotBlank
    @Column(name = "badge_id", nullable = false, unique = true)
    private String badgeId;

    @NotBlank
    @Column(nullable = false)
    private String role;

    @NotBlank
    @Column(nullable = false)
    private String department;

    @NotBlank
    @Column(nullable = false)
    private String shiftGroup;

    @PastOrPresent
    @Column(nullable = false)
    private LocalDate hireDate;

    @NotBlank
    @Column(nullable = false)
    private String status;

    // Soft delete flag
    @Column(nullable = false)
    private boolean deleted = false;

    // Audit fields
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
