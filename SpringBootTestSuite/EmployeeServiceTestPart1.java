package com.warehouse.ems.service;

import com.warehouse.ems.domain.Employee;
import com.warehouse.ems.domain.AuditLog;
import com.warehouse.ems.repository.EmployeeRepository;
import com.warehouse.ems.repository.AuditLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.time.LocalDate;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmployeeServiceTest {
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private AuditLogRepository auditLogRepository;
    @InjectMocks
    private EmployeeService employeeService;

    private Employee employee;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        employee = new Employee("B123", "John Doe", "WORKER");
        employee.setId(1L);
        employee.setDepartment("Logistics");
        employee.setHireDate(LocalDate.of(2020, 1, 1));
        employee.setStatus("ACTIVE");
    }

    @Test
    void testGetAllEmployees_Normal() {
        when(employeeRepository.findAllActive()).thenReturn(List.of(employee));
        List<Employee> result = employeeService.getAllEmployees();
        assertEquals(1, result.size());
        assertEquals("B123", result.get(0).getBadgeId());
    }

    @Test
    void testGetEmployees_Paged() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> page = new PageImpl<>(List.of(employee));
        when(employeeRepository.findAll(pageable)).thenReturn(page);
        Page<Employee> result = employeeService.getEmployees(pageable);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void testGetEmployeeById_Found() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        Optional<Employee> result = employeeService.getEmployeeById(1L);
        assertTrue(result.isPresent());
        assertEquals("B123", result.get().getBadgeId());
    }

    @Test
    void testGetEmployeeById_NotFound() {
        when(employeeRepository.findById(2L)).thenReturn(Optional.empty());
        Optional<Employee> result = employeeService.getEmployeeById(2L);
        assertFalse(result.isPresent());
    }

    @Test
    void testGetEmployeeByBadgeId_Found() {
        when(employeeRepository.findByBadgeId("B123")).thenReturn(Optional.of(employee));
        Optional<Employee> result = employeeService.getEmployeeByBadgeId("B123");
        assertTrue(result.isPresent());
    }

    @Test
    void testGetEmployeeByBadgeId_NotFound() {
        when(employeeRepository.findByBadgeId("B999")).thenReturn(Optional.empty());
        Optional<Employee> result = employeeService.getEmployeeByBadgeId("B999");
        assertFalse(result.isPresent());
    }

    @Test
    void testCreateEmployee_Normal() {
        when(employeeRepository.findByBadgeId("B123")).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(null);
        Employee created = employeeService.createEmployee(employee);
        assertEquals("B123", created.getBadgeId());
        verify(auditLogRepository, times(1)).save(any(AuditLog.class));
    }

    @Test
    void testCreateEmployee_DuplicateBadgeId() {
        when(employeeRepository.findByBadgeId("B123")).thenReturn(Optional.of(employee));
        RuntimeException ex = assertThrows(RuntimeException.class, () -> employeeService.createEmployee(employee));
        assertTrue(ex.getMessage().contains("Badge ID already exists"));
    }
