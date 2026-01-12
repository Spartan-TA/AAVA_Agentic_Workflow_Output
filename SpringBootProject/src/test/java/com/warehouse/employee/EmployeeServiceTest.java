package com.warehouse.employee;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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
        employee = Employee.builder()
                .id(1L)
                .badgeId("B123")
                .name("John Doe")
                .role("WORKER")
                .department("Shipping")
                .shiftGroup("A")
                .hireDate(LocalDate.now())
                .status("ACTIVE")
                .deleted(false)
                .build();
    }

    @Test
    void createEmployee_success() {
        when(employeeRepository.existsByBadgeIdAndDeletedFalse("B123")).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);
        Employee created = employeeService.createEmployee(employee);
        assertEquals("B123", created.getBadgeId());
    }

    @Test
    void createEmployee_duplicateBadgeId_throwsException() {
        when(employeeRepository.existsByBadgeIdAndDeletedFalse("B123")).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> employeeService.createEmployee(employee));
    }

    @Test
    void getAllEmployees_returnsPage() {
        Pageable pageable = PageRequest.of(0, 10);
        when(employeeRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(employee)));
        assertFalse(employeeService.getAllEmployees(pageable).isEmpty());
    }

    @Test
    void getByBadgeId_found() {
        when(employeeRepository.findByBadgeIdAndDeletedFalse("B123")).thenReturn(Optional.of(employee));
        Optional<Employee> found = employeeService.getByBadgeId("B123");
        assertTrue(found.isPresent());
    }

    @Test
    void updateEmployee_success() {
        Employee updated = Employee.builder().name("Jane").build();
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);
        Employee result = employeeService.updateEmployee(1L, updated);
        assertEquals("Jane", result.getName());
    }

    @Test
    void deleteEmployee_setsDeletedTrue() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);
        employeeService.deleteEmployee(1L);
        assertTrue(employee.getDeleted());
    }
}
