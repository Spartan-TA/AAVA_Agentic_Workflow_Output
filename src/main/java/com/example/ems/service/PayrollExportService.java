package com.example.ems.service;

import com.example.ems.entity.PayrollExport;
import com.example.ems.repository.PayrollExportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PayrollExportService {

    @Autowired
    private PayrollExportRepository payrollExportRepository;

    public List<PayrollExport> getAllExports() {
        return payrollExportRepository.findAll();
    }

    public Optional<PayrollExport> getExportById(Long id) {
        return payrollExportRepository.findById(id);
    }

    public PayrollExport createExport(PayrollExport export) {
        return payrollExportRepository.save(export);
    }

    public PayrollExport updateExport(Long id, PayrollExport updatedExport) {
        return payrollExportRepository.findById(id)
                .map(existing -> {
                    existing.setExportTime(updatedExport.getExportTime());
                    existing.setStatus(updatedExport.getStatus());
                    existing.setProvider(updatedExport.getProvider());
                    existing.setFileName(updatedExport.getFileName());
                    existing.setDeliveryMethod(updatedExport.getDeliveryMethod());
                    existing.setErrorMessage(updatedExport.getErrorMessage());
                    existing.setExportFile(updatedExport.getExportFile());
                    return payrollExportRepository.save(existing);
                })
                .orElseThrow(() -> new RuntimeException("PayrollExport not found"));
    }

    public void deleteExport(Long id) {
        payrollExportRepository.deleteById(id);
    }
}
