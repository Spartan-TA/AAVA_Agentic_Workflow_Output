package com.example.warehouse.employee;

import com.example.warehouse.employee.dto.EmployeeDTO;
import com.example.warehouse.employee.entity.Employee;
import com.example.warehouse.employee.repository.EmployeeRepository;
import com.example.warehouse.employee.service.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
class EmployeeServiceTest_Part1 {
    @Autowired
    private EmployeeService employeeService;

    @MockBean
    private EmployeeRepository employeeRepository;

    private EmployeeDTO validDto;
    private Employee validEmployee;

    @BeforeEach
    void setUp() {
        validDto = new EmployeeDTO();
        validDto.setName("John Doe");
        validDto.setBadgeId("EMP001");
        validDto.setRole("WORKER");
        validDto.setDepartment("Receiving");
        validDto.setShiftGroup("A");
        validDto.setHireDate(LocalDate.now().minusYears(1));
        validDto.setStatus("ACTIVE");

        validEmployee = new Employee(1L, "John Doe", "EMP001", "WORKER", "Receiving", "A", LocalDate.now().minusYears(1), "ACTIVE", false);
    }

    @Test
    @DisplayName("Test create employee with valid data")
    void testCreateEmployeeValid() {
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);
        EmployeeDTO result = employeeService.createEmployee(validDto);
        assertNotNull(result);
        assertEquals("John Doe", result.getName());
        assertEquals("EMP001", result.getBadgeId());
    }

    @Test
    @DisplayName("Test create employee with null name throws exception")
    void testCreateEmployeeNullName() {
        validDto.setName(null);
        assertThrows(IllegalArgumentException.class, () -> employeeService.createEmployee(validDto));
    }

    @Test
    @DisplayName("Test create employee with empty badgeId throws exception")
    void testCreateEmployeeEmptyBadgeId() {
        validDto.setBadgeId("");
        assertThrows(IllegalArgumentException.class, () -> employeeService.createEmployee(validDto));
    }

    @Test
    @DisplayName("Test update employee with valid data")
    void testUpdateEmployeeValid() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);
        validDto.setName("Jane Doe");
        EmployeeDTO result = employeeService.updateEmployee(1L, validDto);
        assertNotNull(result);
        assertEquals("Jane Doe", result.getName());
    }

    @Test
    @DisplayName("Test update employee with invalid id throws exception")
    void testUpdateEmployeeInvalidId() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> employeeService.updateEmployee(99L, validDto));
    }
}