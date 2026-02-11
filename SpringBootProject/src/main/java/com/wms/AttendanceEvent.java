package com.wms;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Entity representing an attendance event.
 */
@Entity
@Table(name = "attendance_events")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long employeeId;

    @Column(nullable = false)
    private LocalDateTime eventTime;

    @Column(nullable = false, length = 20)
    private String eventType; // e.g. CLOCK_IN, CLOCK_OUT
}
