package com.warehouse.employee.management.service;

import com.warehouse.employee.management.domain.Employee;
import com.warehouse.employee.management.dto.EmployeeDTO;
import com.warehouse.employee.management.repository.EmployeeRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmployeeServiceTest {

    @Mock
    EmployeeRepository employeeRepository;

    @InjectMocks
    EmployeeService employeeService;

    public EmployeeServiceTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateEmployee() {
        EmployeeDTO dto = new EmployeeDTO();
        dto.setBadgeId("B123");
        dto.setName("John Doe");
        dto.setRole("WORKER");
        dto.setDepartment("Shipping");
        dto.setHireDate(LocalDate.now());
        dto.setStatus("ACTIVE");

        Employee saved = Employee.builder()
                .id(1L)
                .badgeId(dto.getBadgeId())
                .name(dto.getName())
                .role(dto.getRole())
                .department(dto.getDepartment())
                .hireDate(dto.getHireDate())
                .status(dto.getStatus())
                .deleted(false)
                .build();

        when(employeeRepository.save(any(Employee.class))).thenReturn(saved);

        Employee result = employeeService.createEmployee(dto);
        assertEquals("John Doe", result.getName());
        assertEquals("B123", result.getBadgeId());
    }

    @Test
    void testGetAllEmployees() {
        Employee emp = Employee.builder().id(1L).badgeId("B1").name("A").role("WORKER").status("ACTIVE").deleted(false).build();
        when(employeeRepository.findAllByDeletedFalse(any())).thenReturn(new PageImpl<>(List.of(emp)));
        var page = employeeService.getAllEmployees(PageRequest.of(0, 10));
        assertEquals(1, page.getTotalElements());
    }

    @Test
    void testGetEmployeeNotFound() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> employeeService.getEmployee(1L));
    }
}