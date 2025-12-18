package com.warehouse.ems.attendance;

import com.warehouse.ems.employee.Employee;
import com.warehouse.ems.scheduling.Shift;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "attendance")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Attendance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shift_id")
    private Shift shift;

    @Column(name = "clock_in")
    private LocalDateTime clockIn;

    @Column(name = "clock_out")
    private LocalDateTime clockOut;

    @Column(name = "device_info")
    private String deviceInfo;

    @Column(name = "geofence_location")
    private String geofenceLocation;

    @Column(name = "status")
    private String status;

    @Column(name = "correction_requested")
    private Boolean correctionRequested = false;
}
