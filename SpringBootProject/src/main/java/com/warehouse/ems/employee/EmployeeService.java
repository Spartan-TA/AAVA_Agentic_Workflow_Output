package com.warehouse.ems.employee;

import com.warehouse.ems.audit.AuditService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final AuditService auditService;

    public EmployeeService(EmployeeRepository employeeRepository, AuditService auditService) {
        this.employeeRepository = employeeRepository;
        this.auditService = auditService;
    }

    @Transactional
    public Employee createEmployee(Employee employee, Long actorId) {
        Employee saved = employeeRepository.save(employee);
        auditService.log(actorId, "CREATE_EMPLOYEE", "Created employee: " + saved.getId());
        return saved;
    }

    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    public Employee getEmployee(Long id) {
        return employeeRepository.findById(id).orElseThrow(() -> new RuntimeException("Employee not found"));
    }

    @Transactional
    public Employee updateEmployee(Long id, Employee updated, Long actorId) {
        Employee existing = getEmployee(id);
        updated.setId(id);
        Employee saved = employeeRepository.save(updated);
        auditService.log(actorId, "UPDATE_EMPLOYEE", "Updated employee: " + id);
        return saved;
    }

    @Transactional
    public void deleteEmployee(Long id, Long actorId) {
        employeeRepository.deleteById(id);
        auditService.log(actorId, "DELETE_EMPLOYEE", "Deleted employee: " + id);
    }
}