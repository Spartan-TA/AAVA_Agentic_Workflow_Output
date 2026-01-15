package com.warehouse.training.service;

import com.warehouse.training.entity.Certification;
import com.warehouse.training.repository.CertificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class CertificationService {
    @Autowired
    private CertificationRepository certificationRepository;

    public List<Certification> getAllCertifications() {
        return certificationRepository.findAll();
    }

    public List<Certification> getCertificationsByEmployee(Long employeeId) {
        return certificationRepository.findByEmployeeId(employeeId);
    }

    public Optional<Certification> getCertificationById(Long id) {
        return certificationRepository.findById(id);
    }

    @Transactional
    public Certification createCertification(Certification certification) {
        validateCertification(certification);
        certification.setStatus(Certification.Status.ACTIVE);
        return certificationRepository.save(certification);
    }

    @Transactional
    public Certification updateCertification(Long id, Certification updated) {
        Certification cert = certificationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Certification not found"));
        cert.setType(updated.getType());
        cert.setIssueDate(updated.getIssueDate());
        cert.setExpiryDate(updated.getExpiryDate());
        cert.setStatus(updated.getStatus());
        cert.setEmployeeId(updated.getEmployeeId());
        validateCertification(cert);
        return certificationRepository.save(cert);
    }

    @Transactional
    public void deleteCertification(Long id) {
        certificationRepository.deleteById(id);
    }

    public List<Certification> getExpiringCertifications(LocalDate beforeDate) {
        return certificationRepository.findByExpiryDateBefore(beforeDate);
    }

    public void validateCertification(Certification certification) {
        if (certification.getExpiryDate().isBefore(certification.getIssueDate())) {
            throw new IllegalArgumentException("Expiry date cannot be before issue date");
        }
    }

    public List<Certification> getCertificationsByStatus(Certification.Status status) {
        return certificationRepository.findByStatus(status);
    }
}
