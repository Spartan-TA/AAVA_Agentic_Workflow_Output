package com.wms.safety.dtos;

import com.wms.safety.enums.IncidentStatus;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for SafetyIncident
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SafetyIncidentDto {
    private Long id;
    private LocalDateTime incidentDateTime;
    private String location;
    private String description;
    private IncidentStatus status;
    private Long reportedBy;
    private String oshaReportNumber;
}
