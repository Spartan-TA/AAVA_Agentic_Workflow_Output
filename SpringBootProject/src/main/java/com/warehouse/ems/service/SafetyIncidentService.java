package com.warehouse.ems.service;

import com.warehouse.ems.dto.SafetyIncidentDto;
import java.util.List;

public interface SafetyIncidentService {
    SafetyIncidentDto getSafetyIncidentById(Long id);
    List<SafetyIncidentDto> getAllSafetyIncidents();
    SafetyIncidentDto createSafetyIncident(SafetyIncidentDto safetyIncidentDto);
    void deleteSafetyIncident(Long id);
}
