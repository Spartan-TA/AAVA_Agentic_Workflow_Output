package com.wms.ems.safety.service;

import com.wms.ems.safety.entity.SafetyIncident;
import com.wms.ems.safety.repository.SafetyIncidentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * Service class for Safety management.
 * Handles incident recording and workflow.
 */
@Service
@Transactional
public class SafetyService {
    private final SafetyIncidentRepository safetyIncidentRepository;

    @Autowired
    public SafetyService(SafetyIncidentRepository safetyIncidentRepository) {
        this.safetyIncidentRepository = safetyIncidentRepository;
    }

    /**
     * Record a new safety incident.
     * @param incident the safety incident
     * @return the saved SafetyIncident
     */
    public SafetyIncident recordIncident(SafetyIncident incident) {
        incident.setStatus("REPORTED");
        // ... set other fields as needed
        return safetyIncidentRepository.save(incident);
    }

    /**
     * Update the status of a safety incident (workflow).
     * @param incidentId the incident ID
     * @param status the new status
     * @return the updated SafetyIncident
     */
    public SafetyIncident updateIncidentStatus(Long incidentId, String status) {
        SafetyIncident incident = safetyIncidentRepository.findById(incidentId)
                .orElseThrow(() -> new IllegalArgumentException("Safety incident not found"));
        incident.setStatus(status);
        return safetyIncidentRepository.save(incident);
    }

    /**
     * Get all incidents reported by an employee.
     * @param reporterId the reporter's employee ID
     * @return List of SafetyIncident
     */
    public List<SafetyIncident> getIncidentsByReporter(Long reporterId) {
        return safetyIncidentRepository.findByReporterId(reporterId);
    }

    /**
     * Get all incidents by status.
     * @param status the status
     * @return List of SafetyIncident
     */
    public List<SafetyIncident> getIncidentsByStatus(String status) {
        return safetyIncidentRepository.findByStatus(status);
    }
}
