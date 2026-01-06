package com.example.warehouse.service;

import com.example.warehouse.dto.CertificationDTO;
import com.example.warehouse.entity.Certification;
import com.example.warehouse.entity.Employee;
import com.example.warehouse.exception.ResourceNotFoundException;
import com.example.warehouse.exception.ValidationException;
import com.example.warehouse.repository.CertificationRepository;
import com.example.warehouse.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing certifications.
 */
@Service
public class CertificationService {
    private final CertificationRepository certificationRepository;
    private final EmployeeRepository employeeRepository;

    @Autowired
    public CertificationService(CertificationRepository certificationRepository, EmployeeRepository employeeRepository) {
        this.certificationRepository = certificationRepository;
        this.employeeRepository = employeeRepository;
    }

    /**
     * Get all certifications for an employee.
     * @param employeeId Employee ID
     * @return List of CertificationDTO
     */
    @Transactional(readOnly = true)
    public List<CertificationDTO> getCertificationsByEmployee(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));
        return certificationRepository.findByEmployee(employee).stream()
                .map(CertificationDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Add a certification to an employee.
     * @param employeeId Employee ID
     * @param dto CertificationDTO
     * @return CertificationDTO
     */
    @Transactional
    public CertificationDTO addCertification(Long employeeId, CertificationDTO dto) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));
        if (dto.getName() == null || dto.getName().isEmpty()) {
            throw new ValidationException("Certification name is required");
        }
        if (dto.getIssueDate() == null) {
            throw new ValidationException("Certification issue date is required");
        }
        Certification cert = new Certification();
        cert.setEmployee(employee);
        cert.setName(dto.getName());
        cert.setIssueDate(dto.getIssueDate());
        cert.setExpiryDate(dto.getExpiryDate());
        certificationRepository.save(cert);
        return CertificationDTO.fromEntity(cert);
    }

    /**
     * Get all certifications.
     * @return List of CertificationDTO
     */
    @Transactional(readOnly = true)
    public List<CertificationDTO> getAllCertifications() {
        return certificationRepository.findAll().stream()
                .map(CertificationDTO::fromEntity)
                .collect(Collectors.toList());
    }
}
