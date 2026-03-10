package com.warehouse.ems.service;

import com.warehouse.ems.domain.entity.Employee;
import com.warehouse.ems.dto.EmployeeDto;
import com.warehouse.ems.exception.EmployeeNotFoundException;
import com.warehouse.ems.repository.EmployeeRepository;
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

import java.time.LocalDate;
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

    private EmployeeDto validDto;
    private Employee validEmployee;

    @BeforeEach
    void setUp() {
        validDto = new EmployeeDto();
        validDto.setBadgeId("BADGE123");
        validDto.setName("John Doe");
        validDto.setRole("Worker");
        validDto.setDepartment("Logistics");
        validDto.setShiftGroup("A");
        validDto.setHireDate(LocalDate.of(2022, 1, 1));

        validEmployee = new Employee();
        validEmployee.setId(1L);
        validEmployee.setBadgeId("BADGE123");
        validEmployee.setName("John Doe");
        validEmployee.setRole("Worker");
        validEmployee.setDepartment("Logistics");
        validEmployee.setShiftGroup("A");
        validEmployee.setHireDate(LocalDate.of(2022, 1, 1));
        validEmployee.setStatus("ACTIVE");
    }

    @Test
    void testCreateEmployee_ValidInput_Success() {
        when(employeeRepository.existsByBadgeId("BADGE123")).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);

        Employee result = employeeService.createEmployee(validDto);
        assertNotNull(result);
        assertEquals("BADGE123", result.getBadgeId());
        assertEquals("ACTIVE", result.getStatus());
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    void testCreateEmployee_NullDto_ThrowsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            employeeService.createEmployee(null);
        });
        assertEquals("Employee DTO cannot be null", ex.getMessage());
    }

    @Test
    void testCreateEmployee_NullBadgeId_ThrowsException() {
        EmployeeDto dto = new EmployeeDto();
        dto.setBadgeId(null);
        dto.setName("Jane");
        dto.setRole("Worker");
        dto.setDepartment("Logistics");
        dto.setHireDate(LocalDate.now());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            employeeService.createEmployee(dto);
        });
        assertEquals("Badge ID cannot be null or empty", ex.getMessage());
    }

    @Test
    void testCreateEmployee_EmptyBadgeId_ThrowsException() {
        EmployeeDto dto = new EmployeeDto();
        dto.setBadgeId("   ");
        dto.setName("Jane");
        dto.setRole("Worker");
        dto.setDepartment("Logistics");
        dto.setHireDate(LocalDate.now());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            employeeService.createEmployee(dto);
        });
        assertEquals("Badge ID cannot be null or empty", ex.getMessage());
    }

    @Test
    void testCreateEmployee_DuplicateBadgeId_ThrowsException() {
        when(employeeRepository.existsByBadgeId("BADGE123")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            employeeService.createEmployee(validDto);
        });
        assertTrue(ex.getMessage().contains("already exists"));
    }

    @Test
    void testGetEmployee_ValidId_ReturnsEmployee() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));

        Employee result = employeeService.getEmployee(1L);
        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void testGetEmployee_NullId_ThrowsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            employeeService.getEmployee(null);
        });
        assertEquals("Employee ID must be positive", ex.getMessage());
    }

    @Test
    void testGetEmployee_NegativeId_ThrowsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            employeeService.getEmployee(-5L);
        });
        assertEquals("Employee ID must be positive", ex.getMessage());
    }

    @Test
    void testGetEmployee_NonExistentId_ThrowsNotFoundException() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        EmployeeNotFoundException ex = assertThrows(EmployeeNotFoundException.class, () -> {
            employeeService.getEmployee(99L);
        });
        assertTrue(ex.getMessage().contains("Employee not found"));
    }

    @Test
    void testListEmployees_ByDepartment_ReturnsPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> page = new PageImpl<>(Arrays.asList(validEmployee));
        when(employeeRepository.findByDepartment(eq("Logistics"), eq(pageable))).thenReturn(page);

        Page<Employee> result = employeeService.listEmployees("Logistics", null, pageable);
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void testListEmployees_ByStatus_ReturnsPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> page = new PageImpl<>(Arrays.asList(validEmployee));
        when(employeeRepository.findByStatus(eq("ACTIVE"), eq(pageable))).thenReturn(page);

        Page<Employee> result = employeeService.listEmployees(null, "ACTIVE", pageable);
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void testListEmployees_All_ReturnsPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> page = new PageImpl<>(Arrays.asList(validEmployee));
        when(employeeRepository.findAll(eq(pageable))).thenReturn(page);

        Page<Employee> result = employeeService.listEmployees(null, null, pageable);
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void testListEmployees_NullPageable_ThrowsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            employeeService.listEmployees(null, null, null);
        });
        assertEquals("Pageable cannot be null", ex.getMessage());
    }

    @Test
    void testUpdateEmployee_ValidInput_Success() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);

        EmployeeDto updateDto = new EmployeeDto();
        updateDto.setName("Jane Doe");
        updateDto.setRole("Supervisor");
        updateDto.setDepartment("Packing");
        updateDto.setShiftGroup("B");
        updateDto.setHireDate(LocalDate.of(2023, 1, 1));
        updateDto.setBadgeId("BADGE123"); // badgeId is not updated

        Employee result = employeeService.updateEmployee(1L, updateDto);
        assertNotNull(result);
        assertEquals("Jane Doe", result.getName());
        assertEquals("Supervisor", result.getRole());
        assertEquals("Packing", result.getDepartment());
        assertEquals("B", result.getShiftGroup());
        assertEquals(LocalDate.of(2023, 1, 1), result.getHireDate());
    }

    @Test
    void testUpdateEmployee_NullId_ThrowsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            employeeService.updateEmployee(null, validDto);
        });
        assertEquals("Employee ID must be positive", ex.getMessage());
    }

    @Test
    void testUpdateEmployee_NegativeId_ThrowsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            employeeService.updateEmployee(-1L, validDto);
        });
        assertEquals("Employee ID must be positive", ex.getMessage());
    }

    @Test
    void testUpdateEmployee_NullDto_ThrowsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            employeeService.updateEmployee(1L, null);
        });
        assertEquals("Employee DTO cannot be null", ex.getMessage());
    }

    @Test
    void testUpdateEmployee_NonExistentId_ThrowsNotFoundException() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());
        EmployeeDto dto = new EmployeeDto();
        dto.setName("Jane Doe");
        dto.setRole("Supervisor");
        dto.setDepartment("Packing");
        dto.setHireDate(LocalDate.now());
        dto.setBadgeId("BADGE123");

        EmployeeNotFoundException ex = assertThrows(EmployeeNotFoundException.class, () -> {
            employeeService.updateEmployee(99L, dto);
        });
        assertTrue(ex.getMessage().contains("Employee not found"));
    }

    @Test
    void testDeleteEmployee_ValidId_SetsInactive() {
        Employee employee = new Employee();
        employee.setId(1L);
        employee.setStatus("ACTIVE");
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        employeeService.deleteEmployee(1L);
        assertEquals("INACTIVE", employee.getStatus());
        verify(employeeRepository, times(1)).save(employee);
    }

    @Test
    void testDeleteEmployee_NullId_ThrowsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            employeeService.deleteEmployee(null);
        });
        assertEquals("Employee ID must be positive", ex.getMessage());
    }

    @Test
    void testDeleteEmployee_NegativeId_ThrowsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            employeeService.deleteEmployee(-1L);
        });
        assertEquals("Employee ID must be positive", ex.getMessage());
    }

    @Test
    void testDeleteEmployee_NonExistentId_ThrowsNotFoundException() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());
        EmployeeNotFoundException ex = assertThrows(EmployeeNotFoundException.class, () -> {
            employeeService.deleteEmployee(99L);
        });
        assertTrue(ex.getMessage().contains("Employee not found"));
    }
}
