package com.warehouse.ems.employee;

import lombok.*;
import javax.persistence.*;
import javax.validation.constraints.*;
import java.time.LocalDate;
import java.util.Set;

/**
 * Employee entity representing warehouse staff.
 */
@Entity
@Table(name = "employees", uniqueConstraints = {@UniqueConstraint(columnNames = "badge_id")})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "name", nullable = false)
    private String name;

    @NotBlank
    @Column(name = "badge_id", nullable = false, unique = true)
    private String badgeId;

    @NotBlank
    @Column(name = "role", nullable = false)
    private String role; // ADMIN, HR, SUPERVISOR, WORKER

    @NotBlank
    @Column(name = "department", nullable = false)
    private String department;

    @Column(name = "shift_group")
    private String shiftGroup;

    @PastOrPresent
    @Column(name = "hire_date")
    private LocalDate hireDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    public enum Status {
        ACTIVE, INACTIVE
    }

    // Soft-delete flag
    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;

    // Relationships (example)
    // @OneToMany(mappedBy = "employee")
    // private Set<AttendanceEvent> attendanceEvents;
}
