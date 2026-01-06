package com.example.warehouse.service;

import com.example.warehouse.dto.SafetyIncidentDTO;
import com.example.warehouse.entity.Employee;
import com.example.warehouse.entity.SafetyIncident;
import com.example.warehouse.exception.ResourceNotFoundException;
import com.example.warehouse.exception.ValidationException;
import com.example.warehouse.repository.EmployeeRepository;
import com.example.warehouse.repository.SafetyIncidentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing safety incidents.
 */
@Service
public class SafetyIncidentService {
    private final SafetyIncidentRepository safetyIncidentRepository;
    private final EmployeeRepository employeeRepository;

    @Autowired
    public SafetyIncidentService(SafetyIncidentRepository safetyIncidentRepository, EmployeeRepository employeeRepository) {
        this.safetyIncidentRepository = safetyIncidentRepository;
        this.employeeRepository = employeeRepository;
    }

    /**
     * Get all safety incidents for an employee.
     * @param employeeId Employee ID
     * @return List of SafetyIncidentDTO
     */
    @Transactional(readOnly = true)
    public List<SafetyIncidentDTO> getIncidentsByEmployee(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));
        return safetyIncidentRepository.findByEmployee(employee).stream()
                .map(SafetyIncidentDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Report a new safety incident.
     * @param employeeId Employee ID
     * @param dto SafetyIncidentDTO
     * @return SafetyIncidentDTO
     */
    @Transactional
    public SafetyIncidentDTO reportIncident(Long employeeId, SafetyIncidentDTO dto) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));
        if (dto.getDescription() == null || dto.getDescription().isEmpty()) {
            throw new ValidationException("Incident description is required");
        }
        SafetyIncident incident = new SafetyIncident();
        incident.setEmployee(employee);
        incident.setDescription(dto.getDescription());
        incident.setIncidentTime(dto.getIncidentTime() != null ? dto.getIncidentTime() : LocalDateTime.now());
        incident.setSeverity(dto.getSeverity());
        safetyIncidentRepository.save(incident);
        return SafetyIncidentDTO.fromEntity(incident);
    }

    /**
     * Get all safety incidents.
     * @return List of SafetyIncidentDTO
     */
    @Transactional(readOnly = true)
    public List<SafetyIncidentDTO> getAllIncidents() {
        return safetyIncidentRepository.findAll().stream()
                .map(SafetyIncidentDTO::fromEntity)
                .collect(Collectors.toList());
    }
}
