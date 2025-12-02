package com.wms.ems.safety;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class SafetyService {

    @Autowired
    private SafetyIncidentRepository safetyIncidentRepository;

    // Incident workflow
    @Transactional
    public SafetyIncident reportIncident(SafetyIncident incident) {
        incident.setStatus("Open");
        incident.setReportedDate(LocalDate.now());
        return safetyIncidentRepository.save(incident);
    }

    @Transactional
    public SafetyIncident updateIncidentStatus(Long id, String status) {
        SafetyIncident incident = safetyIncidentRepository.findById(id).orElseThrow();
        incident.setStatus(status);
        return safetyIncidentRepository.save(incident);
    }

    public List<SafetyIncident> getIncidents() {
        return safetyIncidentRepository.findAll();
    }

    // OSHA report generation (stub)
    public String generateOSHAReport(String type) {
        // Generate OSHA 300/300A report logic here
        return "OSHA report for type: " + type;
    }
}
