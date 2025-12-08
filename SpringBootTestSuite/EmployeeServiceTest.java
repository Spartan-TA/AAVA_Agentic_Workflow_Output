package com.warehouse.ems.employee.service;

import com.warehouse.ems.employee.domain.Employee;
import com.warehouse.ems.employee.domain.Role;
import com.warehouse.ems.employee.domain.Status;
import com.warehouse.ems.employee.dto.EmployeeRequestDTO;
import com.warehouse.ems.employee.dto.EmployeeResponseDTO;
import com.warehouse.ems.employee.exception.DuplicateBadgeIdException;
import com.warehouse.ems.employee.exception.ResourceNotFoundException;
import com.warehouse.ems.employee.repository.EmployeeRepository;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class EmployeeServiceTest {
    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    private EmployeeRequestDTO validRequestDTO;
    private Employee employee;
    private EmployeeResponseDTO responseDTO;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        validRequestDTO = new EmployeeRequestDTO();
        validRequestDTO.setName("John Doe");
        validRequestDTO.setBadgeId("BADGE123");
        validRequestDTO.setRole(Role.WORKER);
        validRequestDTO.setDepartment("Logistics");
        validRequestDTO.setShiftGroup("A");
        validRequestDTO.setHireDate(LocalDate.now());
        validRequestDTO.setStatus(Status.ACTIVE);

        employee = new Employee();
        employee.setId(1L);
        employee.setName("John Doe");
        employee.setBadgeId("BADGE123");
        employee.setRole(Role.WORKER);
        employee.setDepartment("Logistics");
        employee.setShiftGroup("A");
        employee.setHireDate(LocalDate.now());
        employee.setStatus(Status.ACTIVE);

        responseDTO = new EmployeeResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setName("John Doe");
        responseDTO.setBadgeId("BADGE123");
        responseDTO.setRole(Role.WORKER);
        responseDTO.setDepartment("Logistics");
        responseDTO.setShiftGroup("A");
        responseDTO.setHireDate(LocalDate.now());
        responseDTO.setStatus(Status.ACTIVE);
    }

    @Test
    public void testCreateEmployee_ValidInput_Success() {
        when(employeeRepository.findByBadgeId("BADGE123")).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);
        EmployeeResponseDTO result = employeeService.createEmployee(validRequestDTO);
        assertNotNull(result);
        assertEquals("John Doe", result.getName());
        assertEquals("BADGE123", result.getBadgeId());
    }

    @Test
    public void testCreateEmployee_NullInput_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> employeeService.createEmployee(null));
    }

    @Test
    public void testCreateEmployee_EmptyName_ThrowsException() {
        validRequestDTO.setName("");
        assertThrows(IllegalArgumentException.class, () -> employeeService.createEmployee(validRequestDTO));
    }

    @Test
    public void testCreateEmployee_DuplicateBadgeId_ThrowsException() {
        when(employeeRepository.findByBadgeId("BADGE123")).thenReturn(Optional.of(employee));
        assertThrows(DuplicateBadgeIdException.class, () -> employeeService.createEmployee(validRequestDTO));
    }

    @Test
    public void testUpdateEmployee_ValidInput_Success() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);
        EmployeeResponseDTO result = employeeService.updateEmployee(1L, validRequestDTO);
        assertNotNull(result);
        assertEquals("John Doe", result.getName());
    }

    @Test
    public void testUpdateEmployee_NullInput_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> employeeService.updateEmployee(1L, null));
    }

    @Test
    public void testUpdateEmployee_EmptyBadgeId_ThrowsException() {
        validRequestDTO.setBadgeId("");
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        assertThrows(IllegalArgumentException.class, () -> employeeService.updateEmployee(1L, validRequestDTO));
    }

    @Test
    public void testUpdateEmployee_NotFound_ThrowsException() {
        when(employeeRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> employeeService.updateEmployee(2L, validRequestDTO));
    }

    @Test
    public void testDeleteEmployee_SoftDelete_Success() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        employeeService.deleteEmployee(1L);
        verify(employeeRepository, times(1)).save(any(Employee.class));
        assertEquals(Status.INACTIVE, employee.getStatus());
    }

    @Test
    public void testDeleteEmployee_NotFound_ThrowsException() {
        when(employeeRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> employeeService.deleteEmployee(2L));
    }

    @Test
    public void testGetEmployee_ValidId_Success() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        EmployeeResponseDTO result = employeeService.getEmployee(1L);
        assertNotNull(result);
        assertEquals("John Doe", result.getName());
    }

    @Test
    public void testGetEmployee_NotFound_ThrowsException() {
        when(employeeRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> employeeService.getEmployee(2L));
    }

    @Test
    public void testListEmployees_Pagination_Success() {
        List<Employee> employees = Arrays.asList(employee);
        Page<Employee> page = new PageImpl<>(employees);
        when(employeeRepository.findAll(any(Pageable.class))).thenReturn(page);
        Page<EmployeeResponseDTO> result = employeeService.listEmployees(PageRequest.of(0, 10), null, null);
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    public void testListEmployees_FilterByDepartment_Success() {
        List<Employee> employees = Arrays.asList(employee);
        Page<Employee> page = new PageImpl<>(employees);
        when(employeeRepository.findByDepartment(eq("Logistics"))).thenReturn(employees);
        Page<EmployeeResponseDTO> result = employeeService.listEmployees(PageRequest.of(0, 10), "Logistics", null);
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    public void testListEmployees_FilterByStatus_Success() {
        List<Employee> employees = Arrays.asList(employee);
        Page<Employee> page = new PageImpl<>(employees);
        when(employeeRepository.findByStatus(eq(Status.ACTIVE))).thenReturn(employees);
        Page<EmployeeResponseDTO> result = employeeService.listEmployees(PageRequest.of(0, 10), null, Status.ACTIVE);
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }
}
