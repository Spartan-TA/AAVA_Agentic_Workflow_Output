package com.warehouse.ems.certification;

import com.warehouse.ems.employee.Employee;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.List;

/**
 * Service for managing certifications, including CRUD, expiry alerts, and assignment validation.
 */
@Service
public class CertificationService {
    private final CertificationRepository certificationRepository;

    @Autowired
    public CertificationService(CertificationRepository certificationRepository) {
        this.certificationRepository = certificationRepository;
    }

    /**
     * Create a new certification.
     */
    @Transactional
    public Certification createCertification(Certification certification) {
        // Additional validation logic can be added here
        return certificationRepository.save(certification);
    }

    /**
     * Get all certifications.
     */
    public List<Certification> getAllCertifications() {
        return certificationRepository.findAll();
    }

    /**
     * Get a certification by ID.
     */
    public Certification getCertification(Long id) {
        return certificationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Certification not found with id: " + id));
    }

    /**
     * Update a certification.
     */
    @Transactional
    public Certification updateCertification(Long id, Certification updated) {
        Certification cert = getCertification(id);
        cert.setEmployee(updated.getEmployee());
        cert.setType(updated.getType());
        cert.setExpiryDate(updated.getExpiryDate());
        cert.setDocumentUrl(updated.getDocumentUrl());
        cert.setStatus(updated.getStatus());
        return certificationRepository.save(cert);
    }

    /**
     * Delete a certification.
     */
    @Transactional
    public void deleteCertification(Long id) {
        if (!certificationRepository.existsById(id)) {
            throw new EntityNotFoundException("Certification not found with id: " + id);
        }
        certificationRepository.deleteById(id);
    }

    /**
     * Find certifications expiring in the next 30 or 7 days.
     */
    public List<Certification> getExpiringCertifications(int days) {
        LocalDate cutoff = LocalDate.now().plusDays(days);
        return certificationRepository.findExpiringCertifications(cutoff);
    }

    /**
     * Validate if an employee has a valid certification of a given type.
     * Throws exception if not valid.
     */
    public void validateCertification(Employee employee, String type) {
        List<Certification> certs = certificationRepository.findAll();
        boolean valid = certs.stream().anyMatch(c ->
                c.getEmployee().getId().equals(employee.getId()) &&
                c.getType().equals(type) &&
                c.getStatus() == Certification.Status.ACTIVE &&
                c.getExpiryDate().isAfter(LocalDate.now())
        );
        if (!valid) {
            throw new IllegalStateException("Employee does not have a valid certification of type: " + type);
        }
    }
}
