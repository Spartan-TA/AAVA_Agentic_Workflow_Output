package com.companyname.wem.scheduling.service;

import com.companyname.wem.employee.domain.Employee;
import com.companyname.wem.employee.repository.EmployeeRepository;
import com.companyname.wem.scheduling.domain.EmployeeShiftAssignment;
import com.companyname.wem.scheduling.domain.ShiftTemplate;
import com.companyname.wem.scheduling.dto.ShiftAssignmentDTO;
import com.companyname.wem.scheduling.repository.EmployeeShiftAssignmentRepository;
import com.companyname.wem.scheduling.repository.ShiftTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScheduleService {
    private final EmployeeShiftAssignmentRepository assignmentRepository;
    private final ShiftTemplateRepository templateRepository;
    private final EmployeeRepository employeeRepository;

    @Transactional
    public EmployeeShiftAssignment assignShift(ShiftAssignmentDTO dto) {
        Employee employee = employeeRepository.findById(dto.getEmployeeId())
            .orElseThrow(() -> new RuntimeException("Employee not found"));
        ShiftTemplate template = templateRepository.findById(dto.getShiftTemplateId())
            .orElseThrow(() -> new RuntimeException("Shift template not found"));
        
        if (hasConflict(dto.getEmployeeId(), dto.getDate())) {
            throw new RuntimeException("Shift conflict detected");
        }
        
        EmployeeShiftAssignment assignment = EmployeeShiftAssignment.builder()
            .employee(employee)
            .shiftTemplate(template)
            .date(dto.getDate())
            .overtime(dto.isOvertime())
            .build();
        
        return assignmentRepository.save(assignment);
    }

    public boolean hasConflict(Long employeeId, LocalDate date) {
        List<EmployeeShiftAssignment> existing = assignmentRepository.findByEmployeeIdAndDate(employeeId, date);
        return !existing.isEmpty();
    }

    public List<EmployeeShiftAssignment> getEmployeeSchedule(Long employeeId) {
        return assignmentRepository.findByEmployeeId(employeeId);
    }
}
