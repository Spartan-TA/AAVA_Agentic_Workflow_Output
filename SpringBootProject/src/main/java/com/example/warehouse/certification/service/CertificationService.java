package com.example.warehouse.certification.service;

import com.example.warehouse.certification.entity.Certification;
import com.example.warehouse.certification.repository.CertificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CertificationService {
    @Autowired
    private CertificationRepository certificationRepository;

    // Get all certifications
    public List<Certification> getAllCertifications() {
        return certificationRepository.findAll();
    }

    // Get certifications by employee
    public List<Certification> getCertificationsByEmployee(Long employeeId) {
        return certificationRepository.findByEmployeeId(employeeId);
    }

    // Get certification by ID
    public Optional<Certification> getCertificationById(Long id) {
        return certificationRepository.findById(id);
    }

    // Create new certification
    @Transactional
    public Certification createCertification(Certification certification) {
        return certificationRepository.save(certification);
    }

    // Update certification
    @Transactional
    public Optional<Certification> updateCertification(Long id, Certification certification) {
        return certificationRepository.findById(id).map(existing -> {
            existing.setName(certification.getName());
            existing.setIssueDate(certification.getIssueDate());
            existing.setExpiryDate(certification.getExpiryDate());
            existing.setIssuer(certification.getIssuer());
            return certificationRepository.save(existing);
        });
    }

    // Delete certification
    @Transactional
    public boolean deleteCertification(Long id) {
        if (certificationRepository.existsById(id)) {
            certificationRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
