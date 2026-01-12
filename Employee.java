package com.warehouseems.employee;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "employees")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String badgeId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String role;

    private String department;
    private String shiftGroup;

    @Column(nullable = false)
    private LocalDate hireDate;

    @Column(nullable = false)
    private String status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Relationships
    @OneToMany(mappedBy = "employee")
    private List<AttendanceEvent> attendanceEvents;

    @OneToMany(mappedBy = "employee")
    private List<ShiftAssignment> shiftAssignments;

    @OneToMany(mappedBy = "employee")
    private List<LeaveRequest> leaveRequests;

    @OneToMany(mappedBy = "employee")
    private List<Certification> certifications;

    @OneToMany(mappedBy = "employee")
    private List<PerformanceReview> performanceReviews;

    @OneToMany(mappedBy = "assignedTo")
    private List<Asset> assets;

    @OneToMany(mappedBy = "recipient")
    private List<Notification> notifications;
}
