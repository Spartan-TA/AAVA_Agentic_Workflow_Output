package com.wms.ems.training.service;

import com.wms.ems.training.repository.CertificationRepository;
import com.wms.ems.training.dto.CertificationDto;
import com.wms.ems.training.entity.Certification;
import com.wms.ems.employee.repository.EmployeeRepository;
import com.wms.ems.employee.entity.Employee;
import com.wms.ems.common.exception.ResourceNotFoundException;
import com.wms.ems.common.exception.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Service class for Certification management business logic and operations.
 */
@Slf4j
@Service
@Transactional
public class CertificationService {

    @Autowired
    private CertificationRepository certificationRepository;
    @Autowired
    private EmployeeRepository employeeRepository;

    /**
     * Adds a certification for an employee after validation.
     * @param dto CertificationDto
     * @return Certification
     */
    public Certification addCertification(CertificationDto dto) {
        Employee employee = employeeRepository.findByIdAndDeletedFalse(dto.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + dto.getEmployeeId()));
        Certification cert = new Certification(dto);
        cert.setEmployee(employee);
        return certificationRepository.save(cert);
    }

    /**
     * Gets all certifications for an employee.
     * @param employeeId Employee ID
     * @return List<Certification>
     */
    @Transactional(readOnly = true)
    public List<Certification> getEmployeeCertifications(Long employeeId) {
        return certificationRepository.findByEmployeeId(employeeId);
    }

    /**
     * Gets certifications expiring within a number of days.
     * @param daysAhead Number of days ahead
     * @return List<Certification>
     */
    @Transactional(readOnly = true)
    public List<Certification> getExpiringCertifications(int daysAhead) {
        LocalDate threshold = LocalDate.now().plusDays(daysAhead);
        return certificationRepository.findByExpiryDateBefore(threshold);
    }

    /**
     * Checks if an employee is certified for a given type.
     * @param employeeId Employee ID
     * @param certificationType Certification type
     * @return boolean
     */
    @Transactional(readOnly = true)
    public boolean isEmployeeCertified(Long employeeId, String certificationType) {
        return certificationRepository.existsByEmployeeIdAndTypeAndValid(employeeId, certificationType, true);
    }

    /**
     * Updates a certification.
     * @param certId Certification ID
     * @param dto CertificationDto
     */
    public void updateCertification(Long certId, CertificationDto dto) {
        Certification cert = certificationRepository.findById(certId)
                .orElseThrow(() -> new ResourceNotFoundException("Certification not found: " + certId));
        if (dto.getType() != null) cert.setType(dto.getType());
        if (dto.getIssueDate() != null) cert.setIssueDate(dto.getIssueDate());
        if (dto.getExpiryDate() != null) cert.setExpiryDate(dto.getExpiryDate());
        if (dto.getValid() != null) cert.setValid(dto.getValid());
        certificationRepository.save(cert);
    }

    /**
     * Deletes a certification by ID.
     * @param certId Certification ID
     */
    public void deleteCertification(Long certId) {
        Certification cert = certificationRepository.findById(certId)
                .orElseThrow(() -> new ResourceNotFoundException("Certification not found: " + certId));
        certificationRepository.delete(cert);
    }
}
