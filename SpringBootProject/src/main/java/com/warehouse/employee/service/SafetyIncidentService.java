package com.warehouse.employee.service;

import com.warehouse.employee.domain.SafetyIncident;
import com.warehouse.employee.dto.SafetyIncidentDto;
import com.warehouse.employee.mapper.SafetyIncidentMapper;
import com.warehouse.employee.repository.SafetyIncidentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for safety incident reporting and OSHA export.
 */
@Service
public class SafetyIncidentService {

    private final SafetyIncidentRepository safetyIncidentRepository;
    private final SafetyIncidentMapper safetyIncidentMapper;

    @Autowired
    public SafetyIncidentService(SafetyIncidentRepository safetyIncidentRepository,
                                SafetyIncidentMapper safetyIncidentMapper) {
        this.safetyIncidentRepository = safetyIncidentRepository;
        this.safetyIncidentMapper = safetyIncidentMapper;
    }

    /**
     * Report a new safety incident.
     * @param dto SafetyIncidentDto
     * @return SafetyIncidentDto
     */
    @Transactional
    public SafetyIncidentDto reportIncident(SafetyIncidentDto dto) {
        SafetyIncident incident = safetyIncidentMapper.toEntity(dto);
        SafetyIncident saved = safetyIncidentRepository.save(incident);
        return safetyIncidentMapper.toDto(saved);
    }

    /**
     * Export all safety incidents in OSHA CSV format.
     * @return String CSV content
     */
    @Transactional(readOnly = true)
    public String exportOSHA() {
        List<SafetyIncident> incidents = safetyIncidentRepository.findAll();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        // Header
        pw.println("id,date,employeeId,description,severity,status");
        for (SafetyIncident incident : incidents) {
            pw.printf("%d,%s,%d,%s,%s,%s
",
                    incident.getId(),
                    incident.getDate() != null ? incident.getDate().toString() : "",
                    incident.getEmployee() != null ? incident.getEmployee().getId() : 0,
                    incident.getDescription() != null ? incident.getDescription().replace(",", " ") : "",
                    incident.getSeverity() != null ? incident.getSeverity() : "",
                    incident.getStatus() != null ? incident.getStatus() : "");
        }
        pw.flush();
        return sw.toString();
    }
}
