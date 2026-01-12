package com.warehouseems.shift;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "shift_assignments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShiftAssignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "employee_id")
    private com.warehouseems.employee.Employee employee;

    @ManyToOne
    @JoinColumn(name = "template_id")
    private ShiftTemplate template;

    @Column(nullable = false)
    private LocalDate date;
}
