package com.warehouse.employee.management.service;

import com.warehouse.employee.management.entity.Certification;
import com.warehouse.employee.management.repository.CertificationRepository;
import com.warehouse.employee.management.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for managing Certification entities.
 */
@Service
public class CertificationService {
    private final CertificationRepository certificationRepository;

    @Autowired
    public CertificationService(CertificationRepository certificationRepository) {
        this.certificationRepository = certificationRepository;
    }

    /**
     * Get all certifications.
     * @return List of certifications
     */
    public List<Certification> getAllCertifications() {
        return certificationRepository.findAll();
    }

    /**
     * Get certification by ID.
     * @param id Certification ID
     * @return Certification entity
     */
    public Certification getCertificationById(Long id) {
        return certificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Certification not found with id: " + id));
    }

    /**
     * Create a new certification.
     * @param certification Certification entity
     * @return Created certification
     */
    @Transactional
    public Certification createCertification(Certification certification) {
        return certificationRepository.save(certification);
    }

    /**
     * Update an existing certification.
     * @param id Certification ID
     * @param updatedCertification Updated certification entity
     * @return Updated certification
     */
    @Transactional
    public Certification updateCertification(Long id, Certification updatedCertification) {
        Certification existingCertification = getCertificationById(id);
        existingCertification.setName(updatedCertification.getName());
        existingCertification.setIssueDate(updatedCertification.getIssueDate());
        existingCertification.setExpiryDate(updatedCertification.getExpiryDate());
        existingCertification.setEmployee(updatedCertification.getEmployee());
        // Add other fields as needed
        return certificationRepository.save(existingCertification);
    }

    /**
     * Delete a certification by ID.
     * @param id Certification ID
     */
    @Transactional
    public void deleteCertification(Long id) {
        Certification certification = getCertificationById(id);
        certificationRepository.delete(certification);
    }

    /**
     * Get certifications expiring within given days.
     * @param days Number of days
     * @return List of expiring certifications
     */
    public List<Certification> getExpiringCertifications(int days) {
        LocalDate threshold = LocalDate.now().plusDays(days);
        return certificationRepository.findAll().stream()
                .filter(cert -> cert.getExpiryDate() != null && cert.getExpiryDate().isBefore(threshold))
                .collect(Collectors.toList());
    }
}
