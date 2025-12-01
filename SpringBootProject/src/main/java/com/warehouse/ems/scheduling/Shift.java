package com.warehouse.ems.scheduling;

import lombok.*;
import javax.persistence.*;
import javax.validation.constraints.*;
import java.time.LocalTime;
import java.util.Set;

/**
 * Shift entity for scheduling.
 */
@Entity
@Table(name = "shifts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Shift {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "name", nullable = false)
    private String name;

    @NotNull
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @NotNull
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "shift_days", joinColumns = @JoinColumn(name = "shift_id"))
    @Column(name = "day_of_week")
    private Set<String> daysOfWeek; // e.g., MONDAY, TUESDAY
}
