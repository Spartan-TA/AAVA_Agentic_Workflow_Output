package com.example.warehouse.payroll;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payroll")
public class PayrollController {
    @Autowired
    private PayrollRecordRepository payrollRecordRepository;
    @Autowired
    private PayrollExportService payrollExportService;

    @GetMapping("/records")
    public List<PayrollRecord> getAllPayrollRecords() {
        return payrollRecordRepository.findAll();
    }

    @PostMapping("/records")
    public PayrollRecord createPayrollRecord(@RequestBody PayrollRecord record) {
        return payrollRecordRepository.save(record);
    }

    @DeleteMapping("/records/{id}")
    public ResponseEntity<Void> deletePayrollRecord(@PathVariable Long id) {
        payrollRecordRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportPayrollCsv() {
        byte[] csv = payrollExportService.exportPayrollCsv();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=payroll.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }
}
