package com.wems.safety.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class SafetyIncidentDto {
    private String incidentNumber;
    private LocalDate incidentDate;
    private LocalDateTime incidentTime;
    private String type;
    private String severity;
    private String location;
    private String description;
    private List<Long> involvedEmployeeIds;
    private Long reportedById;
    private String status;
    private String immediateActionTaken;
    private String rootCause;
    private String correctiveActions;
    private Long investigatorId;
    private LocalDateTime investigationStartedAt;
    private LocalDateTime resolvedAt;
    private boolean oshaRecordable;
    private Integer daysAwayFromWork;
}
