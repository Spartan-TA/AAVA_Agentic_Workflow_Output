package com.wms.ems.safety.service;

import com.wms.ems.safety.repository.SafetyIncidentRepository;
import com.wms.ems.employee.repository.EmployeeRepository;
import com.wms.ems.safety.entity.SafetyIncident;
import com.wms.ems.safety.dto.SafetyIncidentDto;
import com.wms.ems.safety.enums.IncidentStatus;
import com.wms.ems.employee.entity.Employee;
import com.wms.ems.common.exception.ResourceNotFoundException;
import com.wms.ems.common.exception.ValidationException;
import com.wms.ems.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing safety incidents.
 */
@Service
@Transactional
@Slf4j
public class SafetyService {

    @Autowired
    private SafetyIncidentRepository safetyIncidentRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    /**
     * Reports a new safety incident with OPEN status.
     * @param dto SafetyIncidentDto
     * @return SafetyIncident
     */
    public SafetyIncident reportIncident(SafetyIncidentDto dto) {
        if (dto == null || dto.getDescription() == null || dto.getDescription().isEmpty()) {
            log.error("Validation failed: Incident description is required");
            throw new ValidationException("Incident description is required");
        }
        SafetyIncident incident = new SafetyIncident();
        incident.setDescription(dto.getDescription());
        incident.setReportedAt(LocalDateTime.now());
        incident.setStatus(IncidentStatus.OPEN);
        incident.setLocation(dto.getLocation());
        incident.setSeverity(dto.getSeverity());
        try {
            SafetyIncident saved = safetyIncidentRepository.save(incident);
            log.info("Safety incident reported: {}", saved.getId());
            return saved;
        } catch (Exception e) {
            log.error("Failed to report incident", e);
            throw new BusinessException("Failed to report incident");
        }
    }

    /**
     * Updates the status of an existing incident.
     * @param incidentId Incident ID
     * @param newStatus New status
     * @return SafetyIncident
     */
    public SafetyIncident updateIncidentStatus(Long incidentId, IncidentStatus newStatus) {
        SafetyIncident incident = safetyIncidentRepository.findById(incidentId)
                .orElseThrow(() -> new ResourceNotFoundException("Incident not found"));
        incident.setStatus(newStatus);
        try {
            SafetyIncident updated = safetyIncidentRepository.save(incident);
            log.info("Incident {} status updated to {}", incidentId, newStatus);
            return updated;
        } catch (Exception e) {
            log.error("Failed to update incident status", e);
            throw new BusinessException("Failed to update incident status");
        }
    }

    /**
     * Gets incidents by status.
     * @param status IncidentStatus
     * @return List of SafetyIncident
     */
    @Transactional(readOnly = true)
    public List<SafetyIncident> getIncidentsByStatus(IncidentStatus status) {
        try {
            return safetyIncidentRepository.findByStatus(status);
        } catch (Exception e) {
            log.error("Failed to fetch incidents by status", e);
            throw new BusinessException("Failed to fetch incidents by status");
        }
    }

    /**
     * Gets incidents within a date range.
     * @param start Start date
     * @param end End date
     * @return List of SafetyIncident
     */
    @Transactional(readOnly = true)
    public List<SafetyIncident> getIncidentsByDateRange(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null || end.isBefore(start)) {
            log.error("Invalid date range");
            throw new ValidationException("Invalid date range");
        }
        try {
            return safetyIncidentRepository.findByReportedAtBetween(start, end);
        } catch (Exception e) {
            log.error("Failed to fetch incidents by date range", e);
            throw new BusinessException("Failed to fetch incidents by date range");
        }
    }

    /**
     * Adds an involved employee to an incident.
     * @param incidentId Incident ID
     * @param employeeId Employee ID
     * @return SafetyIncident
     */
    public SafetyIncident addInvolvedEmployee(Long incidentId, Long employeeId) {
        SafetyIncident incident = safetyIncidentRepository.findById(incidentId)
                .orElseThrow(() -> new ResourceNotFoundException("Incident not found"));
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
        if (incident.getInvolvedEmployees() != null && incident.getInvolvedEmployees().contains(employee)) {
            log.error("Employee already involved in incident");
            throw new ValidationException("Employee already involved in incident");
        }
        incident.getInvolvedEmployees().add(employee);
        try {
            SafetyIncident updated = safetyIncidentRepository.save(incident);
            log.info("Employee {} added to incident {}", employeeId, incidentId);
            return updated;
        } catch (Exception e) {
            log.error("Failed to add employee to incident", e);
            throw new BusinessException("Failed to add employee to incident");
        }
    }
}
