package com.warehouse.employee.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Employee entity representing warehouse employees.
 */
@Entity
@Table(name = "employees", indexes = {
        @Index(name = "idx_employee_badge_id", columnList = "badgeId"),
        @Index(name = "idx_employee_status", columnList = "status"),
        @Index(name = "idx_employee_department", columnList = "department"),
        @Index(name = "idx_employee_role", columnList = "role")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 50)
    @Column(unique = true, nullable = false)
    private String badgeId;

    @NotBlank
    @Size(max = 100)
    private String firstName;

    @NotBlank
    @Size(max = 100)
    private String lastName;

    @NotBlank
    @Email
    @Size(max = 150)
    private String email;

    @NotBlank
    @Size(max = 20)
    private String phoneNumber;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @NotBlank
    @Size(max = 100)
    private String department;

    @Size(max = 100)
    private String shiftGroup;

    @NotNull
    private LocalDate hireDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Size(max = 100)
    private String createdBy;

    @Size(max = 100)
    private String updatedBy;

    /**
     * Attendance records for the employee.
     */
    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Attendance> attendances;

    /**
     * Schedules assigned to the employee.
     */
    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Schedule> schedules;

    /**
     * Leave requests for the employee.
     */
    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Leave> leaves;

    /**
     * Certifications held by the employee.
     */
    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Certification> certifications;

    /**
     * Performance reviews for the employee.
     */
    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<PerformanceReview> performanceReviews;

    /**
     * Employee roles.
     */
    public enum Role {
        ADMIN, HR, SUPERVISOR, WORKER
    }

    /**
     * Employee status.
     */
    public enum Status {
        ACTIVE, INACTIVE, TERMINATED
    }
}
