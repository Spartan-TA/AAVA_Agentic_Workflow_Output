package com.warehouse.ems.service;

import com.warehouse.ems.domain.Employee;
import com.warehouse.ems.domain.AuditLog;
import com.warehouse.ems.repository.EmployeeRepository;
import com.warehouse.ems.repository.AuditLogRepository;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmployeeServiceTestPart2 {
    @Test
    void testUpdateEmployee_Normal() {
        EmployeeRepository employeeRepository = mock(EmployeeRepository.class);
        AuditLogRepository auditLogRepository = mock(AuditLogRepository.class);
        EmployeeService employeeService = new EmployeeService();
        employeeService.employeeRepository = employeeRepository;
        employeeService.auditLogRepository = auditLogRepository;
        Employee employee = new Employee("B123", "John Doe", "WORKER");
        employee.setId(1L);
        Employee updated = new Employee("B123", "Jane Smith", "SUPERVISOR");
        updated.setDepartment("Packing");
        updated.setShiftGroup("B");
        updated.setHireDate(LocalDate.of(2021, 2, 2));
        updated.setStatus("ON_LEAVE");
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(null);
        Employee result = employeeService.updateEmployee(1L, updated);
        assertEquals("Jane Smith", result.getName());
        assertEquals("SUPERVISOR", result.getRole());
        assertEquals("Packing", result.getDepartment());
        assertEquals("B", result.getShiftGroup());
        assertEquals(LocalDate.of(2021, 2, 2), result.getHireDate());
        assertEquals("ON_LEAVE", result.getStatus());
        verify(auditLogRepository, times(1)).save(any(AuditLog.class));
    }

    @Test
    void testUpdateEmployee_NotFound() {
        EmployeeRepository employeeRepository = mock(EmployeeRepository.class);
        AuditLogRepository auditLogRepository = mock(AuditLogRepository.class);
        EmployeeService employeeService = new EmployeeService();
        employeeService.employeeRepository = employeeRepository;
        employeeService.auditLogRepository = auditLogRepository;
        Employee updated = new Employee("B123", "Jane Smith", "SUPERVISOR");
        when(employeeRepository.findById(2L)).thenReturn(Optional.empty());
        RuntimeException ex = assertThrows(RuntimeException.class, () -> employeeService.updateEmployee(2L, updated));
        assertTrue(ex.getMessage().contains("Employee not found"));
    }

    @Test
    void testSoftDeleteEmployee_Normal() {
        EmployeeRepository employeeRepository = mock(EmployeeRepository.class);
        AuditLogRepository auditLogRepository = mock(AuditLogRepository.class);
        EmployeeService employeeService = new EmployeeService();
        employeeService.employeeRepository = employeeRepository;
        employeeService.auditLogRepository = auditLogRepository;
        Employee employee = new Employee("B123", "John Doe", "WORKER");
        employee.setId(1L);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(null);
        employeeService.softDeleteEmployee(1L);
        assertTrue(employee.isDeleted());
        assertEquals("TERMINATED", employee.getStatus());
        verify(auditLogRepository, times(1)).save(any(AuditLog.class));
    }

    @Test
    void testSoftDeleteEmployee_NotFound() {
        EmployeeRepository employeeRepository = mock(EmployeeRepository.class);
        AuditLogRepository auditLogRepository = mock(AuditLogRepository.class);
        EmployeeService employeeService = new EmployeeService();
        employeeService.employeeRepository = employeeRepository;
        employeeService.auditLogRepository = auditLogRepository;
        when(employeeRepository.findById(2L)).thenReturn(Optional.empty());
        RuntimeException ex = assertThrows(RuntimeException.class, () -> employeeService.softDeleteEmployee(2L));
        assertTrue(ex.getMessage().contains("Employee not found"));
    }

    @Test
    void testGetEmployeesByDepartment() {
        EmployeeRepository employeeRepository = mock(EmployeeRepository.class);
        AuditLogRepository auditLogRepository = mock(AuditLogRepository.class);
        EmployeeService employeeService = new EmployeeService();
        employeeService.employeeRepository = employeeRepository;
        employeeService.auditLogRepository = auditLogRepository;
        Employee employee = new Employee("B123", "John Doe", "WORKER");
        employee.setDepartment("Logistics");
        when(employeeRepository.findByDepartment("Logistics")).thenReturn(List.of(employee));
        List<Employee> result = employeeService.getEmployeesByDepartment("Logistics");
        assertEquals(1, result.size());
        assertEquals("Logistics", result.get(0).getDepartment());
    }

    @Test
    void testGetEmployeesByRole() {
        EmployeeRepository employeeRepository = mock(EmployeeRepository.class);
        AuditLogRepository auditLogRepository = mock(AuditLogRepository.class);
        EmployeeService employeeService = new EmployeeService();
        employeeService.employeeRepository = employeeRepository;
        employeeService.auditLogRepository = auditLogRepository;
        Employee employee = new Employee("B123", "John Doe", "WORKER");
        when(employeeRepository.findByRole("WORKER")).thenReturn(List.of(employee));
        List<Employee> result = employeeService.getEmployeesByRole("WORKER");
        assertEquals(1, result.size());
        assertEquals("WORKER", result.get(0).getRole());
    }

    @Test
    void testCreateEmployee_NullBadgeId() {
        EmployeeRepository employeeRepository = mock(EmployeeRepository.class);
        AuditLogRepository auditLogRepository = mock(AuditLogRepository.class);
        EmployeeService employeeService = new EmployeeService();
        employeeService.employeeRepository = employeeRepository;
        employeeService.auditLogRepository = auditLogRepository;
        Employee emp = new Employee(null, "Name", "Role");
        when(employeeRepository.findByBadgeId(null)).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenReturn(emp);
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(null);
        Employee created = employeeService.createEmployee(emp);
        assertNull(created.getBadgeId());
    }

    @Test
    void testUpdateEmployee_NullFields() {
        EmployeeRepository employeeRepository = mock(EmployeeRepository.class);
        AuditLogRepository auditLogRepository = mock(AuditLogRepository.class);
        EmployeeService employeeService = new EmployeeService();
        employeeService.employeeRepository = employeeRepository;
        employeeService.auditLogRepository = auditLogRepository;
        Employee employee = new Employee("B123", "John Doe", "WORKER");
        employee.setId(1L);
        Employee updated = new Employee(null, null, null);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(null);
        Employee result = employeeService.updateEmployee(1L, updated);
        assertNull(result.getName());
        assertNull(result.getRole());
    }
}
