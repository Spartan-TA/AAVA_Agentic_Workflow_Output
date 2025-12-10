package com.warehouse.ems.service.impl;

import com.warehouse.ems.entity.Certification;
import com.warehouse.ems.repository.CertificationRepository;
import com.warehouse.ems.dto.CertificationDto;
import com.warehouse.ems.exception.ResourceNotFoundException;
import com.warehouse.ems.service.CertificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CertificationServiceImpl implements CertificationService {
    @Autowired
    private CertificationRepository certificationRepository;

    // Helper method to map Certification to CertificationDto
    private CertificationDto mapToDto(Certification certification) {
        CertificationDto dto = new CertificationDto();
        dto.setId(certification.getId());
        dto.setEmployeeId(certification.getEmployee().getId());
        dto.setName(certification.getName());
        dto.setExpiryDate(certification.getExpiryDate());
        // Add other fields as needed
        return dto;
    }

    @Override
    public CertificationDto getCertificationById(Long id) {
        Certification certification = certificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Certification not found with id: " + id));
        return mapToDto(certification);
    }

    @Override
    public List<CertificationDto> getAllCertifications() {
        return certificationRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public CertificationDto createCertification(CertificationDto certificationDto) {
        Certification certification = new Certification();
        // Assume Employee is set elsewhere or via service
        certification.setName(certificationDto.getName());
        certification.setExpiryDate(certificationDto.getExpiryDate());
        // Set other fields as needed
        Certification saved = certificationRepository.save(certification);
        return mapToDto(saved);
    }

    @Override
    public List<CertificationDto> getExpiringCertifications() {
        LocalDate soon = LocalDate.now().plusMonths(1);
        return certificationRepository.findAll().stream()
                .filter(cert -> cert.getExpiryDate() != null && cert.getExpiryDate().isBefore(soon))
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteCertification(Long id) {
        if (!certificationRepository.existsById(id)) {
            throw new ResourceNotFoundException("Certification not found with id: " + id);
        }
        certificationRepository.deleteById(id);
    }
}
