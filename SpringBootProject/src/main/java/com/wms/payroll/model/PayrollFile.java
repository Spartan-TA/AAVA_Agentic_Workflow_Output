package com.wms.payroll.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Entity representing a payroll export file.
 */
@Data
@Entity
@Table(name = "payroll_files")
public class PayrollFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Name of the payroll file (e.g., payroll_2024-06-01.csv) */
    @Column(nullable = false)
    private String fileName;

    /** Status of the payroll file (e.g., GENERATED, DELIVERED, FAILED) */
    @Column(nullable = false)
    private String status;

    /** Timestamp when the file was delivered */
    private LocalDateTime deliveredAt;

    /** Number of delivery retries attempted */
    @Column(nullable = false)
    private int retryCount = 0;

    /** File content as a BLOB (CSV, XML, etc.) */
    @Lob
    @Column(nullable = false)
    private byte[] content;
}
