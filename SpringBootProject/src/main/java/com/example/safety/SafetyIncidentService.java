package com.example.safety;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    public SafetyIncident saveIncident(SafetyIncident incident) {
        return safetyIncidentRepository.save(incident);
    }

    public void deleteIncident(Long id) {
        safetyIncidentRepository.deleteById(id);
    }
}