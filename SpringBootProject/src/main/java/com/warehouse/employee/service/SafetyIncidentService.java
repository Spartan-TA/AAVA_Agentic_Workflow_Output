package com.warehouse.employee.service;

import com.warehouse.employee.entity.SafetyIncident;
import com.warehouse.employee.repository.SafetyIncidentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SafetyIncidentService {
    @Autowired
    private SafetyIncidentRepository safetyIncidentRepository;

    public List<SafetyIncident> getAllSafetyIncidents() {
        return safetyIncidentRepository.findAll();
    }

    public Optional<SafetyIncident> getSafetyIncidentById(Long id) {
        return safetyIncidentRepository.findById(id);
    }

    public SafetyIncident saveSafetyIncident(SafetyIncident safetyIncident) {
        return safetyIncidentRepository.save(safetyIncident);
    }

    public void deleteSafetyIncident(Long id) {
        safetyIncidentRepository.deleteById(id);
    }
}
