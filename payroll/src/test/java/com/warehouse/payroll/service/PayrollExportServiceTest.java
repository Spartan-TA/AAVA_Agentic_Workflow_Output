package com.warehouse.payroll.service;

import com.warehouse.payroll.entity.PayrollExport;
import com.warehouse.payroll.repository.PayrollExportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.time.LocalDateTime;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PayrollExportServiceTest {
    @Mock
    private PayrollExportRepository payrollExportRepository;

    @InjectMocks
    private PayrollExportService payrollExportService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateExport() {
        PayrollExport export = PayrollExport.builder()
                .exportDate(LocalDateTime.now())
                .format("CSV")
                .filePath("/tmp/export.csv")
                .status("CREATED")
                .build();
        when(payrollExportRepository.save(any(PayrollExport.class))).thenReturn(export);
        PayrollExport result = payrollExportService.createExport("CSV", "/tmp/export.csv");
        assertEquals("CSV", result.getFormat());
        assertEquals("CREATED", result.getStatus());
    }

    @Test
    void testReconcileExport() {
        PayrollExport export = PayrollExport.builder().id(1L).status("CREATED").build();
        when(payrollExportRepository.findById(1L)).thenReturn(Optional.of(export));
        when(payrollExportRepository.save(any(PayrollExport.class))).thenReturn(export);
        PayrollExport result = payrollExportService.reconcileExport(1L, "SUCCESS");
        assertEquals("SUCCESS", result.getReconciliationStatus());
    }
}
