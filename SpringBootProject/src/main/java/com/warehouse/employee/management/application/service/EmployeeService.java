package com.warehouse.employee.management.application.service;

import com.warehouse.employee.management.domain.employee.*;
import com.warehouse.employee.management.infrastructure.repository.EmployeeRepository;
import com.warehouse.employee.management.infrastructure.repository.EmployeeSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmployeeService {
    private final EmployeeRepository employeeRepository;

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public Employee createEmployee(Employee employee) {
        return employeeRepository.save(employee);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public Employee updateEmployee(UUID id, Employee updated) {
        Employee existing = employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));
        // Update fields
        existing.setFirstName(updated.getFirstName());
        existing.setLastName(updated.getLastName());
        existing.setEmail(updated.getEmail());
        existing.setPhone(updated.getPhone());
        existing.setHireDate(updated.getHireDate());
        existing.setTerminationDate(updated.getTerminationDate());
        existing.setStatus(updated.getStatus());
        existing.setDepartment(updated.getDepartment());
        existing.setPosition(updated.getPosition());
        existing.setSupervisor(updated.getSupervisor());
        existing.setAddress(updated.getAddress());
        existing.setEmergencyContact(updated.getEmergencyContact());
        return employeeRepository.save(existing);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public Employee patchEmployee(UUID id, Employee patch) {
        Employee existing = employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));
        // Patch only non-null fields
        if (patch.getFirstName() != null) existing.setFirstName(patch.getFirstName());
        if (patch.getLastName() != null) existing.setLastName(patch.getLastName());
        if (patch.getEmail() != null) existing.setEmail(patch.getEmail());
        if (patch.getPhone() != null) existing.setPhone(patch.getPhone());
        if (patch.getHireDate() != null) existing.setHireDate(patch.getHireDate());
        if (patch.getTerminationDate() != null) existing.setTerminationDate(patch.getTerminationDate());
        if (patch.getStatus() != null) existing.setStatus(patch.getStatus());
        if (patch.getDepartment() != null) existing.setDepartment(patch.getDepartment());
        if (patch.getPosition() != null) existing.setPosition(patch.getPosition());
        if (patch.getSupervisor() != null) existing.setSupervisor(patch.getSupervisor());
        if (patch.getAddress() != null) existing.setAddress(patch.getAddress());
        if (patch.getEmergencyContact() != null) existing.setEmergencyContact(patch.getEmergencyContact());
        return employeeRepository.save(existing);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public void softDeleteEmployee(UUID id) {
        Employee existing = employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));
        existing.setDeleted(true);
        employeeRepository.save(existing);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public void restoreEmployee(UUID id) {
        Employee existing = employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));
        existing.setDeleted(false);
        employeeRepository.save(existing);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ADMIN','HR','SUPERVISOR')")
    public List<Employee> searchEmployees(Specification<Employee> spec) {
        return employeeRepository.findAll(spec);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ADMIN','HR','SUPERVISOR')")
    public Optional<Employee> getEmployeeById(UUID id) {
        return employeeRepository.findById(id);
    }
}
