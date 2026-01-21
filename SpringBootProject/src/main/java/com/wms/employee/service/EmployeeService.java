package com.wms.employee.service;

import com.wms.employee.dto.CreateEmployeeRequest;
import com.wms.employee.dto.EmployeeDto;
import com.wms.employee.dto.UpdateEmployeeRequest;
import com.wms.employee.entity.Employee;
import com.wms.employee.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Service for Employee CRUD operations and business logic.
 */
@Service
public class EmployeeService {
    @Autowired
    private EmployeeRepository employeeRepository;

    public Page<Employee> getAllEmployees(String department, Pageable pageable) {
        if (department != null) {
            return employeeRepository.findByDepartment(department, pageable);
        }
        return employeeRepository.findAllBySoftDeleteFalse(pageable);
    }

    public Optional<Employee> getEmployeeById(Long id) {
        return employeeRepository.findById(id).filter(e -> !e.isSoftDelete());
    }

    public Optional<Employee> getEmployeeByBadgeId(String badgeId) {
        return employeeRepository.findByBadgeIdAndSoftDeleteFalse(badgeId);
    }

    @Transactional
    public Employee createEmployee(CreateEmployeeRequest request, String actor) {
        Employee employee = new Employee();
        employee.setBadgeId(request.getBadgeId());
        employee.setName(request.getName());
        employee.setRole(request.getRole());
        employee.setDepartment(request.getDepartment());
        employee.setShiftGroup(request.getShiftGroup());
        employee.setHireDate(request.getHireDate());
        employee.setStatus(request.getStatus());
        employee.setSoftDelete(false);
        employee.setCreatedAt(LocalDateTime.now());
        employee.setUpdatedAt(LocalDateTime.now());
        employee.setCreatedBy(actor);
        employee.setUpdatedBy(actor);
        return employeeRepository.save(employee);
    }

    @Transactional
    public Optional<Employee> updateEmployee(Long id, UpdateEmployeeRequest request, String actor) {
        return employeeRepository.findById(id).filter(e -> !e.isSoftDelete()).map(employee -> {
            if (request.getName() != null) employee.setName(request.getName());
            if (request.getRole() != null) employee.setRole(request.getRole());
            if (request.getDepartment() != null) employee.setDepartment(request.getDepartment());
            if (request.getShiftGroup() != null) employee.setShiftGroup(request.getShiftGroup());
            if (request.getHireDate() != null) employee.setHireDate(request.getHireDate());
            if (request.getStatus() != null) employee.setStatus(request.getStatus());
            employee.setUpdatedAt(LocalDateTime.now());
            employee.setUpdatedBy(actor);
            return employeeRepository.save(employee);
        });
    }

    @Transactional
    public boolean softDeleteEmployee(Long id, String actor) {
        return employeeRepository.findById(id).filter(e -> !e.isSoftDelete()).map(employee -> {
            employee.setSoftDelete(true);
            employee.setUpdatedAt(LocalDateTime.now());
            employee.setUpdatedBy(actor);
            employeeRepository.save(employee);
            return true;
        }).orElse(false);
    }
}
