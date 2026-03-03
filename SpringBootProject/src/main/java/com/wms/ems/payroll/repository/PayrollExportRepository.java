package com.wms.ems.payroll.repository;

import com.wms.ems.payroll.entity.PayrollExport;
import com.wms.ems.payroll.entity.ExportStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

/**
 * Repository interface for PayrollExport entity operations.
 * Provides CRUD operations and custom queries for payroll export management.
 */
public interface PayrollExportRepository extends JpaRepository<PayrollExport, Long> {

    /**
     * Finds payroll exports by status.
     * @param status the export status
     * @return a list of payroll exports
     */
    List<PayrollExport> findByStatus(ExportStatus status);

    /**
     * Finds payroll exports exported between two dates.
     * @param start the start date
     * @param end the end date
     * @return a list of payroll exports
     */
    List<PayrollExport> findByExportDateBetween(LocalDate start, LocalDate end);
}
