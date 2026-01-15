package com.warehouse.safety.service;

import com.warehouse.safety.entity.SafetyIncident;
import com.warehouse.safety.repository.SafetyIncidentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class SafetyMetricsService {
    @Autowired
    private SafetyIncidentRepository safetyIncidentRepository;

    public long getTotalIncidents() {
        return safetyIncidentRepository.count();
    }

    public long getIncidentsBySeverity(SafetyIncident.Severity severity) {
        return safetyIncidentRepository.findBySeverity(severity).size();
    }

    public long getResolvedIncidents() {
        return safetyIncidentRepository.findByStatus(SafetyIncident.Status.RESOLVED).size();
    }

    public long getOpenIncidents() {
        return safetyIncidentRepository.findByStatus(SafetyIncident.Status.REPORTED).size() +
               safetyIncidentRepository.findByStatus(SafetyIncident.Status.INVESTIGATING).size();
    }
}
