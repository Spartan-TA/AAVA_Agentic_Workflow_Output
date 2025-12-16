package com.companyname.wems.certification.service;

import com.companyname.wems.certification.model.EmployeeCertification;
import com.companyname.wems.certification.repository.CertificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CertificationService {
    private final CertificationRepository certificationRepository;

    // Add or update certification
    public EmployeeCertification addOrUpdateCertification(EmployeeCertification cert) {
        cert.setStatus(determineStatus(cert.getExpiryDate()));
        return certificationRepository.save(cert);
    }

    // Check expiry status
    public String determineStatus(LocalDate expiryDate) {
        LocalDate today = LocalDate.now();
        if (expiryDate.isBefore(today)) {
            return "EXPIRED";
        } else if (expiryDate.isBefore(today.plusDays(7))) {
            return "EXPIRING_SOON";
        } else if (expiryDate.isBefore(today.plusDays(30))) {
            return "EXPIRING_SOON";
        } else {
            return "ACTIVE";
        }
    }

    // Generate alerts for expiring certifications (within 30/7 days)
    public List<EmployeeCertification> getExpiringCertifications() {
        LocalDate today = LocalDate.now();
        LocalDate in30Days = today.plusDays(30);
        return certificationRepository.findByExpiryDateBetween(today, in30Days);
    }

    // Validate certification for task assignment
    public boolean isCertificationValid(Long employeeId, String certificationName) {
        List<EmployeeCertification> certs = certificationRepository.findByEmployeeId(employeeId);
        return certs.stream().anyMatch(c -> c.getCertificationName().equals(certificationName) && c.getStatus().equals("ACTIVE"));
    }

    // Get certifications for employee
    public List<EmployeeCertification> getEmployeeCertifications(Long employeeId) {
        return certificationRepository.findByEmployeeId(employeeId);
    }
}