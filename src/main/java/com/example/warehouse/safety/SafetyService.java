package com.example.warehouse.safety;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SafetyService {
    @Autowired
    private SafetyIncidentRepository safetyIncidentRepository;

    public List<SafetyIncident> getAllIncidents() {
        return safetyIncidentRepository.findAll();
    }

    public Optional<SafetyIncident> getIncidentById(Long id) {
        return safetyIncidentRepository.findById(id);
    }

    public List<SafetyIncident> getIncidentsByEmployee(Long employeeId) {
        return safetyIncidentRepository.findByEmployeeId(employeeId);
    }

    public SafetyIncident createIncident(SafetyIncidentDto dto) {
        SafetyIncident incident = new SafetyIncident();
        incident.setEmployeeId(dto.getEmployeeId());
        incident.setDescription(dto.getDescription());
        incident.setIncidentTime(dto.getIncidentTime());
        incident.setSeverity(dto.getSeverity());
        incident.setStatus(dto.getStatus());
        return safetyIncidentRepository.save(incident);
    }

    public void deleteIncident(Long id) {
        safetyIncidentRepository.deleteById(id);
    }
}
