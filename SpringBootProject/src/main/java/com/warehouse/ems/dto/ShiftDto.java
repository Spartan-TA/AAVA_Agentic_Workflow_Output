package com.warehouse.ems.dto;

import com.warehouse.ems.domain.ShiftTemplate;
import java.time.LocalTime;
import java.util.Set;

/**
 * DTO for ShiftTemplate requests and responses.
 */
public class ShiftDto {
    private Long id;
    private String name;
    private LocalTime start;
    private LocalTime end;
    private String recurrenceRule;
    private Set<Long> assignedEmployeeIds;

    // Getters and setters omitted for brevity

    /**
     * Convert DTO to ShiftTemplate entity.
     */
    public ShiftTemplate toEntity(Set<com.warehouse.ems.domain.Employee> employees) {
        ShiftTemplate shift = new ShiftTemplate();
        shift.setId(this.id);
        shift.setName(this.name);
        shift.setStart(this.start);
        shift.setEnd(this.end);
        shift.setRecurrenceRule(this.recurrenceRule);
        shift.setAssignedEmployees(employees);
        return shift;
    }

    /**
     * Create DTO from ShiftTemplate entity.
     */
    public static ShiftDto fromEntity(ShiftTemplate shift) {
        ShiftDto dto = new ShiftDto();
        dto.id = shift.getId();
        dto.name = shift.getName();
        dto.start = shift.getStart();
        dto.end = shift.getEnd();
        dto.recurrenceRule = shift.getRecurrenceRule();
        if (shift.getAssignedEmployees() != null) {
            dto.assignedEmployeeIds = new java.util.HashSet<>();
            for (com.warehouse.ems.domain.Employee emp : shift.getAssignedEmployees()) {
                dto.assignedEmployeeIds.add(emp.getId());
            }
        }
        return dto;
    }
}
