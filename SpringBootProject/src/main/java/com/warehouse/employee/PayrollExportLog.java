package com.warehouse.employee;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * PayrollExportLog entity for payroll export tracking.
 */
@Entity
@Table(name = "payroll_export_log")
public class PayrollExportLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "export_date", nullable = false)
    private LocalDateTime exportDate;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "file_url")
    private String fileUrl;

    @Column(name = "error_message")
    private String errorMessage;

    // Audit fields
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;

    // Getters and setters omitted for brevity
}
