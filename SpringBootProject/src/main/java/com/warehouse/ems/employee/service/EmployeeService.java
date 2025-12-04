package com.warehouse.ems.employee.service;

import com.warehouse.ems.employee.entity.Employee;
import com.warehouse.ems.employee.repository.EmployeeRepository;
import com.warehouse.ems.employee.dto.EmployeeDto;
import com.warehouse.ems.employee.dto.EmployeeCreateDto;
import com.warehouse.ems.employee.dto.EmployeeUpdateDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Service for Employee business logic: CRUD, soft-delete, filtering.
 */
@Service
public class EmployeeService {
    @Autowired
    private EmployeeRepository employeeRepository;

    public Page<Employee> getAllEmployees(Pageable pageable) {
        return employeeRepository.findAllByDeletedFalse(pageable);
    }

    public Optional<Employee> getEmployeeByBadgeId(String badgeId) {
        return employeeRepository.findByBadgeIdAndDeletedFalse(badgeId);
    }

    public Employee createEmployee(EmployeeCreateDto dto) {
        Employee employee = new Employee();
        employee.setBadgeId(dto.getBadgeId());
        employee.setName(dto.getName());
        employee.setRole(dto.getRole());
        employee.setDepartment(dto.getDepartment());
        employee.setShiftGroup(dto.getShiftGroup());
        employee.setHireDate(dto.getHireDate());
        employee.setStatus(dto.getStatus());
        return employeeRepository.save(employee);
    }

    @Transactional
    public Employee updateEmployee(Long id, EmployeeUpdateDto dto) {
        Employee employee = employeeRepository.findById(id).orElseThrow();
        if (dto.getName() != null) employee.setName(dto.getName());
        if (dto.getRole() != null) employee.setRole(dto.getRole());
        if (dto.getDepartment() != null) employee.setDepartment(dto.getDepartment());
        if (dto.getShiftGroup() != null) employee.setShiftGroup(dto.getShiftGroup());
        if (dto.getHireDate() != null) employee.setHireDate(dto.getHireDate());
        if (dto.getStatus() != null) employee.setStatus(dto.getStatus());
        return employeeRepository.save(employee);
    }

    @Transactional
    public void softDeleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id).orElseThrow();
        employee.setDeleted(true);
        employeeRepository.save(employee);
    }

    public Page<Employee> filterEmployees(String department, String role, Pageable pageable) {
        return employeeRepository.filterByDepartmentAndRole(department, role, pageable);
    }
}
