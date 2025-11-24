package com.warehousemgmt.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.time.LocalDate;

/**
 * Employee entity representing warehouse staff.
 * Includes RBAC role, department, shift group, and soft delete support.
 */
@Entity
@Table(name = "employees", uniqueConstraints = @UniqueConstraint(columnNames = "badge_id"))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 100)
    private String name;

    @NotBlank
    @Column(name = "badge_id", nullable = false, unique = true)
    @Size(max = 32)
    private String badgeId;

    @NotBlank
    @Size(max = 32)
    private String role; // ADMIN, HR, SUPERVISOR, WORKER

    @NotBlank
    @Size(max = 64)
    private String department;

    @Size(max = 32)
    private String shiftGroup;

    @NotNull
    private LocalDate hireDate;

    @NotBlank
    @Size(max = 32)
    private String status; // ACTIVE, INACTIVE, TERMINATED

    @NotNull
    private Boolean deleted = false;

    @Column(name = "tenant_id")
    private String tenantId;
}
