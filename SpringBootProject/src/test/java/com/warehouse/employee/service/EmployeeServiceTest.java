package com.warehouse.employee.service;

import com.warehouse.employee.domain.Employee;
import com.warehouse.employee.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmployeeServiceTest {
    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeService employeeService;

    private Employee employee;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        employee = new Employee();
        employee.setId(1L);
        employee.setBadgeId("B123");
        employee.setName("John Doe");
        employee.setRole("WORKER");
        employee.setDepartment("Shipping");
        employee.setShiftGroup("A");
        employee.setHireDate(LocalDate.now());
        employee.setStatus("ACTIVE");
        employee.setDeleted(false);
    }

    @Test
    void testCreateEmployee_UniqueBadgeId() {
        when(employeeRepository.findByBadgeIdAndDeletedFalse("B123")).thenReturn(Optional.empty());
        when(employeeRepository.save(employee)).thenReturn(employee);
        Employee created = employeeService.createEmployee(employee);
        assertEquals("B123", created.getBadgeId());
    }

    @Test
    void testCreateEmployee_DuplicateBadgeIdThrows() {
        when(employeeRepository.findByBadgeIdAndDeletedFalse("B123")).thenReturn(Optional.of(employee));
        assertThrows(IllegalArgumentException.class, () -> employeeService.createEmployee(employee));
    }

    @Test
    void testUpdateEmployee() {
        Employee updated = new Employee();
        updated.setName("Jane Doe");
        updated.setRole("SUPERVISOR");
        updated.setDepartment("Receiving");
        updated.setShiftGroup("B");
        updated.setHireDate(LocalDate.now());
        updated.setStatus("ACTIVE");
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(updated);
        Employee result = employeeService.updateEmployee(1L, updated);
        assertEquals("Jane Doe", result.getName());
        assertEquals("SUPERVISOR", result.getRole());
    }

    @Test
    void testSoftDeleteEmployee() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);
        employeeService.softDeleteEmployee(1L);
        assertTrue(employee.isDeleted());
    }

    @Test
    void testGetAllEmployees() {
        Page<Employee> page = new PageImpl<>(List.of(employee));
        when(employeeRepository.findAllByDeletedFalse(PageRequest.of(0, 10))).thenReturn(page);
        Page<Employee> result = employeeService.getAllEmployees(PageRequest.of(0, 10));
        assertEquals(1, result.getTotalElements());
    }
}
