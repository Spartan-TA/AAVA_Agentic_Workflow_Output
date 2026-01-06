package com.example.warehouse.service;

import com.example.warehouse.entity.Employee;
import com.example.warehouse.entity.PayrollExport;
import com.example.warehouse.exception.ResourceNotFoundException;
import com.example.warehouse.repository.EmployeeRepository;
import com.example.warehouse.repository.PayrollExportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Service for managing payroll exports.
 */
@Service
public class PayrollService {
    private final PayrollExportRepository payrollExportRepository;
    private final EmployeeRepository employeeRepository;

    @Autowired
    public PayrollService(PayrollExportRepository payrollExportRepository, EmployeeRepository employeeRepository) {
        this.payrollExportRepository = payrollExportRepository;
        this.employeeRepository = employeeRepository;
    }

    /**
     * Export payroll for a given period.
     * @param startDate Start date
     * @param endDate End date
     * @return PayrollExport
     */
    @Transactional
    public PayrollExport exportPayroll(LocalDate startDate, LocalDate endDate) {
        PayrollExport export = new PayrollExport();
        export.setStartDate(startDate);
        export.setEndDate(endDate);
        export.setExportedAt(LocalDate.now());
        // In a real system, add logic to calculate payroll data and attach to export
        payrollExportRepository.save(export);
        return export;
    }

    /**
     * Get all payroll exports.
     * @return List of PayrollExport
     */
    @Transactional(readOnly = true)
    public List<PayrollExport> getAllPayrollExports() {
        return payrollExportRepository.findAll();
    }

    /**
     * Get payroll export by ID.
     * @param id PayrollExport ID
     * @return PayrollExport
     */
    @Transactional(readOnly = true)
    public PayrollExport getPayrollExportById(Long id) {
        return payrollExportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payroll export not found with id: " + id));
    }
}
