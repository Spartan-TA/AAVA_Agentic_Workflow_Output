package com.warehouse.ems.employee;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;

/**
 * Employee entity representing warehouse staff.
 */
@Entity
@Table(name = "employee")
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
    @Size(max = 50)
    @Column(name = "badge_id", unique = true, nullable = false)
    private String badgeId;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Role role;

    @Size(max = 100)
    private String department;

    @Size(max = 50)
    private String shiftGroup;

    private LocalDate hireDate;

    @Size(max = 30)
    private String status;

    private boolean active = true;
    private boolean deleted = false;

    /**
     * Role enumeration for employee roles.
     */
    public enum Role {
        ADMIN, HR, SUPERVISOR, WORKER
    }
}
