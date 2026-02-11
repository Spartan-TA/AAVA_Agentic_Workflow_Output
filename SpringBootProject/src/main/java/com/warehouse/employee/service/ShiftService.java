package com.warehouse.employee.service;

import com.warehouse.employee.domain.Employee;
import com.warehouse.employee.domain.ShiftAssignment;
import com.warehouse.employee.domain.ShiftTemplate;
import com.warehouse.employee.dto.ShiftAssignmentRequest;
import com.warehouse.employee.dto.ShiftAssignmentResponse;
import com.warehouse.employee.exception.EmployeeNotFoundException;
import com.warehouse.employee.exception.ShiftConflictException;
import com.warehouse.employee.mapper.ShiftMapper;
import com.warehouse.employee.repository.EmployeeRepository;
import com.warehouse.employee.repository.ShiftAssignmentRepository;
import com.warehouse.employee.repository.ShiftTemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for shift assignment, conflict detection, and retrieving employee shifts.
 */
@Service
public class ShiftService {

    private final ShiftAssignmentRepository shiftAssignmentRepository;
    private final ShiftTemplateRepository shiftTemplateRepository;
    private final EmployeeRepository employeeRepository;
    private final ShiftMapper shiftMapper;

    @Autowired
    public ShiftService(ShiftAssignmentRepository shiftAssignmentRepository,
                        ShiftTemplateRepository shiftTemplateRepository,
                        EmployeeRepository employeeRepository,
                        ShiftMapper shiftMapper) {
        this.shiftAssignmentRepository = shiftAssignmentRepository;
        this.shiftTemplateRepository = shiftTemplateRepository;
        this.employeeRepository = employeeRepository;
        this.shiftMapper = shiftMapper;
    }

    /**
     * Assign a shift to an employee, checking for conflicts.
     * @param request ShiftAssignmentRequest
     * @return ShiftAssignmentResponse
     */
    @Transactional
    public ShiftAssignmentResponse assignShift(ShiftAssignmentRequest request) {
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .filter(e -> !e.isDeleted())
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found: " + request.getEmployeeId()));
        ShiftTemplate template = shiftTemplateRepository.findById(request.getTemplateId())
                .orElseThrow(() -> new IllegalArgumentException("Shift template not found: " + request.getTemplateId()));
        LocalDate shiftDate = request.getShiftDate();
        if (hasConflict(employee, shiftDate)) {
            throw new ShiftConflictException("Employee already has a shift on " + shiftDate);
        }
        ShiftAssignment assignment = new ShiftAssignment();
        assignment.setEmployee(employee);
        assignment.setShiftTemplate(template);
        assignment.setAssignmentDate(shiftDate);
        ShiftAssignment saved = shiftAssignmentRepository.save(assignment);
        return shiftMapper.toAssignmentResponse(saved);
    }

    /**
     * Check if an employee has a shift conflict on a given date.
     * @param employee Employee
     * @param date LocalDate
     * @return true if conflict exists
     */
    @Transactional(readOnly = true)
    public boolean hasConflict(Employee employee, LocalDate date) {
        return shiftAssignmentRepository.findByEmployeeAndAssignmentDate(employee, date).isPresent();
    }

    /**
     * Get all shifts assigned to an employee.
     * @param employeeId Employee ID
     * @return List of ShiftAssignmentResponse
     */
    @Transactional(readOnly = true)
    public List<ShiftAssignmentResponse> getEmployeeShifts(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .filter(e -> !e.isDeleted())
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found: " + employeeId));
        List<ShiftAssignment> assignments = shiftAssignmentRepository.findAll()
                .stream()
                .filter(a -> a.getEmployee().getId().equals(employeeId))
                .collect(Collectors.toList());
        return assignments.stream().map(shiftMapper::toAssignmentResponse).collect(Collectors.toList());
    }
}
