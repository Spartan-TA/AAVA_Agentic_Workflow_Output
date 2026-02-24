package com.companyname.wems.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import jakarta.validation.constraints.*;

/**
 * AttendanceCorrection entity for managing corrections to attendance events.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "attendance_corrections")
public class AttendanceCorrection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(nullable = false)
    private Long attendanceEventId;

    @NotNull
    @Column(nullable = false)
    private Long employeeId;

    @NotBlank
    @Size(max = 100)
    private String reason;

    @NotBlank
    @Size(max = 20)
    private String status; // PENDING, APPROVED, REJECTED

    @NotNull
    @Column(nullable = false)
    private LocalDateTime requestedAt;

    private LocalDateTime reviewedAt;

    private Long reviewedBy;

    @NotNull
    @Column(nullable = false)
    private Long tenantId;
}
