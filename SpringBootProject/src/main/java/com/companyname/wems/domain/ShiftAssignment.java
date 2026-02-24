package com.companyname.wems.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import jakarta.validation.constraints.*;

/**
 * ShiftAssignment entity for assigning shifts to employees.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "shift_assignments")
public class ShiftAssignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(nullable = false)
    private Long employeeId;

    @NotNull
    @Column(nullable = false)
    private Long shiftTemplateId;

    @NotNull
    private LocalDate date;

    @NotBlank
    @Size(max = 20)
    private String status; // ASSIGNED, COMPLETED, MISSED

    @NotNull
    @Column(nullable = false)
    private Long tenantId;
}
