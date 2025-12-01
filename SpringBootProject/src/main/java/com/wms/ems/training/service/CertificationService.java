package com.wms.ems.training.service;

import com.wms.ems.training.entity.Certification;
import com.wms.ems.training.repository.CertificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;

/**
 * Service class for Certification management.
 * Handles CRUD and expiry alerts.
 */
@Service
@Transactional
public class CertificationService {
    private final CertificationRepository certificationRepository;

    @Autowired
    public CertificationService(CertificationRepository certificationRepository) {
        this.certificationRepository = certificationRepository;
    }

    /**
     * Get all certifications for an employee.
     * @param employeeId the employee's ID
     * @return List of Certification
     */
    public List<Certification> getCertificationsForEmployee(Long employeeId) {
        return certificationRepository.findByEmployeeId(employeeId);
    }

    /**
     * Create a new certification.
     * @param certification the certification to create
     * @return the saved Certification
     */
    public Certification createCertification(Certification certification) {
        return certificationRepository.save(certification);
    }

    /**
     * Update an existing certification.
     * @param id the certification ID
     * @param updated the updated certification data
     * @return the updated Certification
     */
    public Certification updateCertification(Long id, Certification updated) {
        Certification existing = certificationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Certification not found"));
        existing.setName(updated.getName());
        existing.setExpiryDate(updated.getExpiryDate());
        // ... update other fields
        return certificationRepository.save(existing);
    }

    /**
     * Delete a certification.
     * @param id the certification ID
     */
    public void deleteCertification(Long id) {
        certificationRepository.deleteById(id);
    }

    /**
     * Get certifications expiring before a given date (for alerts).
     * @param expiryDate the expiry date
     * @return List of Certification
     */
    public List<Certification> getExpiringCertifications(LocalDate expiryDate) {
        return certificationRepository.findByExpiryDateBefore(expiryDate);
    }
}
