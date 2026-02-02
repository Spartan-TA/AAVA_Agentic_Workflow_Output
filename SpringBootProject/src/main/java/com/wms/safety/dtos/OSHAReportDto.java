package com.wms.safety.dtos;

import lombok.*;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for OSHA report generation
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OSHAReportDto {
    private String oshaReportNumber;
    private Long incidentId;
    private LocalDateTime incidentDateTime;
    private String location;
    private String description;
    private Long reportedBy;
}
