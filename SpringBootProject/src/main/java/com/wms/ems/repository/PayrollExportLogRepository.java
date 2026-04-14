package com.wms.ems.repository;

import com.wms.ems.entity.PayrollExportLog;
import com.wms.ems.enums.PayrollExportStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

/**
 * Repository interface for PayrollExportLog entity operations.
 * Provides CRUD and custom query methods for payroll export log management.
 */
public interface PayrollExportLogRepository extends JpaRepository<PayrollExportLog, Long> {
    /**
     * Find payroll export logs within a date range.
     * @param startDate start date
     * @param endDate end date
     * @return List of PayrollExportLogs
     */
    List<PayrollExportLog> findByExportDateBetween(LocalDate startDate, LocalDate endDate);

    /**
     * Find payroll export logs by status.
     * @param status the export status
     * @return List of PayrollExportLogs
     */
    List<PayrollExportLog> findByStatus(PayrollExportStatus status);
}
