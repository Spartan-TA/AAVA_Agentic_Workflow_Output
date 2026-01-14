package com.example.warehouse.service;

import com.example.warehouse.entity.SafetyIncident;
import com.example.warehouse.repository.SafetyIncidentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Service for SafetyIncident operations.
 */
@Service
public class SafetyIncidentService {
    @Autowired
    private SafetyIncidentRepository safetyIncidentRepository;

    public List<SafetyIncident> getIncidentsByDateRange(LocalDate from, LocalDate to) {
        return safetyIncidentRepository.findByDateRange(from, to);
    }

    public List<SafetyIncident> getOpenIncidents() {
        return safetyIncidentRepository.findAllOpenIncidents();
    }

    @Transactional
    public SafetyIncident reportIncident(SafetyIncident incident) {
        incident.setStatus("OPEN");
        return safetyIncidentRepository.save(incident);
    }

    @Transactional
    public SafetyIncident updateIncidentStatus(Long id, String status) {
        SafetyIncident incident = safetyIncidentRepository.findById(id).orElseThrow();
        incident.setStatus(status);
        return safetyIncidentRepository.save(incident);
    }
}
