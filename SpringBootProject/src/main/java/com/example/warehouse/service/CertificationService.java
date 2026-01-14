package com.example.warehouse.service;

import com.example.warehouse.entity.Certification;
import com.example.warehouse.repository.CertificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Service for Certification operations.
 */
@Service
public class CertificationService {
    @Autowired
    private CertificationRepository certificationRepository;

    public List<Certification> getExpiringCertifications(LocalDate from, LocalDate to) {
        return certificationRepository.findExpiringBetween(from, to);
    }

    public List<Certification> getCertificationsForEmployee(Long employeeId) {
        return certificationRepository.findByEmployee(employeeId);
    }

    @Transactional
    public Certification addCertification(Certification certification) {
        return certificationRepository.save(certification);
    }

    @Transactional
    public Certification updateCertification(Long id, Certification updated) {
        Certification cert = certificationRepository.findById(id).orElseThrow();
        cert.setName(updated.getName());
        cert.setExpiryDate(updated.getExpiryDate());
        cert.setEmployee(updated.getEmployee());
        return certificationRepository.save(cert);
    }
}
