package com.example.warehouse.safety.service;

import com.example.warehouse.safety.entity.SafetyIncident;
import com.example.warehouse.safety.repository.SafetyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class SafetyService {
    @Autowired
    private SafetyRepository safetyRepository;

    // Get all safety incidents
    public List<SafetyIncident> getAllIncidents() {
        return safetyRepository.findAll();
    }

    // Get safety incidents by employee
    public List<SafetyIncident> getIncidentsByEmployee(Long employeeId) {
        return safetyRepository.findByEmployeeId(employeeId);
    }

    // Get safety incident by ID
    public Optional<SafetyIncident> getIncidentById(Long id) {
        return safetyRepository.findById(id);
    }

    // Create new safety incident
    @Transactional
    public SafetyIncident createIncident(SafetyIncident incident) {
        return safetyRepository.save(incident);
    }

    // Update safety incident
    @Transactional
    public Optional<SafetyIncident> updateIncident(Long id, SafetyIncident incident) {
        return safetyRepository.findById(id).map(existing -> {
            existing.setDescription(incident.getDescription());
            existing.setIncidentTime(incident.getIncidentTime());
            existing.setSeverity(incident.getSeverity());
            existing.setCorrectiveAction(incident.getCorrectiveAction());
            return safetyRepository.save(existing);
        });
    }

    // Delete safety incident
    @Transactional
    public boolean deleteIncident(Long id) {
        if (safetyRepository.existsById(id)) {
            safetyRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
