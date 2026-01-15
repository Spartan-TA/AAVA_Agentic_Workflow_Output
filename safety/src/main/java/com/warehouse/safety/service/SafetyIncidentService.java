package com.warehouse.safety.service;

import com.warehouse.safety.entity.SafetyIncident;
import com.warehouse.safety.repository.SafetyIncidentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class SafetyIncidentService {
    @Autowired
    private SafetyIncidentRepository safetyIncidentRepository;

    public List<SafetyIncident> getAllIncidents() {
        return safetyIncidentRepository.findAll();
    }

    public Optional<SafetyIncident> getIncidentById(Long id) {
        return safetyIncidentRepository.findById(id);
    }

    public List<SafetyIncident> getIncidentsBySeverity(SafetyIncident.Severity severity) {
        return safetyIncidentRepository.findBySeverity(severity);
    }

    public List<SafetyIncident> getIncidentsByStatus(SafetyIncident.Status status) {
        return safetyIncidentRepository.findByStatus(status);
    }

    @Transactional
    public SafetyIncident reportIncident(SafetyIncident incident) {
        incident.setStatus(SafetyIncident.Status.REPORTED);
        return safetyIncidentRepository.save(incident);
    }

    @Transactional
    public SafetyIncident startInvestigation(Long id) {
        SafetyIncident incident = safetyIncidentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Incident not found"));
        incident.setStatus(SafetyIncident.Status.INVESTIGATING);
        return safetyIncidentRepository.save(incident);
    }

    @Transactional
    public SafetyIncident resolveIncident(Long id) {
        SafetyIncident incident = safetyIncidentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Incident not found"));
        incident.setStatus(SafetyIncident.Status.RESOLVED);
        return safetyIncidentRepository.save(incident);
    }

    @Transactional
    public SafetyIncident closeIncident(Long id) {
        SafetyIncident incident = safetyIncidentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Incident not found"));
        incident.setStatus(SafetyIncident.Status.CLOSED);
        return safetyIncidentRepository.save(incident);
    }

    public String exportToOSHA(Long id) {
        SafetyIncident incident = safetyIncidentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Incident not found"));
        // Simulate OSHA export logic
        return "OSHA Exported: " + incident.getId();
    }
}
