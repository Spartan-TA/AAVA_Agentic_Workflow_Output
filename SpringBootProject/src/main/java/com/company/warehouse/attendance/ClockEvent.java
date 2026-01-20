package com.company.warehouse.attendance;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Entity for clock-in/out events.
 */
@Entity
@Table(name = "clock_events")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClockEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private Long employeeId;

    @NotNull
    private LocalDateTime timestamp;

    @NotNull
    @Enumerated(EnumType.STRING)
    private ClockEventType type;

    private Double latitude;
    private Double longitude;
    private String deviceId;

    public enum ClockEventType {
        CLOCK_IN, CLOCK_OUT
    }
}
