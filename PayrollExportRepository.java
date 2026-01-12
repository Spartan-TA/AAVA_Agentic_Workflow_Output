package com.warehouseems.payroll;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PayrollExportRepository extends JpaRepository<PayrollExport, Long> {
}
