package com.company.wms.safety.service;

import com.company.wms.safety.model.SafetyIncident;
import com.company.wms.safety.repository.SafetyIncidentRepository;
import com.company.wms.employee.model.Employee;
import com.company.wms.employee.repository.EmployeeRepository;
import com.company.wms.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for safety incident business logic.
 */
@Service
@RequiredArgsConstructor
public class SafetyService {
    private final SafetyIncidentRepository safetyIncidentRepository;
    private final EmployeeRepository employeeRepository;

    public List<SafetyIncident> getAllIncidents() {
        return safetyIncidentRepository.findAll();
    }

    public SafetyIncident getIncidentById(Long id) {
        return safetyIncidentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Safety incident not found with id: " + id));
    }

    public List<SafetyIncident> getIncidentsByReporter(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));
        return safetyIncidentRepository.findByReportedBy(employee);
    }

    public SafetyIncident createIncident(SafetyIncident incident) {
        return safetyIncidentRepository.save(incident);
    }

    @Transactional
    public SafetyIncident updateIncident(Long id, SafetyIncident updatedIncident) {
        SafetyIncident incident = getIncidentById(id);
        incident.setType(updatedIncident.getType());
        incident.setDescription(updatedIncident.getDescription());
        incident.setOccurredAt(updatedIncident.getOccurredAt());
        incident.setReportedBy(updatedIncident.getReportedBy());
        return safetyIncidentRepository.save(incident);
    }

    public void deleteIncident(Long id) {
        safetyIncidentRepository.deleteById(id);
    }
}
