package com.companyname.wem.shift.service;

import com.companyname.wem.shift.domain.EmployeeShiftAssignment;
import com.companyname.wem.shift.domain.ShiftTemplate;
import com.companyname.wem.shift.dto.ShiftAssignmentDTO;
import com.companyname.wem.shift.dto.ShiftTemplateDTO;
import com.companyname.wem.shift.repository.EmployeeShiftAssignmentRepository;
import com.companyname.wem.shift.repository.ShiftTemplateRepository;
import com.companyname.wem.employee.domain.Employee;
import com.companyname.wem.employee.repository.EmployeeRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ScheduleService {
    private final ShiftTemplateRepository shiftTemplateRepository;
    private final EmployeeShiftAssignmentRepository assignmentRepository;
    private final EmployeeRepository employeeRepository;

    public List<ShiftTemplateDTO> getAllShiftTemplates() {
        return shiftTemplateRepository.findAll()
                .stream()
                .map(this::toDto)
                .toList();
    }

    public Optional<ShiftTemplateDTO> getShiftTemplateById(Long id) {
        return shiftTemplateRepository.findById(id).map(this::toDto);
    }

    @Transactional
    public ShiftTemplateDTO createShiftTemplate(@Valid ShiftTemplateDTO dto) {
        ShiftTemplate entity = toEntity(dto);
        ShiftTemplate saved = shiftTemplateRepository.save(entity);
        return toDto(saved);
    }

    @Transactional
    public Optional<ShiftTemplateDTO> updateShiftTemplate(Long id, @Valid ShiftTemplateDTO dto) {
        return shiftTemplateRepository.findById(id)
                .map(existing -> {
                    ShiftTemplate updated = toEntity(dto);
                    updated.setId(existing.getId());
                    ShiftTemplate saved = shiftTemplateRepository.save(updated);
                    return toDto(saved);
                });
    }

    @Transactional
    public boolean deleteShiftTemplate(Long id) {
        return shiftTemplateRepository.findById(id)
                .map(template -> {
                    shiftTemplateRepository.delete(template);
                    return true;
                }).orElse(false);
    }

    public List<ShiftAssignmentDTO> getAssignmentsByEmployee(Long employeeId) {
        return employeeRepository.findById(employeeId)
                .map(employee -> assignmentRepository.findByEmployee(employee)
                        .stream()
                        .map(this::toAssignmentDto)
                        .toList())
                .orElse(List.of());
    }

    @Transactional
    public ShiftAssignmentDTO assignShift(@Valid ShiftAssignmentDTO dto) {
        Employee employee = employeeRepository.findById(dto.getEmployeeId()).orElseThrow();
        ShiftTemplate template = shiftTemplateRepository.findById(dto.getShiftTemplateId()).orElseThrow();
        EmployeeShiftAssignment assignment = EmployeeShiftAssignment.builder()
                .employee(employee)
                .shiftTemplate(template)
                .date(dto.getDate())
                .build();
        EmployeeShiftAssignment saved = assignmentRepository.save(assignment);
        return toAssignmentDto(saved);
    }

    private ShiftTemplateDTO toDto(ShiftTemplate entity) {
        return ShiftTemplateDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .startTime(entity.getStartTime())
                .endTime(entity.getEndTime())
                .group(entity.getGroup())
                .build();
    }

    private ShiftTemplate toEntity(ShiftTemplateDTO dto) {
        return ShiftTemplate.builder()
                .id(dto.getId())
                .name(dto.getName())
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .group(dto.getGroup())
                .build();
    }

    private ShiftAssignmentDTO toAssignmentDto(EmployeeShiftAssignment entity) {
        return ShiftAssignmentDTO.builder()
                .id(entity.getId())
                .employeeId(entity.getEmployee().getId())
                .shiftTemplateId(entity.getShiftTemplate().getId())
                .date(entity.getDate())
                .build();
    }
}
