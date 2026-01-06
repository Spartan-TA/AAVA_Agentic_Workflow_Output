package com.company.wms.scheduling.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

/**
 * Entity representing an assignment of a shift to an employee.
 */
@Entity
@Table(name = "shift_assignments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShiftAssignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long employeeId;

    @Column(nullable = false)
    private Long shiftTemplateId;

    @Column(nullable = false)
    private LocalDate shiftDate;

    @Column(nullable = false)
    private Boolean confirmed;
}
