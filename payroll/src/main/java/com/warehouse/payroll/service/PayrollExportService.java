package com.warehouse.payroll.service;

import com.warehouse.payroll.entity.PayrollExport;
import com.warehouse.payroll.repository.PayrollExportRepository;
import com.jcraft.jsch.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
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

    @Transactional
    public PayrollExport createExport(String format, String filePath) {
        PayrollExport export = PayrollExport.builder()
                .exportDate(LocalDateTime.now())
                .format(format)
                .filePath(filePath)
                .status("CREATED")
                .build();
        return payrollExportRepository.save(export);
    }

    public String mapFormat(String format) {
        switch (format.toUpperCase()) {
            case "CSV": return "text/csv";
            case "PDF": return "application/pdf";
            default: return "application/octet-stream";
        }
    }

    public boolean deliverViaSFTP(String filePath, String sftpHost, String sftpUser, String sftpPassword, String remoteDir) {
        try {
            JSch jsch = new JSch();
            Session session = jsch.getSession(sftpUser, sftpHost, 22);
            session.setPassword(sftpPassword);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();
            Channel channel = session.openChannel("sftp");
            channel.connect();
            ChannelSftp sftp = (ChannelSftp) channel;
            sftp.put(filePath, remoteDir + "/" + new java.io.File(filePath).getName());
            sftp.exit();
            session.disconnect();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Transactional
    public PayrollExport reconcileExport(Long id, String status) {
        PayrollExport export = payrollExportRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Export not found"));
        export.setReconciliationStatus(status);
        return payrollExportRepository.save(export);
    }
}
