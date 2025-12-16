package com.companyname.wems.safety.service;

import com.companyname.wems.safety.model.SafetyIncident;
import com.companyname.wems.safety.repository.SafetyIncidentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SafetyService {
    private final SafetyIncidentRepository safetyIncidentRepository;

    // Record incident
    public SafetyIncident recordIncident(SafetyIncident incident) {
        incident.setStatus("OPEN");
        return safetyIncidentRepository.save(incident);
    }

    // Update incident status
    public SafetyIncident updateIncidentStatus(Long id, String status, String notes) {
        SafetyIncident incident = safetyIncidentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("SafetyIncident not found"));
        incident.setStatus(status);
        incident.setInvestigationNotes(notes);
        return safetyIncidentRepository.save(incident);
    }

    // List incidents
    public List<SafetyIncident> listIncidents() {
        return safetyIncidentRepository.findAll();
    }

    // Export OSHA 300/300A report (dummy implementation)
    public List<SafetyIncident> exportOSHAReport(LocalDate start, LocalDate end) {
        return safetyIncidentRepository.findByIncidentDateBetween(start, end);
    }
}