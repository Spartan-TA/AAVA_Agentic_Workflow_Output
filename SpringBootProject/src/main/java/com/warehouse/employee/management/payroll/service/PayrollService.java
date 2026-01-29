package com.warehouse.employee.management.payroll.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
public class PayrollService {
    private final List<String> payrollExports = new ArrayList<>();

    @Transactional
    public String generatePayrollExport(String period) {
        String export = "PayrollExport_" + period + ".csv";
        payrollExports.add(export);
        return export;
    }

    public boolean reconcilePayroll(String exportName) {
        // Dummy reconciliation logic
        return payrollExports.contains(exportName);
    }

    public void deliverViaSftp(String exportName) {
        // Stub for SFTP delivery logic
    }

    public List<String> getAllExports() {
        return Collections.unmodifiableList(payrollExports);
    }
}
