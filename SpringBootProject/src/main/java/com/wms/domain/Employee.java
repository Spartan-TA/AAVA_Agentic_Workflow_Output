package com.wms.domain;

import jakarta.persistence.*;
import java.util.Set;

@Entity
public class Employee extends BaseEntity {
    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String role;

    @OneToMany(mappedBy = "employee")
    private Set<AttendanceEvent> attendanceEvents;

    @OneToMany(mappedBy = "employee")
    private Set<EmployeeShift> shifts;

    @OneToMany(mappedBy = "employee")
    private Set<LeaveRequest> leaveRequests;

    @OneToMany(mappedBy = "employee")
    private Set<Certification> certifications;

    @OneToMany(mappedBy = "employee")
    private Set<SafetyIncident> safetyIncidents;

    @OneToMany(mappedBy = "employee")
    private Set<PerformanceReview> performanceReviews;

    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
