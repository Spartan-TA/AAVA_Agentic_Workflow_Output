package com.warehouse.employee.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.time.LocalTime;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Shift entity representing shift templates.
 */
@Entity
@Table(name = "shifts", indexes = {
        @Index(name = "idx_shift_department", columnList = "department"),
        @Index(name = "idx_shift_is_recurring", columnList = "isRecurring")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Shift {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 100)
    private String name;

    @NotNull
    private LocalTime startTime;

    @NotNull
    private LocalTime endTime;

    @Min(0)
    @Max(480)
    private Integer breakDuration; // in minutes

    @ElementCollection(targetClass = DayOfWeek.class)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "shift_days_of_week", joinColumns = @JoinColumn(name = "shift_id"))
    @Column(name = "day_of_week")
    private List<DayOfWeek> daysOfWeek;

    @NotNull
    private Boolean isRecurring;

    @NotBlank
    @Size(max = 100)
    private String department;

    @Min(1)
    private Integer maxCapacity;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    /**
     * Schedules for this shift.
     */
    @OneToMany(mappedBy = "shift", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Schedule> schedules;
}
