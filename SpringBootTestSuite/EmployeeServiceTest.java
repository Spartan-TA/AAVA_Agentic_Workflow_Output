package com.warehouse.employee.service;

import com.warehouse.employee.entity.Employee;
import com.warehouse.employee.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmployeeServiceTest {
    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeService employeeService;

    private Employee employee;

    @BeforeEach
    void setUp() {
        employee = new Employee();
        employee.setId(1L);
        employee.setName("John Doe");
        employee.setDeleted(false);
    }

    @Test
    void testCreateEmployee_ValidInput() {
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);
        Employee result = employeeService.createEmployee(employee);
        assertNotNull(result);
        assertEquals("John Doe", result.getName());
    }

    @Test
    void testCreateEmployee_NullInput() {
        when(employeeRepository.save(null)).thenThrow(new IllegalArgumentException("Employee is null"));
        assertThrows(IllegalArgumentException.class, () -> employeeService.createEmployee(null));
    }

    @Test
    void testGetAllEmployees_NormalCase() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> page = new PageImpl<>(Arrays.asList(employee));
        when(employeeRepository.findAllByDeletedFalse(pageable)).thenReturn(page);
        Page<Employee> result = employeeService.getAllEmployees(pageable);
        assertEquals(1, result.getTotalElements());
        assertEquals("John Doe", result.getContent().get(0).getName());
    }

    @Test
    void testGetAllEmployees_EmptyPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> page = new PageImpl<>(Arrays.asList());
        when(employeeRepository.findAllByDeletedFalse(pageable)).thenReturn(page);
        Page<Employee> result = employeeService.getAllEmployees(pageable);
        assertEquals(0, result.getTotalElements());
    }

    @Test
    void testSoftDeleteEmployee_ValidId() {
        Employee notDeletedEmployee = new Employee();
        notDeletedEmployee.setId(2L);
        notDeletedEmployee.setDeleted(false);
        when(employeeRepository.findById(2L)).thenReturn(Optional.of(notDeletedEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(notDeletedEmployee);
        employeeService.softDeleteEmployee(2L);
        assertTrue(notDeletedEmployee.isDeleted());
    }

    @Test
    void testSoftDeleteEmployee_InvalidId() {
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());
        RuntimeException ex = assertThrows(RuntimeException.class, () -> employeeService.softDeleteEmployee(999L));
        assertEquals("Employee not found", ex.getMessage());
    }

    @Test
    void testSoftDeleteEmployee_NullId() {
        when(employeeRepository.findById(null)).thenThrow(new IllegalArgumentException("ID is null"));
        assertThrows(IllegalArgumentException.class, () -> employeeService.softDeleteEmployee(null));
    }

    @Test
    void testCreateEmployee_BoundaryCase_EmptyName() {
        Employee emptyNameEmployee = new Employee();
        emptyNameEmployee.setId(3L);
        emptyNameEmployee.setName("");
        when(employeeRepository.save(any(Employee.class))).thenReturn(emptyNameEmployee);
        Employee result = employeeService.createEmployee(emptyNameEmployee);
        assertEquals("", result.getName());
    }

    @Test
    void testCreateEmployee_BoundaryCase_LongName() {
        String longName = "A".repeat(255);
        Employee longNameEmployee = new Employee();
        longNameEmployee.setId(4L);
        longNameEmployee.setName(longName);
        when(employeeRepository.save(any(Employee.class))).thenReturn(longNameEmployee);
        Employee result = employeeService.createEmployee(longNameEmployee);
        assertEquals(longName, result.getName());
    }
}
