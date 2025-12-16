package com.companyname.wems.scheduling.model;

import lombok.*;
import javax.persistence.*;
import javax.validation.constraints.*;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Set;

@Entity
@Table(name = "shift_templates")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShiftTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @NotNull
    @Column(nullable = false)
    private LocalTime startTime;

    @NotNull
    @Column(nullable = false)
    private LocalTime endTime;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "shift_template_days", joinColumns = @JoinColumn(name = "shift_template_id"))
    @Column(name = "day_of_week")
    private Set<String> daysOfWeek; // e.g., MONDAY, TUESDAY

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "shift_template_blackout_dates", joinColumns = @JoinColumn(name = "shift_template_id"))
    @Column(name = "blackout_date")
    private Set<LocalDateTime> blackoutDates;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
