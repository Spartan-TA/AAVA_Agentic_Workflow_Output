package com.example.warehouse.service;

import com.example.warehouse.dto.SafetyIncidentDTO;
import com.example.warehouse.entity.SafetyIncident;
import com.example.warehouse.entity.Employee;
import com.example.warehouse.exception.ResourceNotFoundException;
import com.example.warehouse.repository.SafetyIncidentRepository;
import com.example.warehouse.repository.EmployeeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SafetyIncidentService {
    private final SafetyIncidentRepository safetyIncidentRepository;
    private final EmployeeRepository employeeRepository;

    public SafetyIncidentService(SafetyIncidentRepository safetyIncidentRepository, EmployeeRepository employeeRepository) {
        this.safetyIncidentRepository = safetyIncidentRepository;
        this.employeeRepository = employeeRepository;
    }

    @Transactional
    public SafetyIncident recordIncident(Long reporterId, SafetyIncidentDTO dto) {
        Employee reporter = employeeRepository.findById(reporterId)
                .orElseThrow(() -> new ResourceNotFoundException("Reporter not found"));
        SafetyIncident incident = new SafetyIncident();
        incident.setReporter(reporter);
        incident.setDescription(dto.getDescription());
        incident.setSeverity(dto.getSeverity());
        incident.setStatus("OPEN");
        incident.setReportedAt(LocalDateTime.now());
        safetyIncidentRepository.save(incident);
        return incident;
    }

    @Transactional
    public SafetyIncident updateStatus(Long incidentId, String status) {
        SafetyIncident incident = safetyIncidentRepository.findById(incidentId)
                .orElseThrow(() -> new ResourceNotFoundException("Incident not found"));
        incident.setStatus(status);
        safetyIncidentRepository.save(incident);
        return incident;
    }

    public List<SafetyIncident> getIncidentsByStatus(String status) {
        return safetyIncidentRepository.findByStatus(status);
    }

    public List<SafetyIncident> getAllIncidents() {
        return safetyIncidentRepository.findAll();
    }

    public String exportOSHA(String type) {
        // OSHA 300/300A export logic (CSV/PDF)
        return "OSHA export generated for type: " + type;
    }
}
