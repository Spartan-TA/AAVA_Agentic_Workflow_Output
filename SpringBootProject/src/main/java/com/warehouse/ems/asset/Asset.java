package com.warehouse.ems.asset;

import com.warehouse.ems.employee.Employee;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.time.LocalDate;

/**
 * Entity representing an asset assigned to an employee.
 */
@Entity
@Table(name = "assets")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Asset {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "type", nullable = false)
    private String type;

    @NotBlank
    @Column(name = "serial_number", nullable = false)
    private String serialNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to")
    private Employee assignedTo;

    @NotBlank
    @Column(name = "condition", nullable = false)
    private String condition;

    @Column(name = "checkout_date")
    private LocalDate checkoutDate;

    @Column(name = "return_date")
    private LocalDate returnDate;
}
