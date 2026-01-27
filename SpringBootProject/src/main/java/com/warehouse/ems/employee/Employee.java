package com.warehouse.ems.employee;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.Set;

/**
 * Employee entity representing warehouse employees.
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

    @Column(name = "badge_id", nullable = false, unique = true)
    private String badgeId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String role;

    private String department;
    private String shiftGroup;
    private LocalDate hireDate;

    @Column(nullable = false)
    private String status;

    @Builder.Default
    private boolean deleted = false;

    // Relationships (examples, more can be added as needed)
    // @OneToMany(mappedBy = "employee")
    // private Set<AttendanceEvent> attendanceEvents;

    // Soft delete logic can be handled in repository/service
}
