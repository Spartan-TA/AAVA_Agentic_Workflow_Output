package com.warehouse.ems.attendance;

import com.warehouse.ems.employee.Employee;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Entity representing a clock-in/out attendance event.
 */
@Entity
@Table(name = "attendance_event")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "employee_id")
    @NotNull
    private Employee employee;

    @NotNull
    private LocalDateTime timestamp;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Type type;

    @Column(length = 100)
    private String deviceId;

    @Column(length = 100)
    private String location;

    /**
     * Type of attendance event: IN or OUT.
     */
    public enum Type {
        IN, OUT
    }
}
