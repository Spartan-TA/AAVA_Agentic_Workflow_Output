package com.example.warehouse.service;

import com.example.warehouse.dto.EmployeeDTO;
import com.example.warehouse.entity.Employee;
import com.example.warehouse.entity.EmployeeStatus;
import com.example.warehouse.entity.Role;
import com.example.warehouse.exception.DuplicateResourceException;
import com.example.warehouse.exception.ResourceNotFoundException;
import com.example.warehouse.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeService employeeService;

    private EmployeeDTO validDto;
    private Employee validEmployee;

    @BeforeEach
    void setUp() {
        validDto = new EmployeeDTO();
        validDto.setName("John Doe");
        validDto.setBadgeId("B123");
        validDto.setEmail("john.doe@example.com");
        validDto.setRole(Role.WORKER);
        validDto.setDepartment("Logistics");
        validDto.setShiftGroup("A");
        validDto.setHireDate(LocalDate.now());
        validDto.setStatus(EmployeeStatus.ACTIVE);

        validEmployee = new Employee();
        validEmployee.setId(1L);
        validEmployee.setName("John Doe");
        validEmployee.setBadgeId("B123");
        validEmployee.setEmail("john.doe@example.com");
        validEmployee.setRole(Role.WORKER);
        validEmployee.setDepartment("Logistics");
        validEmployee.setShiftGroup("A");
        validEmployee.setHireDate(LocalDate.now());
        validEmployee.setStatus(EmployeeStatus.ACTIVE);
        validEmployee.setDeleted(false);
    }

    @Test
    void testCreateEmployee_ValidInput_Success() {
        when(employeeRepository.existsByBadgeId(validDto.getBadgeId())).thenReturn(false);
        when(employeeRepository.existsByEmail(validDto.getEmail())).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);

        EmployeeDTO result = employeeService.createEmployee(validDto);

        assertNotNull(result);
        assertEquals(validDto.getName(), result.getName());
        verify(employeeRepository).save(any(Employee.class));
    }

    @Test
    void testCreateEmployee_NullName_ThrowsException() {
        validDto.setName(null);
        assertThrows(Exception.class, () -> employeeService.createEmployee(validDto));
    }

    @Test
    void testCreateEmployee_EmptyBadgeId_ThrowsException() {
        validDto.setBadgeId("");
        assertThrows(Exception.class, () -> employeeService.createEmployee(validDto));
    }

    @Test
    void testCreateEmployee_DuplicateBadgeId_ThrowsDuplicateResourceException() {
        when(employeeRepository.existsByBadgeId(validDto.getBadgeId())).thenReturn(true);
        assertThrows(DuplicateResourceException.class, () -> employeeService.createEmployee(validDto));
    }

    @Test
    void testCreateEmployee_DuplicateEmail_ThrowsDuplicateResourceException() {
        when(employeeRepository.existsByBadgeId(validDto.getBadgeId())).thenReturn(false);
        when(employeeRepository.existsByEmail(validDto.getEmail())).thenReturn(true);
        assertThrows(DuplicateResourceException.class, () -> employeeService.createEmployee(validDto));
    }

    @Test
    void testCreateEmployee_InvalidEmail_ThrowsException() {
        validDto.setEmail("invalid-email");
        when(employeeRepository.existsByBadgeId(validDto.getBadgeId())).thenReturn(false);
        assertThrows(Exception.class, () -> employeeService.createEmployee(validDto));
    }

    @Test
    void testGetEmployeeById_ExistingId_ReturnsEmployee() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        EmployeeDTO result = employeeService.getEmployeeById(1L);
        assertNotNull(result);
        assertEquals(validEmployee.getName(), result.getName());
    }

    @Test
    void testGetEmployeeById_NonExistingId_ThrowsResourceNotFoundException() {
        when(employeeRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> employeeService.getEmployeeById(2L));
    }

    @Test
    void testGetEmployeeById_DeletedEmployee_ThrowsResourceNotFoundException() {
        validEmployee.setDeleted(true);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        assertThrows(ResourceNotFoundException.class, () -> employeeService.getEmployeeById(1L));
    }

    @Test
    void testGetAllEmployees_WithPagination_ReturnsPagedResults() {
        Page<Employee> page = new PageImpl<>(List.of(validEmployee));
        when(employeeRepository.findAllByDeletedFalse(any(Pageable.class))).thenReturn(page);

        Page<EmployeeDTO> result = employeeService.getAllEmployees(PageRequest.of(0, 10));
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void testGetAllEmployees_EmptyDatabase_ReturnsEmptyPage() {
        Page<Employee> page = new PageImpl<>(Collections.emptyList());
        when(employeeRepository.findAllByDeletedFalse(any(Pageable.class))).thenReturn(page);

        Page<EmployeeDTO> result = employeeService.getAllEmployees(PageRequest.of(0, 10));
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
    }

    @Test
    void testUpdateEmployee_ValidInput_Success() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);

        EmployeeDTO updateDto = new EmployeeDTO();
        updateDto.setName("Jane Doe");
        updateDto.setEmail("jane.doe@example.com");
        updateDto.setDepartment("Packing");

        EmployeeDTO result = employeeService.updateEmployee(1L, updateDto);

        assertNotNull(result);
        assertEquals("Jane Doe", result.getName());
        assertEquals("jane.doe@example.com", result.getEmail());
        assertEquals("Packing", result.getDepartment());
    }

    @Test
    void testUpdateEmployee_NonExistingId_ThrowsResourceNotFoundException() {
        when(employeeRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> employeeService.updateEmployee(2L, validDto));
    }

    @Test
    void testUpdateEmployee_NullFields_ThrowsException() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        EmployeeDTO updateDto = new EmployeeDTO();
        updateDto.setName(null);
        updateDto.setEmail(null);
        updateDto.setDepartment(null);
        assertThrows(Exception.class, () -> employeeService.updateEmployee(1L, updateDto));
    }

    @Test
    void testDeleteEmployee_ExistingId_SoftDeletesEmployee() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);

        employeeService.deleteEmployee(1L);

        assertTrue(validEmployee.isDeleted());
        assertEquals(EmployeeStatus.TERMINATED, validEmployee.getStatus());
        verify(employeeRepository).save(validEmployee);
    }

    @Test
    void testDeleteEmployee_NonExistingId_ThrowsResourceNotFoundException() {
        when(employeeRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> employeeService.deleteEmployee(2L));
    }

    @Test
    void testDeleteEmployee_AlreadyDeleted_ThrowsResourceNotFoundException() {
        validEmployee.setDeleted(true);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        assertThrows(ResourceNotFoundException.class, () -> employeeService.deleteEmployee(1L));
    }
}