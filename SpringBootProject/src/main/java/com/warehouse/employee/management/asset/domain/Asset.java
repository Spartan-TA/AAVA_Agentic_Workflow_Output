package com.warehouse.employee.management.asset.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import com.warehouse.employee.management.employee.domain.Employee;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Asset {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "assigned_to")
    private Employee assignedTo;

    private LocalDate assignedDate;
    private String status;
}
