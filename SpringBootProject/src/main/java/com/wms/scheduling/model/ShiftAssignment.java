package com.wms.scheduling.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

/**
 * Entity representing the assignment of a shift to an employee on a specific date
 */
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

    /**
     * Employee ID to whom the shift is assigned
     */
    @Column(nullable = false)
    private Long employeeId;

    /**
     * The shift template assigned
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shift_template_id", nullable = false)
    private ShiftTemplate shiftTemplate;

    /**
     * Date of the shift assignment
     */
    @Column(nullable = false)
    private LocalDate shiftDate;

    /**
     * Is this assignment active?
     */
    @Column(nullable = false)
    private boolean active;
}
