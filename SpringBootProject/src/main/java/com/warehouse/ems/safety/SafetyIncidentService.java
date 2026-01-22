package com.warehouse.ems.safety;

import com.warehouse.ems.employee.Employee;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing safety incidents, workflow, and OSHA reporting.
 */
@Service
public class SafetyIncidentService {
    private final SafetyIncidentRepository incidentRepository;

    @Autowired
    public SafetyIncidentService(SafetyIncidentRepository incidentRepository) {
        this.incidentRepository = incidentRepository;
    }

    /**
     * Record a new safety incident.
     */
    @Transactional
    public SafetyIncident recordIncident(SafetyIncident incident) {
        // Additional validation can be added here
        return incidentRepository.save(incident);
    }

    /**
     * Get all safety incidents.
     */
    public List<SafetyIncident> getAllIncidents() {
        return incidentRepository.findAll();
    }

    /**
     * Update the status of an incident (workflow state transitions).
     */
    @Transactional
    public SafetyIncident updateIncidentStatus(Long id, SafetyIncident.Status status) {
        SafetyIncident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Incident not found with id: " + id));
        incident.setStatus(status);
        return incidentRepository.save(incident);
    }

    /**
     * Generate OSHA 300/300A report (stub - implement CSV/PDF logic as needed).
     * @param start Start date
     * @param end End date
     * @return List of incidents in the period
     */
    public List<SafetyIncident> generateOshaReport(LocalDate start, LocalDate end) {
        // For demo, filter in-memory; in production, use a custom query
        return incidentRepository.findAll().stream()
                .filter(i -> !i.getIncidentDate().toLocalDate().isBefore(start) && !i.getIncidentDate().toLocalDate().isAfter(end))
                .toList();
    }
}
