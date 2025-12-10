package com.warehouse.employee.service;

import com.warehouse.employee.dto.EmployeeDTO;
import com.warehouse.employee.model.Employee;
import com.warehouse.employee.repository.EmployeeRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * JUnit test stub for EmployeeService.
 */
class EmployeeServiceTest {
    @Mock
    private EmployeeRepository employeeRepository;
    @InjectMocks
    private EmployeeService employeeService;

    public EmployeeServiceTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllEmployees() {
        Pageable pageable = PageRequest.of(0, 10);
        when(employeeRepository.findAllByDeletedFalse(pageable))
                .thenReturn(new PageImpl<>(Collections.emptyList()));
        assertNotNull(employeeService.getAllEmployees(pageable, null));
    }

    @Test
    void testCreateEmployee() {
        EmployeeDTO dto = new EmployeeDTO();
        dto.setName("John Doe");
        dto.setBadgeId("B123");
        dto.setRole("WORKER");
        dto.setDepartment("Shipping");
        dto.setHireDate(LocalDate.now());
        dto.setStatus("ACTIVE");
        Employee emp = new Employee();
        emp.setId(1L);
        emp.setName(dto.getName());
        emp.setBadgeId(dto.getBadgeId());
        emp.setRole(dto.getRole());
        emp.setDepartment(dto.getDepartment());
        emp.setHireDate(dto.getHireDate());
        emp.setStatus(dto.getStatus());
        when(employeeRepository.save(any(Employee.class))).thenReturn(emp);
        EmployeeDTO created = employeeService.createEmployee(dto);
        assertEquals("John Doe", created.getName());
    }
}
