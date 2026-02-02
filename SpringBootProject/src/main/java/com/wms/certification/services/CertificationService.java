package com.wms.certification.services;

import com.wms.certification.dtos.CertificationDto;
import com.wms.certification.dtos.EmployeeCertificationDto;
import com.wms.certification.model.Certification;
import com.wms.certification.model.EmployeeCertification;
import com.wms.certification.repositories.CertificationRepository;
import com.wms.certification.repositories.EmployeeCertificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for managing certifications and employee certifications
 */
@Service
@RequiredArgsConstructor
public class CertificationService {
    private final CertificationRepository certificationRepository;
    private final EmployeeCertificationRepository employeeCertificationRepository;

    /**
     * Create a new certification type
     */
    public CertificationDto createCertification(CertificationDto dto) {
        Certification cert = Certification.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .active(dto.isActive())
                .build();
        Certification saved = certificationRepository.save(cert);
        dto.setId(saved.getId());
        return dto;
    }

    /**
     * Get all certifications
     */
    public List<CertificationDto> getAllCertifications() {
        return certificationRepository.findAll().stream()
                .map(c -> CertificationDto.builder()
                        .id(c.getId())
                        .name(c.getName())
                        .description(c.getDescription())
                        .active(c.isActive())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Assign a certification to an employee
     */
    @Transactional
    public EmployeeCertificationDto assignCertification(EmployeeCertificationDto dto) {
        Optional<Certification> certOpt = certificationRepository.findById(dto.getCertificationId());
        if (certOpt.isEmpty()) {
            throw new IllegalArgumentException("Certification not found");
        }
        EmployeeCertification empCert = EmployeeCertification.builder()
                .employeeId(dto.getEmployeeId())
                .certification(certOpt.get())
                .obtainedDate(dto.getObtainedDate())
                .expiryDate(dto.getExpiryDate())
                .active(dto.isActive())
                .build();
        EmployeeCertification saved = employeeCertificationRepository.save(empCert);
        dto.setId(saved.getId());
        return dto;
    }

    /**
     * Get all certifications for an employee
     */
    public List<EmployeeCertificationDto> getCertificationsForEmployee(Long employeeId) {
        return employeeCertificationRepository.findByEmployeeId(employeeId).stream()
                .map(ec -> EmployeeCertificationDto.builder()
                        .id(ec.getId())
                        .employeeId(ec.getEmployeeId())
                        .certificationId(ec.getCertification().getId())
                        .obtainedDate(ec.getObtainedDate())
                        .expiryDate(ec.getExpiryDate())
                        .active(ec.isActive())
                        .build())
                .collect(Collectors.toList());
    }
}
