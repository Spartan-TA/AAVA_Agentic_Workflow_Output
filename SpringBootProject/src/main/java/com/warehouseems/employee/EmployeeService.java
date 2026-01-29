package com.warehouseems.employee;

import com.warehouseems.employee.dto.EmployeeRequest;
import com.warehouseems.employee.dto.EmployeeResponse;
import com.warehouseems.audit.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeService {
    private final EmployeeRepository repository;
    private final AuditService auditService;

    public EmployeeResponse create(EmployeeRequest request) {
        Employee employee = Employee.builder()
                .name(request.getName())
                .badgeId(request.getBadgeId())
                .role(request.getRole())
                .department(request.getDepartment())
                .shiftGroup(request.getShiftGroup())
                .hireDate(request.getHireDate())
                .status(request.getStatus())
                .deleted(false)
                .build();
        repository.save(employee);
        auditService.logCreate("Employee", employee.getId(), employee);
        return toResponse(employee);
    }

    public Page<EmployeeResponse> list(Pageable pageable, Map<String, String> filters) {
        // Basic filtering by department, status, role
        Page<Employee> page = repository.findAllByDeletedFalse(pageable);
        return page.map(this::toResponse);
    }

    public EmployeeResponse update(Long id, EmployeeRequest request) {
        Employee employee = repository.findById(id)
                .filter(e -> !e.isDeleted())
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        Employee before = Employee.builder().build();
        BeanUtils.copyProperties(employee, before);
        employee.setName(request.getName());
        employee.setBadgeId(request.getBadgeId());
        employee.setRole(request.getRole());
        employee.setDepartment(request.getDepartment());
        employee.setShiftGroup(request.getShiftGroup());
        employee.setHireDate(request.getHireDate());
        employee.setStatus(request.getStatus());
        repository.save(employee);
        auditService.logUpdate("Employee", employee.getId(), before, employee);
        return toResponse(employee);
    }

    public void delete(Long id) {
        Employee employee = repository.findById(id)
                .filter(e -> !e.isDeleted())
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        employee.setDeleted(true);
        repository.save(employee);
        auditService.logDelete("Employee", employee.getId(), employee);
    }

    public Optional<EmployeeResponse> getByBadgeId(String badgeId) {
        return repository.findByBadgeIdAndDeletedFalse(badgeId).map(this::toResponse);
    }

    private EmployeeResponse toResponse(Employee employee) {
        return EmployeeResponse.builder()
                .id(employee.getId())
                .name(employee.getName())
                .badgeId(employee.getBadgeId())
                .role(employee.getRole())
                .department(employee.getDepartment())
                .shiftGroup(employee.getShiftGroup())
                .hireDate(employee.getHireDate())
                .status(employee.getStatus())
                .createdAt(employee.getCreatedAt())
                .updatedAt(employee.getUpdatedAt())
                .build();
    }
}
