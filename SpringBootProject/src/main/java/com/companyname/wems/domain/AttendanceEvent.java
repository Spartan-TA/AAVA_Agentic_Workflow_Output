package com.companyname.wems.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import jakarta.validation.constraints.*;

/**
 * AttendanceEvent entity for tracking employee clock-in/out events.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "attendance_events")
public class AttendanceEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(nullable = false)
    private Long employeeId;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime timestamp;

    @NotBlank
    @Column(nullable = false)
    private String type; // IN or OUT

    @Size(max = 50)
    private String deviceId;

    @Size(max = 100)
    private String location;

    @NotBlank
    @Size(max = 20)
    private String status;

    @NotNull
    @Column(nullable = false)
    private Long tenantId;
}
