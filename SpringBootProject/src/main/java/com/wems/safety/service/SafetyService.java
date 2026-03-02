package com.wems.safety.service;

import com.wems.safety.domain.*;
import com.wems.employee.domain.Employee;
import com.wems.common.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SafetyService {
    @Autowired
    private SafetyIncidentRepository safetyIncidentRepository;

    public SafetyIncident reportIncident(Employee reporter, SafetyIncident incident) {
        incident.setReportedBy(reporter);
        incident.setStatus(IncidentStatus.OPEN);
        return safetyIncidentRepository.save(incident);
    }

    @Transactional
    public SafetyIncident startInvestigation(Long incidentId, Employee investigator) {
        SafetyIncident incident = safetyIncidentRepository.findById(incidentId)
                .orElseThrow(() -> new ResourceNotFoundException("Incident not found"));
        incident.setStatus(IncidentStatus.INVESTIGATING);
        incident.setInvestigator(investigator);
        incident.setInvestigationStartedAt(LocalDateTime.now());
        return safetyIncidentRepository.save(incident);
    }

    @Transactional
    public SafetyIncident completeInvestigation(Long incidentId, String rootCause, String correctiveActions) {
        SafetyIncident incident = safetyIncidentRepository.findById(incidentId)
                .orElseThrow(() -> new ResourceNotFoundException("Incident not found"));
        incident.setStatus(IncidentStatus.RESOLVED);
        incident.setRootCause(rootCause);
        incident.setCorrectiveActions(correctiveActions);
        incident.setResolvedAt(LocalDateTime.now());
        return safetyIncidentRepository.save(incident);
    }

    public List<SafetyIncident> generateOshaSummary() {
        return safetyIncidentRepository.findAll(); // Replace with proper OSHA summary logic
    }
}
