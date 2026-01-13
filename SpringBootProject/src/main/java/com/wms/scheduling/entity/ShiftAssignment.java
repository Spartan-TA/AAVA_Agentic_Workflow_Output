package com.wms.scheduling.entity;

import com.wms.employee.entity.Employee;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

/**
 * Entity representing a shift assignment for an employee.
 */
@Entity
@Table(name = "shift_assignment")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShiftAssignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shift_template_id", nullable = false)
    private ShiftTemplate shiftTemplate;

    @Column(nullable = false)
    private LocalDate date;
}
