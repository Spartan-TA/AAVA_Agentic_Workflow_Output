package com.company.wems.attendance;

import com.company.wems.employee.Employee;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@RequiredArgsConstructor
@Entity
@Table(name = "attendance_events")
public class AttendanceEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttendanceType type;

    private String device;
    private String location;
    private LocalDateTime clockOutTime;
    private Double totalHours;

    @Enumerated(EnumType.STRING)
    private ApprovalStatus approvalStatus;

    public enum AttendanceType {
        CLOCK_IN, CLOCK_OUT
    }

    public enum ApprovalStatus {
        PENDING, APPROVED, REJECTED
    }
}
