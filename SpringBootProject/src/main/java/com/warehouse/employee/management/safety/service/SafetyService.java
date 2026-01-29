package com.warehouse.employee.management.safety.service;

import com.warehouse.employee.management.dto.SafetyIncidentDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
public class SafetyService {
    private final List<SafetyIncidentDto> incidents = new ArrayList<>();

    @Transactional
    public SafetyIncidentDto reportIncident(SafetyIncidentDto incident) {
        incident.setStatus("REPORTED");
        incidents.add(incident);
        return incident;
    }

    @Transactional
    public SafetyIncidentDto updateIncidentStatus(int incidentIndex, String status) {
        if (incidentIndex < 0 || incidentIndex >= incidents.size()) throw new IllegalArgumentException("Invalid incident index");
        SafetyIncidentDto incident = incidents.get(incidentIndex);
        incident.setStatus(status);
        return incident;
    }

    public List<SafetyIncidentDto> getAllIncidents() {
        return Collections.unmodifiableList(incidents);
    }

    public List<SafetyIncidentDto> getIncidentsByStatus(String status) {
        List<SafetyIncidentDto> result = new ArrayList<>();
        for (SafetyIncidentDto i : incidents) {
            if (i.getStatus().equalsIgnoreCase(status)) {
                result.add(i);
            }
        }
        return result;
    }

    public void generateOSHAReport() {
        // Stub for OSHA reporting logic
    }
}
