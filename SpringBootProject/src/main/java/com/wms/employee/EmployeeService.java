package com.wms.employee;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class EmployeeService {
    @Autowired
    private EmployeeRepository employeeRepository;

    public Employee createEmployee(EmployeeDto dto) {
        if (employeeRepository.findByBadgeId(dto.getBadgeId()).isPresent()) {
            throw new DuplicateBadgeIdException("Badge ID already exists");
        }
        Employee employee = Employee.builder()
                .name(dto.getName())
                .badgeId(dto.getBadgeId())
                .role(dto.getRole())
                .department(dto.getDepartment())
                .shiftGroup(dto.getShiftGroup())
                .hireDate(dto.getHireDate())
                .status(dto.getStatus())
                .deleted(false)
                .build();
        return employeeRepository.save(employee);
    }

    public Page<Employee> listEmployees(Pageable pageable, String department, String status) {
        if (department != null) return employeeRepository.findByDepartmentAndDeletedFalse(department, pageable);
        if (status != null) return employeeRepository.findByStatusAndDeletedFalse(status, pageable);
        return employeeRepository.findAllByDeletedFalse(pageable);
    }

    public void softDelete(Long id) {
        Employee employee = employeeRepository.findById(id).orElseThrow();
        employee.setDeleted(true);
        employee.setStatus("INACTIVE");
        employeeRepository.save(employee);
    }
}
