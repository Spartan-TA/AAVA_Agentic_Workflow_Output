package com.companyname.wems.scheduling.model;

import lombok.*;
import javax.persistence.*;
import javax.validation.constraints.*;
import java.time.LocalDate;

@Entity
@Table(name = "shift_assignments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
    @Column(nullable = false)
    private LocalDate assignmentDate;

    @NotBlank
    @Column(nullable = false)
    private String status; // e.g., ASSIGNED, CONFLICT, CANCELLED

    /**
     * Conflict detection logic should be implemented in the service layer.
     * This entity only stores assignment data.
     */
}
