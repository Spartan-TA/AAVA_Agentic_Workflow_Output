package com.wms.attendance;

import lombok.*;
import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Attendance record entity.
 */
@Entity
@Table(name = "attendance_record")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    private LocalDateTime clockIn;
    private LocalDateTime clockOut;
    private Long shiftId;
    private String deviceInfo;
    private String status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}