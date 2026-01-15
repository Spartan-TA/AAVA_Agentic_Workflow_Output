package com.warehouse.payroll.controller;

import com.warehouse.payroll.entity.PayrollExport;
import com.warehouse.payroll.service.PayrollExportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/payroll/exports")
public class PayrollExportController {
    @Autowired
    private PayrollExportService payrollExportService;

    @GetMapping
    public ResponseEntity<List<PayrollExport>> getAllExports() {
        return ResponseEntity.ok(payrollExportService.getAllExports());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PayrollExport> getExportById(@PathVariable Long id) {
        return payrollExportService.getExportById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<PayrollExport> createExport(@RequestParam String format, @RequestParam String filePath) {
        PayrollExport export = payrollExportService.createExport(format, filePath);
        return ResponseEntity.ok(export);
    }

    @PostMapping("/{id}/reconcile")
    public ResponseEntity<PayrollExport> reconcileExport(@PathVariable Long id, @RequestParam String status) {
        PayrollExport export = payrollExportService.reconcileExport(id, status);
        return ResponseEntity.ok(export);
    }

    @PostMapping("/sftp-deliver")
    public ResponseEntity<Boolean> deliverViaSFTP(@RequestParam String filePath,
                                                  @RequestParam String sftpHost,
                                                  @RequestParam String sftpUser,
                                                  @RequestParam String sftpPassword,
                                                  @RequestParam String remoteDir) {
        boolean result = payrollExportService.deliverViaSFTP(filePath, sftpHost, sftpUser, sftpPassword, remoteDir);
        return ResponseEntity.ok(result);
    }
}
