package com.wems.certification.service;

import com.wems.certification.domain.*;
import com.wems.employee.domain.Employee;
import com.wems.common.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class CertificationService {
    @Autowired
    private CertificationRepository certificationRepository;
    @Autowired
    private EmployeeCertificationRepository employeeCertificationRepository;

    public Certification addCertification(String name, String description) {
        Certification cert = new Certification();
        cert.setName(name);
        cert.setDescription(description);
        cert.setActive(true);
        return certificationRepository.save(cert);
    }

    public boolean isEmployeeCertified(Employee employee, Long certificationId) {
        return employeeCertificationRepository.findAll().stream()
                .anyMatch(ec -> ec.getEmployee().equals(employee) &&
                        ec.getCertification().getId().equals(certificationId) &&
                        ec.getStatus() == CertificationStatus.ACTIVE);
    }

    public void processExpiryAlerts() {
        // Placeholder for expiry alert logic
    }

    @Transactional
    public EmployeeCertification renewCertification(Long employeeCertificationId, LocalDate newExpiryDate) {
        EmployeeCertification ec = employeeCertificationRepository.findById(employeeCertificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee certification not found"));
        ec.setExpiryDate(newExpiryDate);
        ec.setStatus(CertificationStatus.ACTIVE);
        return employeeCertificationRepository.save(ec);
    }
}
