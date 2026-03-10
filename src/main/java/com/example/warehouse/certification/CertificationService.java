package com.example.warehouse.certification;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CertificationService {
    @Autowired
    private CertificationRepository certificationRepository;

    public List<Certification> getAllCertifications() {
        return certificationRepository.findAll();
    }

    public Optional<Certification> getCertificationById(Long id) {
        return certificationRepository.findById(id);
    }

    public List<Certification> getCertificationsByEmployee(Long employeeId) {
        return certificationRepository.findByEmployeeId(employeeId);
    }

    public Certification createCertification(CertificationDto dto) {
        Certification cert = new Certification();
        cert.setEmployeeId(dto.getEmployeeId());
        cert.setName(dto.getName());
        cert.setIssueDate(dto.getIssueDate());
        cert.setExpiryDate(dto.getExpiryDate());
        cert.setAuthority(dto.getAuthority());
        return certificationRepository.save(cert);
    }

    public void deleteCertification(Long id) {
        certificationRepository.deleteById(id);
    }
}
