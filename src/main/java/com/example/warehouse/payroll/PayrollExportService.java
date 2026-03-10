package com.example.warehouse.payroll;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class PayrollExportService {
    @Autowired
    private PayrollRecordRepository payrollRecordRepository;

    public byte[] exportPayrollCsv() {
        List<PayrollRecord> records = payrollRecordRepository.findAll();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(out, true, StandardCharsets.UTF_8);
        writer.println("ID,EmployeeID,PeriodStart,PeriodEnd,GrossPay,NetPay,Deductions");
        for (PayrollRecord record : records) {
            writer.printf("%d,%d,%s,%s,%.2f,%.2f,%.2f
",
                    record.getId(),
                    record.getEmployeeId(),
                    record.getPeriodStart(),
                    record.getPeriodEnd(),
                    record.getGrossPay(),
                    record.getNetPay(),
                    record.getDeductions()
            );
        }
        writer.flush();
        return out.toByteArray();
    }
}
