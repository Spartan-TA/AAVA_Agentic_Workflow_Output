package com.wms.safety.services;

import com.wms.safety.dtos.OSHAReportDto;
import com.wms.safety.dtos.SafetyIncidentDto;
import com.wms.safety.enums.IncidentStatus;
import com.wms.safety.model.SafetyIncident;
import com.wms.safety.repositories.SafetyIncidentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing safety incidents and OSHA reporting
 */
@Service
@RequiredArgsConstructor
public class SafetyService {
    private final SafetyIncidentRepository safetyIncidentRepository;

    /**
     * Report a new safety incident
     */
    @Transactional
    public SafetyIncidentDto reportIncident(SafetyIncidentDto dto) {
        SafetyIncident incident = SafetyIncident.builder()
                .incidentDateTime(dto.getIncidentDateTime())
                .location(dto.getLocation())
                .description(dto.getDescription())
                .status(dto.getStatus())
                .reportedBy(dto.getReportedBy())
                .oshaReportNumber(dto.getOshaReportNumber())
                .build();
        SafetyIncident saved = safetyIncidentRepository.save(incident);
        dto.setId(saved.getId());
        return dto;
    }

    /**
     * Get all incidents by status
     */
    public List<SafetyIncidentDto> getIncidentsByStatus(IncidentStatus status) {
        return safetyIncidentRepository.findByStatus(status).stream()
                .map(i -> SafetyIncidentDto.builder()
                        .id(i.getId())
                        .incidentDateTime(i.getIncidentDateTime())
                        .location(i.getLocation())
                        .description(i.getDescription())
                        .status(i.getStatus())
                        .reportedBy(i.getReportedBy())
                        .oshaReportNumber(i.getOshaReportNumber())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Generate OSHA report DTO for an incident
     */
    public OSHAReportDto generateOSHAReport(Long incidentId) {
        SafetyIncident incident = safetyIncidentRepository.findById(incidentId)
                .orElseThrow(() -> new IllegalArgumentException("Incident not found"));
        return OSHAReportDto.builder()
                .oshaReportNumber(incident.getOshaReportNumber())
                .incidentId(incident.getId())
                .incidentDateTime(incident.getIncidentDateTime())
                .location(incident.getLocation())
                .description(incident.getDescription())
                .reportedBy(incident.getReportedBy())
                .build();
    }
}
