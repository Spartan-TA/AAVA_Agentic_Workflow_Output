package com.companyname.wems.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalTime;
import jakarta.validation.constraints.*;

/**
 * ShiftTemplate entity for defining recurring shift patterns.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "shift_templates")
public class ShiftTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 50)
    private String name;

    @NotNull
    private LocalTime startTime;

    @NotNull
    private LocalTime endTime;

    @NotBlank
    @Size(max = 100)
    private String recurrenceRule; // e.g., "FREQ=WEEKLY;BYDAY=MO,TU,WE,TH,FR"

    @NotNull
    @Column(nullable = false)
    private Long tenantId;
}
