package com.wems.scheduling.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class ScheduleDto {
    private Long employeeId;
    private Long shiftTemplateId;
    private LocalDate scheduleDate;
    private String notes;
}
