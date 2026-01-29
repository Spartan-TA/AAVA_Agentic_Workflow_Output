package com.warehouse.employee.management.certification.service;

import com.warehouse.employee.management.dto.CertificationDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.*;

@Service
public class CertificationService {
    private final Map<Long, List<CertificationDto>> employeeCerts = new HashMap<>();

    @Transactional
    public CertificationDto addCertification(CertificationDto cert) {
        employeeCerts.computeIfAbsent(cert.getEmployeeId(), k -> new ArrayList<>()).add(cert);
        return cert;
    }

    @Transactional
    public boolean removeCertification(Long employeeId, int certIndex) {
        List<CertificationDto> certs = employeeCerts.getOrDefault(employeeId, new ArrayList<>());
        if (certIndex < 0 || certIndex >= certs.size()) return false;
        certs.remove(certIndex);
        return true;
    }

    public List<CertificationDto> getCertifications(Long employeeId) {
        return employeeCerts.getOrDefault(employeeId, Collections.emptyList());
    }

    public boolean isCertificationExpired(CertificationDto cert) {
        return cert.getExpiryDate().isBefore(LocalDate.now());
    }

    public boolean canAssignToAsset(Long employeeId, String requiredCert) {
        List<CertificationDto> certs = employeeCerts.getOrDefault(employeeId, Collections.emptyList());
        return certs.stream().anyMatch(c -> c.getName().equals(requiredCert) && !isCertificationExpired(c));
    }
}
