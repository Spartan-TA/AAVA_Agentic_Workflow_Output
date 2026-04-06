package com.example.warehouse.service;

import com.example.warehouse.dto.CertificationDTO;
import com.example.warehouse.entity.Certification;
import com.example.warehouse.entity.Employee;
import com.example.warehouse.exception.CertificationExpiredException;
import com.example.warehouse.exception.ResourceNotFoundException;
import com.example.warehouse.repository.CertificationRepository;
import com.example.warehouse.repository.EmployeeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class CertificationService {
    private final CertificationRepository certificationRepository;
    private final EmployeeRepository employeeRepository;

    public CertificationService(CertificationRepository certificationRepository, EmployeeRepository employeeRepository) {
        this.certificationRepository = certificationRepository;
        this.employeeRepository = employeeRepository;
    }

    public Certification createCertification(CertificationDTO dto) {
        Certification cert = new Certification();
        cert.setName(dto.getName());
        cert.setEmployee(employeeRepository.findById(dto.getEmployeeId()).orElseThrow(() -> new ResourceNotFoundException("Employee not found")));
        cert.setExpiryDate(dto.getExpiryDate());
        cert.setDocumentUrl(dto.getDocumentUrl());
        return certificationRepository.save(cert);
    }

    public Certification updateCertification(Long id, CertificationDTO dto) {
        Certification cert = certificationRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Certification not found"));
        cert.setName(dto.getName());
        cert.setExpiryDate(dto.getExpiryDate());
        cert.setDocumentUrl(dto.getDocumentUrl());
        return certificationRepository.save(cert);
    }

    public void deleteCertification(Long id) {
        certificationRepository.deleteById(id);
    }

    public List<Certification> getExpiringCertifications(int days) {
        LocalDate now = LocalDate.now();
        LocalDate threshold = now.plusDays(days);
        return certificationRepository.findByExpiryDateBetween(now, threshold);
    }

    public void validateCertification(Long employeeId, String certName) {
        Certification cert = certificationRepository.findTopByEmployeeIdAndNameOrderByExpiryDateDesc(employeeId, certName)
                .orElseThrow(() -> new CertificationExpiredException("Certification expired or not found"));
        if (cert.getExpiryDate().isBefore(LocalDate.now())) {
            throw new CertificationExpiredException("Certification expired");
        }
    }

    // Document upload logic would be handled by a separate storage service
}
