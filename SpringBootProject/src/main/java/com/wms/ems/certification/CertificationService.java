package com.wms.ems.certification;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class CertificationService {

    @Autowired
    private CertificationRepository certificationRepository;

    // CRUD
    public Certification createCertification(Certification cert) {
        return certificationRepository.save(cert);
    }

    public List<Certification> getCertifications(Long employeeId) {
        return certificationRepository.findByEmployeeId(employeeId);
    }

    public Certification updateCertification(Long id, Certification updated) {
        Certification cert = certificationRepository.findById(id).orElseThrow();
        cert.setType(updated.getType());
        cert.setIssueDate(updated.getIssueDate());
        cert.setExpiryDate(updated.getExpiryDate());
        cert.setProofDocumentUrl(updated.getProofDocumentUrl());
        return certificationRepository.save(cert);
    }

    public void deleteCertification(Long id) {
        certificationRepository.deleteById(id);
    }

    // Expiry alerts (30/7 days)
    public List<Certification> getExpiringCertifications(int days) {
        LocalDate now = LocalDate.now();
        LocalDate target = now.plusDays(days);
        return certificationRepository.findByExpiryDateBetween(now, target);
    }

    // Assignment validation
    public boolean isCertificationValid(Long employeeId, String type) {
        List<Certification> certs = certificationRepository.findByEmployeeId(employeeId);
        return certs.stream().anyMatch(c -> c.getType().equals(type) && c.getExpiryDate().isAfter(LocalDate.now()));
    }
}
