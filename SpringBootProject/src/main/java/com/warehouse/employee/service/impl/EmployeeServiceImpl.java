package com.warehouse.employee.service.impl;

import com.warehouse.employee.domain.Employee;
import com.warehouse.employee.domain.enums.EmployeeStatus;
import com.warehouse.employee.exception.DuplicateBadgeIdException;
import com.warehouse.employee.exception.EmployeeNotFoundException;
import com.warehouse.employee.repository.EmployeeRepository;
import com.warehouse.employee.service.AuditService;
import com.warehouse.employee.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Implementation of EmployeeService.
 */
@Service
@Slf4j
public class EmployeeServiceImpl implements EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final AuditService auditService;

    public EmployeeServiceImpl(EmployeeRepository employeeRepository, AuditService auditService) {
        this.employeeRepository = employeeRepository;
        this.auditService = auditService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public Employee createEmployee(Employee employee) {
        log.info("Creating employee with badgeId: {}", employee.getBadgeId());
        if (employeeRepository.existsByBadgeId(employee.getBadgeId())) {
            log.warn("Duplicate badgeId detected: {}", employee.getBadgeId());
            throw new DuplicateBadgeIdException("Badge ID already exists");
        }
        employee.setStatus(EmployeeStatus.ACTIVE);
        Employee saved = employeeRepository.save(employee);
        auditService.logAudit("Employee", saved.getId(), "CREATE", getCurrentUser());
        return saved;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public Employee updateEmployee(Employee employee) {
        log.info("Updating employee with id: {}", employee.getId());
        Employee existing = employeeRepository.findById(employee.getId())
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found"));
        if (!existing.getBadgeId().equals(employee.getBadgeId()) && employeeRepository.existsByBadgeId(employee.getBadgeId())) {
            log.warn("Duplicate badgeId detected during update: {}", employee.getBadgeId());
            throw new DuplicateBadgeIdException("Badge ID already exists");
        }
        // Copy updatable fields
        existing.setName(employee.getName());
        existing.setDepartment(employee.getDepartment());
        existing.setRole(employee.getRole());
        existing.setBadgeId(employee.getBadgeId());
        existing.setEmail(employee.getEmail());
        // ... other fields as needed
        Employee updated = employeeRepository.save(existing);
        auditService.logAudit("Employee", updated.getId(), "UPDATE", getCurrentUser());
        return updated;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<Employee> getEmployeeById(Long id) {
        log.debug("Fetching employee by id: {}", id);
        return employeeRepository.findById(id).filter(e -> e.getStatus() == EmployeeStatus.ACTIVE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Page<Employee> getAllEmployees(Pageable pageable) {
        log.debug("Fetching all employees (paginated)");
        return employeeRepository.findAllByStatus(EmployeeStatus.ACTIVE, pageable);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void deleteEmployee(Long id) {
        log.info("Soft deleting employee with id: {}", id);
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found"));
        employee.setStatus(EmployeeStatus.INACTIVE);
        employeeRepository.save(employee);
        auditService.logAudit("Employee", id, "DELETE", getCurrentUser());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<Employee> findByBadgeId(String badgeId) {
        log.debug("Finding employee by badgeId: {}", badgeId);
        return employeeRepository.findByBadgeIdAndStatus(badgeId, EmployeeStatus.ACTIVE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<Employee> findByDepartment(String department) {
        log.debug("Finding employees by department: {}", department);
        return employeeRepository.findByDepartmentAndStatus(department, EmployeeStatus.ACTIVE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<Employee> findByRole(String role) {
        log.debug("Finding employees by role: {}", role);
        return employeeRepository.findByRoleAndStatus(role, EmployeeStatus.ACTIVE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Page<Employee> searchEmployees(String keyword, Pageable pageable) {
        log.debug("Searching employees with keyword: {}", keyword);
        return employeeRepository.searchByKeyword(keyword, EmployeeStatus.ACTIVE, pageable);
    }

    /**
     * Get current user for audit logging (stub).
     */
    private String getCurrentUser() {
        // TODO: Integrate with security context
        return "system";
    }
}
