package com.wms.ems.payroll.model;

import com.wms.ems.common.BaseEntity;
import com.wms.ems.common.ExportStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;

/**
 * Entity representing a payroll export record.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "payroll_exports")
public class PayrollExport extends BaseEntity {

    /**
     * Date of the export.
     */
    @Column(nullable = false)
    private LocalDate exportDate;

    /**
     * URL to the exported file.
     */
    private String fileUrl;

    /**
     * Status of the export.
     */
    @Enumerated(EnumType.STRING)
    private ExportStatus status;
}