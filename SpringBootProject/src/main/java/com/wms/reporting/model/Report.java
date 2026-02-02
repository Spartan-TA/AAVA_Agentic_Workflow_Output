package com.wms.reporting.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Entity representing a generated report.
 */
@Data
@Entity
@Table(name = "reports")
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Type of report (e.g., ATTENDANCE, OVERTIME, LEAVE_BALANCE, CERTIFICATION_STATUS, SAFETY_KPI) */
    @Column(nullable = false)
    private String type;

    /** Timestamp when the report was generated */
    @Column(nullable = false)
    private LocalDateTime generatedAt;

    /** Report content (CSV, PDF, JSON, etc.) */
    @Lob
    @Column(nullable = false)
    private byte[] content;

    /** Format of the report (e.g., CSV, PDF, JSON) */
    @Column(nullable = false)
    private String format;
}
