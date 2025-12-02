package com.wms.ems.schedule;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "shift_assignments")
public class ShiftAssignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long employeeId;

    @Column(nullable = false)
    private Long shiftTemplateId;

    @Column(nullable = false)
    private LocalDate date;

    // Getters and setters omitted for brevity
}
