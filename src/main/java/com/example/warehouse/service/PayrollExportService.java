package com.example.warehouse.service;

import com.example.warehouse.dto.PayrollExportDTO;
import com.example.warehouse.entity.PayrollExport;
import com.example.warehouse.repository.PayrollExportRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PayrollExportService {
    private final PayrollExportRepository payrollExportRepository;

    public PayrollExportService(PayrollExportRepository payrollExportRepository) {
        this.payrollExportRepository = payrollExportRepository;
    }

    @Transactional
    public PayrollExport exportPayroll(PayrollExportDTO dto) {
        PayrollExport export = new PayrollExport();
        export.setRequestedAt(LocalDateTime.now());
        export.setStatus("PENDING");
        export.setProvider(dto.getProvider());
        // File generation, format mapping, SFTP/API delivery, retry logic, audit logging
        payrollExportRepository.save(export);
        return export;
    }

    public List<PayrollExport> getExports() {
        return payrollExportRepository.findAll();
    }

    public PayrollExport getExportStatus(Long exportId) {
        return payrollExportRepository.findById(exportId).orElse(null);
    }
}
