package com.wems.employee.service;

import com.wems.common.exception.ResourceNotFoundException;
import com.wems.employee.domain.Employee;
import com.wems.employee.dto.EmployeeDTO;
import com.wems.employee.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {
    @Mock
    private EmployeeRepository employeeRepository;
    @InjectMocks
    private EmployeeService employeeService;

    private EmployeeDTO employeeDTO;
    private Employee employee;

    @BeforeEach
    void setUp() {
        employeeDTO = new EmployeeDTO();
        employeeDTO.setName("John Doe");
        employeeDTO.setEmail("john.doe@example.com");
        employeeDTO.setBadgeId("BADGE123");
        employeeDTO.setRole("WORKER");

        employee = new Employee();
        employee.setId(1L);
        employee.setName("John Doe");
        employee.setEmail("john.doe@example.com");
        employee.setBadgeId("BADGE123");
        employee.setRole("WORKER");
        employee.setActive(true);
    }

    @Test
    void createEmployee_Success() {
        when(employeeRepository.findByBadgeIdAndActiveTrue(anyString())).thenReturn(null);
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);
        Employee result = employeeService.createEmployee(employeeDTO);
        assertNotNull(result);
        assertEquals(employee.getName(), result.getName());
        verify(employeeRepository).save(any(Employee.class));
    }

    @Test
    void createEmployee_DuplicateBadgeId_ThrowsException() {
        when(employeeRepository.findByBadgeIdAndActiveTrue(anyString())).thenReturn(employee);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                employeeService.createEmployee(employeeDTO));
        assertEquals("Badge ID must be unique.", ex.getMessage());
        verify(employeeRepository, never()).save(any());
    }

    @Test
    void getActiveEmployees_ReturnsPage() {
        Page<Employee> page = new PageImpl<>(Collections.singletonList(employee));
        when(employeeRepository.findAllActive(any(Pageable.class))).thenReturn(page);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> result = employeeService.getActiveEmployees(pageable);
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(employeeRepository).findAllActive(pageable);
    }

    @Test
    void updateEmployee_Success() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);
        employeeDTO.setName("Jane Smith");
        employeeDTO.setEmail("jane.smith@example.com");
        employeeDTO.setBadgeId("BADGE456");
        employeeDTO.setRole("SUPERVISOR");
        Employee result = employeeService.updateEmployee(1L, employeeDTO);
        assertEquals("Jane Smith", result.getName());
        assertEquals("jane.smith@example.com", result.getEmail());
        assertEquals("BADGE456", result.getBadgeId());
        assertEquals("SUPERVISOR", result.getRole());
        verify(employeeRepository).save(employee);
    }

    @Test
    void updateEmployee_NotFound_ThrowsException() {
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.empty());
        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () ->
                employeeService.updateEmployee(99L, employeeDTO));
        assertEquals("Employee not found: 99", ex.getMessage());
        verify(employeeRepository, never()).save(any());
    }

    @Test
    void softDeleteEmployee_Success() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);
        employeeService.softDeleteEmployee(1L);
        assertFalse(employee.isActive());
        verify(employeeRepository).save(employee);
    }

    @Test
    void softDeleteEmployee_NotFound_ThrowsException() {
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.empty());
        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () ->
                employeeService.softDeleteEmployee(99L));
        assertEquals("Employee not found: 99", ex.getMessage());
        verify(employeeRepository, never()).save(any());
    }

    @Test
    void getEmployeeByBadgeId_Success() {
        when(employeeRepository.findByBadgeIdAndActiveTrue("BADGE123")).thenReturn(employee);
        Employee result = employeeService.getEmployeeByBadgeId("BADGE123");
        assertNotNull(result);
        assertEquals("BADGE123", result.getBadgeId());
    }

    @Test
    void getEmployeeByBadgeId_NotFound_ThrowsException() {
        when(employeeRepository.findByBadgeIdAndActiveTrue("BADGE999")).thenReturn(null);
        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () ->
                employeeService.getEmployeeByBadgeId("BADGE999"));
        assertEquals("Employee not found with badgeId: BADGE999", ex.getMessage());
    }

    @Test
    void createEmployee_NullDTO_ThrowsException() {
        assertThrows(NullPointerException.class, () -> employeeService.createEmployee(null));
    }

    @Test
    void updateEmployee_NullDTO_ThrowsException() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        assertThrows(NullPointerException.class, () -> employeeService.updateEmployee(1L, null));
    }

    @Test
    void getEmployeeByBadgeId_NullBadgeId_ThrowsException() {
        assertThrows(ResourceNotFoundException.class, () -> employeeService.getEmployeeByBadgeId(null));
    }
}
