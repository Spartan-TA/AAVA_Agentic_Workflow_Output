package com.wms.ems.employee.service;

import com.wms.ems.employee.domain.Employee;
import com.wms.ems.employee.dto.CreateEmployeeRequest;
import com.wms.ems.employee.dto.EmployeeDTO;
import com.wms.ems.employee.dto.UpdateEmployeeRequest;
import com.wms.ems.employee.exception.ResourceNotFoundException;
import com.wms.ems.employee.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

    private Employee employee;
    private UUID employeeId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        employeeId = UUID.randomUUID();
        employee = new Employee();
        employee.setId(employeeId);
        employee.setName("John Doe");
        employee.setBadgeId("BADGE12345");
        employee.setRole("WORKER");
        employee.setDepartment("Logistics");
        employee.setShiftGroup("A");
        employee.setHireDate(LocalDate.of(2022, 1, 1));
        employee.setStatus("ACTIVE");
        employee.setDeleted(false);
        employee.setCreatedAt(LocalDateTime.now());
        employee.setUpdatedAt(LocalDateTime.now());
    }

    // Helper for mapping
    private EmployeeDTO mapToDTO(Employee e) {
        EmployeeDTO dto = new EmployeeDTO();
        dto.setId(e.getId());
        dto.setName(e.getName());
        dto.setBadgeId(e.getBadgeId());
        dto.setRole(e.getRole());
        dto.setDepartment(e.getDepartment());
        dto.setShiftGroup(e.getShiftGroup());
        dto.setHireDate(e.getHireDate());
        dto.setStatus(e.getStatus());
        dto.setCreatedAt(e.getCreatedAt());
        dto.setUpdatedAt(e.getUpdatedAt());
        return dto;
    }

    @Test
    void testCreateEmployee_WithValidInput_ReturnsCreatedEmployee() {
        CreateEmployeeRequest request = new CreateEmployeeRequest();
        request.setName("Jane Smith");
        request.setBadgeId("BADGE54321");
        request.setRole("HR");
        request.setDepartment("HR");
        request.setShiftGroup("B");
        request.setHireDate(LocalDate.of(2023, 5, 10));
        request.setStatus("ACTIVE");

        when(employeeRepository.existsByBadgeIdAndNotDeleted("BADGE54321")).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> {
            Employee emp = invocation.getArgument(0);
            emp.setId(UUID.randomUUID());
            return emp;
        });

        EmployeeDTO result = employeeService.createEmployee(request);
        assertNotNull(result);
        assertEquals("Jane Smith", result.getName());
        assertEquals("BADGE54321", result.getBadgeId());
        verify(employeeRepository).save(any(Employee.class));
    }

    @Test
    void testCreateEmployee_WithDuplicateBadgeId_ThrowsIllegalArgumentException() {
        CreateEmployeeRequest request = new CreateEmployeeRequest();
        request.setName("Jane Smith");
        request.setBadgeId("BADGE12345");
        request.setRole("HR");
        request.setDepartment("HR");
        request.setHireDate(LocalDate.now());
        request.setStatus("ACTIVE");

        when(employeeRepository.existsByBadgeIdAndNotDeleted("BADGE12345")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            employeeService.createEmployee(request);
        });
        assertTrue(ex.getMessage().contains("Badge ID already exists"));
        verify(employeeRepository, never()).save(any());
    }

    @Test
    void testCreateEmployee_WithNullName_ThrowsException() {
        CreateEmployeeRequest request = new CreateEmployeeRequest();
        request.setName(null);
        request.setBadgeId("BADGE54321");
        request.setRole("HR");
        request.setDepartment("HR");
        request.setHireDate(LocalDate.now());
        request.setStatus("ACTIVE");

        when(employeeRepository.existsByBadgeIdAndNotDeleted(anyString())).thenReturn(false);
        // Simulate validation exception (would be handled by controller in real app)
        assertThrows(NullPointerException.class, () -> {
            employeeService.createEmployee(request);
        });
    }

    @Test
    void testGetEmployeeById_WithValidId_ReturnsEmployee() {
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
        EmployeeDTO dto = employeeService.getEmployeeById(employeeId);
        assertNotNull(dto);
        assertEquals(employeeId, dto.getId());
        verify(employeeRepository).findById(employeeId);
    }

    @Test
    void testGetEmployeeById_WithNonExistentId_ThrowsResourceNotFoundException() {
        UUID randomId = UUID.randomUUID();
        when(employeeRepository.findById(randomId)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> {
            employeeService.getEmployeeById(randomId);
        });
    }

    @Test
    void testGetEmployeeById_WithDeletedEmployee_ThrowsResourceNotFoundException() {
        Employee deletedEmp = new Employee();
        deletedEmp.setId(employeeId);
        deletedEmp.setDeleted(true);
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(deletedEmp));
        assertThrows(ResourceNotFoundException.class, () -> {
            employeeService.getEmployeeById(employeeId);
        });
    }

    @Test
    void testGetAllEmployees_WithPagination_ReturnsPageOfEmployees() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Employee> employees = Arrays.asList(employee);
        Page<Employee> page = new PageImpl<>(employees, pageable, 1);
        when(employeeRepository.findAllActive(pageable)).thenReturn(page);
        Page<EmployeeDTO> result = employeeService.getAllEmployees(pageable);
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(employeeRepository).findAllActive(pageable);
    }

    @Test
    void testGetAllEmployees_WithEmptyDatabase_ReturnsEmptyPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> page = new PageImpl<>(Collections.emptyList(), pageable, 0);
        when(employeeRepository.findAllActive(pageable)).thenReturn(page);
        Page<EmployeeDTO> result = employeeService.getAllEmployees(pageable);
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
    }

    @Test
    void testUpdateEmployee_WithValidData_ReturnsUpdatedEmployee() {
        UpdateEmployeeRequest request = new UpdateEmployeeRequest();
        request.setName("Updated Name");
        request.setBadgeId("BADGE99999");
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
        when(employeeRepository.existsByBadgeIdAndNotDeleted("BADGE99999")).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> invocation.getArgument(0));
        EmployeeDTO result = employeeService.updateEmployee(employeeId, request);
        assertNotNull(result);
        assertEquals("Updated Name", result.getName());
        assertEquals("BADGE99999", result.getBadgeId());
        verify(employeeRepository).save(any(Employee.class));
    }

    @Test
    void testUpdateEmployee_WithDuplicateBadgeId_ThrowsException() {
        UpdateEmployeeRequest request = new UpdateEmployeeRequest();
        request.setBadgeId("BADGE12345");
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
        when(employeeRepository.existsByBadgeIdAndNotDeleted("BADGE12345")).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.updateEmployee(employeeId, request);
        });
    }

    @Test
    void testUpdateEmployee_WithNonExistentId_ThrowsResourceNotFoundException() {
        UpdateEmployeeRequest request = new UpdateEmployeeRequest();
        UUID randomId = UUID.randomUUID();
        when(employeeRepository.findById(randomId)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> {
            employeeService.updateEmployee(randomId, request);
        });
    }

    @Test
    void testDeleteEmployee_WithValidId_SoftDeletesEmployee() {
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> invocation.getArgument(0));
        employeeService.deleteEmployee(employeeId);
        assertTrue(employee.getDeleted());
        assertEquals("TERMINATED", employee.getStatus());
        verify(employeeRepository).save(employee);
    }

    @Test
    void testDeleteEmployee_WithNonExistentId_ThrowsResourceNotFoundException() {
        UUID randomId = UUID.randomUUID();
        when(employeeRepository.findById(randomId)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> {
            employeeService.deleteEmployee(randomId);
        });
    }
}
