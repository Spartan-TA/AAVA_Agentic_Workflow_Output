package com.wms.asset;

import javax.persistence.*;
import java.time.LocalDate;

import com.wms.employee.Employee;

/**
 * Entity representing an asset (equipment, PPE) assigned to employees.
 */
@Entity
@Table(name = "assets")
public class Asset {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String assetTag;

    @Column(nullable = false)
    private String assetType; // SCANNER, FORKLIFT, PPE

    private String condition; // GOOD, NEEDS_REPAIR, RETIRED

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_employee_id")
    private Employee assignedEmployee;

    private LocalDate checkoutDate;
    private LocalDate returnDate;

    // Getters and setters omitted for brevity
    // ...
}
