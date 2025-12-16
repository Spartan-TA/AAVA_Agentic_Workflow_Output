package com.warehouse.employee.management;

import com.warehouse.employee.management.domain.Employee;
import com.warehouse.employee.management.dto.EmployeeRequestDto;
import com.warehouse.employee.management.dto.EmployeeResponseDto;
import com.warehouse.employee.management.exception.ResourceNotFoundException;
import com.warehouse.employee.management.repository.EmployeeRepository;
import com.warehouse.employee.management.service.EmployeeService;
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
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
public class EmployeeServiceTest {
    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeService employeeService;

    private Employee employee;
    private EmployeeRequestDto employeeRequestDto;

    @BeforeEach
    public void setUp() {
        employee = new Employee();
        employee.setId(1L);
        employee.setName("John Doe");
        employee.setBadgeId("BADGE123");
        employee.setRole("WORKER");
        employee.setDepartment("Logistics");
        employee.setShiftGroup("A");
        employee.setHireDate(LocalDate.of(2022, 1, 1));
        employee.setStatus("ACTIVE");
        employee.setCreatedAt(LocalDateTime.now());
        employee.setUpdatedAt(LocalDateTime.now());

        employeeRequestDto = new EmployeeRequestDto();
        employeeRequestDto.setName("John Doe");
        employeeRequestDto.setBadgeId("BADGE123");
        employeeRequestDto.setRole("WORKER");
        employeeRequestDto.setDepartment("Logistics");
        employeeRequestDto.setShiftGroup("A");
        employeeRequestDto.setHireDate(LocalDate.of(2022, 1, 1));
    }

    @Test
    public void testCreateEmployee_WithValidData_ShouldReturnCreatedEmployee() {
        Mockito.when(employeeRepository.findByBadgeId(anyString())).thenReturn(Optional.empty());
        Mockito.when(employeeRepository.save(any(Employee.class))).thenReturn(employee);
        EmployeeResponseDto response = employeeService.createEmployee(employeeRequestDto);
        assertNotNull(response);
        assertEquals("John Doe", response.getName());
        assertEquals("BADGE123", response.getBadgeId());
    }

    @Test
    public void testCreateEmployee_WithDuplicateBadgeId_ShouldThrowException() {
        Mockito.when(employeeRepository.findByBadgeId(anyString())).thenReturn(Optional.of(employee));
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            employeeService.createEmployee(employeeRequestDto);
        });
        assertTrue(exception.getMessage().contains("Badge ID already exists"));
    }

    @Test
    public void testCreateEmployee_WithNullFields_ShouldThrowException() {
        EmployeeRequestDto invalidDto = new EmployeeRequestDto();
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            employeeService.createEmployee(invalidDto);
        });
        assertTrue(exception.getMessage().contains("Validation failed"));
    }

    @Test
    public void testUpdateEmployee_WithValidData_ShouldReturnUpdatedEmployee() {
        Mockito.when(employeeRepository.findById(anyLong())).thenReturn(Optional.of(employee));
        Mockito.when(employeeRepository.save(any(Employee.class))).thenReturn(employee);
        EmployeeResponseDto response = employeeService.updateEmployee(1L, employeeRequestDto);
        assertNotNull(response);
        assertEquals("John Doe", response.getName());
    }

    @Test
    public void testUpdateEmployee_WithNonExistentId_ShouldThrowResourceNotFoundException() {
        Mockito.when(employeeRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> {
            employeeService.updateEmployee(99L, employeeRequestDto);
        });
    }

    @Test
    public void testGetEmployeeById_WithValidId_ShouldReturnEmployee() {
        Mockito.when(employeeRepository.findById(anyLong())).thenReturn(Optional.of(employee));
        EmployeeResponseDto response = employeeService.getEmployeeById(1L);
        assertNotNull(response);
        assertEquals("John Doe", response.getName());
    }

    @Test
    public void testGetEmployeeById_WithNonExistentId_ShouldThrowResourceNotFoundException() {
        Mockito.when(employeeRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> {
            employeeService.getEmployeeById(99L);
        });
    }

    @Test
    public void testGetAllEmployees_WithPagination_ShouldReturnPageOfEmployees() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> page = new PageImpl<>(Collections.singletonList(employee));
        Mockito.when(employeeRepository.findAll(pageable)).thenReturn(page);
        Page<EmployeeResponseDto> result = employeeService.getAllEmployees(pageable);
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    public void testSoftDeleteEmployee_ShouldSetStatusInactive() {
        Mockito.when(employeeRepository.findById(anyLong())).thenReturn(Optional.of(employee));
        Mockito.when(employeeRepository.save(any(Employee.class))).thenReturn(employee);
        employeeService.softDeleteEmployee(1L);
        assertEquals("INACTIVE", employee.getStatus());
    }

    @Test
    public void testFilterByStatus_WithValidStatus_ShouldReturnEmployees() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> page = new PageImpl<>(Collections.singletonList(employee));
        Mockito.when(employeeRepository.findByStatus(eq("ACTIVE"), eq(pageable))).thenReturn(page);
        Page<EmployeeResponseDto> result = employeeService.filterByStatus("ACTIVE", pageable);
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    public void testFilterByStatus_WithNullStatus_ShouldReturnEmptyPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> page = new PageImpl<>(Collections.emptyList());
        Mockito.when(employeeRepository.findByStatus(isNull(), eq(pageable))).thenReturn(page);
        Page<EmployeeResponseDto> result = employeeService.filterByStatus(null, pageable);
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
    }

    @Test
    public void testFilterByDepartment_WithValidDepartment_ShouldReturnEmployees() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> page = new PageImpl<>(Collections.singletonList(employee));
        Mockito.when(employeeRepository.findByDepartment(eq("Logistics"), eq(pageable))).thenReturn(page);
        Page<EmployeeResponseDto> result = employeeService.filterByDepartment("Logistics", pageable);
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    public void testFilterByDepartment_WithNullDepartment_ShouldReturnEmptyPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> page = new PageImpl<>(Collections.emptyList());
        Mockito.when(employeeRepository.findByDepartment(isNull(), eq(pageable))).thenReturn(page);
        Page<EmployeeResponseDto> result = employeeService.filterByDepartment(null, pageable);
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
    }
}
