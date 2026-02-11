package com.warehouse.employee.service;

import com.warehouse.employee.domain.Certification;
import com.warehouse.employee.domain.Employee;
import com.warehouse.employee.dto.CertificationDto;
import com.warehouse.employee.exception.EmployeeNotFoundException;
import com.warehouse.employee.mapper.CertificationMapper;
import com.warehouse.employee.repository.CertificationRepository;
import com.warehouse.employee.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for certification tracking and expiration alerts.
 */
@Service
public class CertificationService {

    private final CertificationRepository certificationRepository;
    private final EmployeeRepository employeeRepository;
    private final CertificationMapper certificationMapper;

    @Autowired
    public CertificationService(CertificationRepository certificationRepository,
                               EmployeeRepository employeeRepository,
                               CertificationMapper certificationMapper) {
        this.certificationRepository = certificationRepository;
        this.employeeRepository = employeeRepository;
        this.certificationMapper = certificationMapper;
    }

    /**
     * Add a certification for an employee.
     * @param dto CertificationDto
     * @return CertificationDto
     */
    @Transactional
    public CertificationDto addCertification(CertificationDto dto) {
        Employee employee = employeeRepository.findById(dto.getEmployeeId())
                .filter(e -> !e.isDeleted())
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found: " + dto.getEmployeeId()));
        Certification certification = certificationMapper.toEntity(dto);
        certification.setEmployee(employee);
        Certification saved = certificationRepository.save(certification);
        return certificationMapper.toDto(saved);
    }

    /**
     * Check for certifications expiring soon for an employee.
     * @param employeeId Employee ID
     * @param days int days until expiry
     * @return List of CertificationDto
     */
    @Transactional(readOnly = true)
    public List<CertificationDto> checkExpiringSoon(Long employeeId, int days) {
        Employee employee = employeeRepository.findById(employeeId)
                .filter(e -> !e.isDeleted())
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found: " + employeeId));
        LocalDate threshold = LocalDate.now().plusDays(days);
        List<Certification> expiring = certificationRepository.findByEmployeeAndExpiryDateBefore(employee, threshold);
        return expiring.stream().map(certificationMapper::toDto).collect(Collectors.toList());
    }
}
