package com.warehouse.ems.domain;

import com.warehouse.ems.enums.EventType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Entity representing an attendance event (clock-in/clock-out).
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

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @NotNull
    @Enumerated(EnumType.STRING)
    private EventType eventType;

    @NotNull
    private LocalDateTime eventTime;

    private String deviceId;
    private String location;
}
