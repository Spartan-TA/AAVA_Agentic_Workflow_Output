package com.warehouse.employee.management.service;

import com.warehouse.employee.management.entity.SafetyIncident;
import com.warehouse.employee.management.repository.SafetyIncidentRepository;
import com.warehouse.employee.management.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * Service class for managing SafetyIncident entities.
 */
@Service
public class SafetyIncidentService {
    private final SafetyIncidentRepository safetyIncidentRepository;

    @Autowired
    public SafetyIncidentService(SafetyIncidentRepository safetyIncidentRepository) {
        this.safetyIncidentRepository = safetyIncidentRepository;
    }

    /**
     * Get all safety incidents.
     * @return List of safety incidents
     */
    public List<SafetyIncident> getAllSafetyIncidents() {
        return safetyIncidentRepository.findAll();
    }

    /**
     * Get safety incident by ID.
     * @param id SafetyIncident ID
     * @return SafetyIncident entity
     */
    public SafetyIncident getSafetyIncidentById(Long id) {
        return safetyIncidentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SafetyIncident not found with id: " + id));
    }

    /**
     * Record a new safety incident.
     * @param safetyIncident SafetyIncident entity
     * @return Recorded safety incident
     */
    @Transactional
    public SafetyIncident recordSafetyIncident(SafetyIncident safetyIncident) {
        return safetyIncidentRepository.save(safetyIncident);
    }

    /**
     * Update an existing safety incident.
     * @param id SafetyIncident ID
     * @param updatedIncident Updated safety incident entity
     * @return Updated safety incident
     */
    @Transactional
    public SafetyIncident updateSafetyIncident(Long id, SafetyIncident updatedIncident) {
        SafetyIncident existingIncident = getSafetyIncidentById(id);
        existingIncident.setDescription(updatedIncident.getDescription());
        existingIncident.setIncidentDate(updatedIncident.getIncidentDate());
        existingIncident.setStatus(updatedIncident.getStatus());
        existingIncident.setInvestigationDetails(updatedIncident.getInvestigationDetails());
        // Add other fields as needed
        return safetyIncidentRepository.save(existingIncident);
    }

    /**
     * Delete a safety incident by ID.
     * @param id SafetyIncident ID
     */
    @Transactional
    public void deleteSafetyIncident(Long id) {
        SafetyIncident incident = getSafetyIncidentById(id);
        safetyIncidentRepository.delete(incident);
    }

    /**
     * Investigate a safety incident.
     * @param id SafetyIncident ID
     * @param investigationDetails Investigation details
     * @return Updated safety incident
     */
    @Transactional
    public SafetyIncident investigateIncident(Long id, String investigationDetails) {
        SafetyIncident incident = getSafetyIncidentById(id);
        incident.setInvestigationDetails(investigationDetails);
        incident.setStatus("INVESTIGATED");
        return safetyIncidentRepository.save(incident);
    }
}
