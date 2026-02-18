package com.companyname.wem.attendance.domain;

import com.companyname.wem.employee.domain.Employee;
import jakarta.persistence.*;
lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "attendance_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventType type;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "device_id")
    private String deviceId;

    private Double latitude;

    private Double longitude;

    @Column(nullable = false)
    private boolean correction = false;
}
