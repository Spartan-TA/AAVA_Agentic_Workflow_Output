package com.warehouseems.payroll;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "payroll_exports")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PayrollExport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate periodStart;

    @Column(nullable = false)
    private LocalDate periodEnd;

    @Column(nullable = false)
    private String filePath;

    private LocalDateTime deliveredAt;
}
