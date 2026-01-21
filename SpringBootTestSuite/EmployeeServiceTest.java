package com.wms.employee;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * JUnit tests for EmployeeService covering all method signatures and edge cases.
 */
@ExtendWith(MockitoExtension.class)
public class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeService employeeService;

    private EmployeeDTO validEmployeeDTO;
    private Employee validEmployee;

    @BeforeEach
    public void setUp() {
        validEmployeeDTO = new EmployeeDTO();
        validEmployeeDTO.setName("John Doe");
        validEmployeeDTO.setBadgeId("BADGE123");
        validEmployeeDTO.setRole("WORKER");
        validEmployeeDTO.setDepartment("Logistics");
        validEmployeeDTO.setShiftGroup("A");
        validEmployeeDTO.setHireDate(LocalDate.now());
        validEmployeeDTO.setStatus("ACTIVE");

        validEmployee = new Employee();
        validEmployee.setId(1L);
        validEmployee.setName("John Doe");
        validEmployee.setBadgeId("BADGE123");
        validEmployee.setRole("WORKER");
        validEmployee.setDepartment("Logistics");
        validEmployee.setShiftGroup("A");
        validEmployee.setHireDate(LocalDate.now());
        validEmployee.setStatus("ACTIVE");
    }

    @Test
    public void testCreateEmployee_ValidInput_ReturnsEmployeeDTO() {
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);
        EmployeeDTO result = employeeService.createEmployee(validEmployeeDTO);
        assertNotNull(result);
        assertEquals("John Doe", result.getName());
    }

    @Test
    public void testCreateEmployee_NullName_ThrowsValidationException() {
        validEmployeeDTO.setName(null);
        assertThrows(ValidationException.class, () -> employeeService.createEmployee(validEmployeeDTO));
    }

    @Test
    public void testCreateEmployee_NullBadgeId_ThrowsValidationException() {
        validEmployeeDTO.setBadgeId(null);
        assertThrows(ValidationException.class, () -> employeeService.createEmployee(validEmployeeDTO));
    }

    @Test
    public void testCreateEmployee_DuplicateBadgeId_ThrowsValidationException() {
        when(employeeRepository.findByBadgeId("BADGE123")).thenReturn(Optional.of(validEmployee));
        assertThrows(ValidationException.class, () -> employeeService.createEmployee(validEmployeeDTO));
    }

    @Test
    public void testCreateEmployee_EmptyStrings_ThrowsValidationException() {
        validEmployeeDTO.setName("");
        validEmployeeDTO.setBadgeId("");
        assertThrows(ValidationException.class, () -> employeeService.createEmployee(validEmployeeDTO));
    }

    @Test
    public void testCreateEmployee_InvalidRole_ThrowsValidationException() {
        validEmployeeDTO.setRole("INVALID_ROLE");
        assertThrows(ValidationException.class, () -> employeeService.createEmployee(validEmployeeDTO));
    }

    @Test
    public void testUpdateEmployee_ValidUpdate_ReturnsEmployeeDTO() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);
        EmployeeDTO result = employeeService.updateEmployee(1L, validEmployeeDTO);
        assertNotNull(result);
        assertEquals("John Doe", result.getName());
    }

    @Test
    public void testUpdateEmployee_NonExistentId_ThrowsResourceNotFoundException() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> employeeService.updateEmployee(99L, validEmployeeDTO));
    }

    @Test
    public void testUpdateEmployee_NullFields_ThrowsValidationException() {
        validEmployeeDTO.setName(null);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        assertThrows(ValidationException.class, () -> employeeService.updateEmployee(1L, validEmployeeDTO));
    }

    @Test
    public void testUpdateEmployee_BadgeIdConflict_ThrowsValidationException() {
        Employee otherEmployee = new Employee();
        otherEmployee.setId(2L);
        otherEmployee.setBadgeId("BADGE123");
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        when(employeeRepository.findByBadgeId("BADGE123")).thenReturn(Optional.of(otherEmployee));
        assertThrows(ValidationException.class, () -> employeeService.updateEmployee(1L, validEmployeeDTO));
    }

    @Test
    public void testDeleteEmployee_ValidDelete_Succeeds() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        doNothing().when(employeeRepository).deleteById(1L);
        assertDoesNotThrow(() -> employeeService.deleteEmployee(1L));
    }

    @Test
    public void testDeleteEmployee_NonExistentId_ThrowsResourceNotFoundException() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> employeeService.deleteEmployee(99L));
    }

    @Test
    public void testDeleteEmployee_AlreadyDeleted_ThrowsResourceNotFoundException() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> employeeService.deleteEmployee(1L));
    }

    @Test
    public void testGetEmployeeById_ExistingId_ReturnsEmployeeDTO() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        EmployeeDTO result = employeeService.getEmployeeById(1L);
        assertNotNull(result);
        assertEquals("John Doe", result.getName());
    }

    @Test
    public void testGetEmployeeById_NonExistentId_ThrowsResourceNotFoundException() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> employeeService.getEmployeeById(99L));
    }

    @Test
    public void testGetEmployeeById_NullId_ThrowsValidationException() {
        assertThrows(ValidationException.class, () -> employeeService.getEmployeeById(null));
    }

    @Test
    public void testListEmployees_EmptyList_ReturnsEmptyPage() {
        Pageable pageable = PageRequest.of(0, 10);
        when(employeeRepository.findAll(pageable)).thenReturn(new PageImpl<>(Collections.emptyList()));
        Page<EmployeeDTO> result = employeeService.listEmployees(pageable, null, null);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testListEmployees_WithPagination_ReturnsPagedResults() {
        Pageable pageable = PageRequest.of(0, 10);
        when(employeeRepository.findAll(pageable)).thenReturn(new PageImpl<>(Arrays.asList(validEmployee)));
        Page<EmployeeDTO> result = employeeService.listEmployees(pageable, null, null);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    public void testListEmployees_WithRoleFilter_ReturnsFilteredResults() {
        Pageable pageable = PageRequest.of(0, 10);
        when(employeeRepository.findByRole("WORKER", pageable)).thenReturn(new PageImpl<>(Arrays.asList(validEmployee)));
        Page<EmployeeDTO> result = employeeService.listEmployees(pageable, "WORKER", null);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    public void testListEmployees_WithDepartmentFilter_ReturnsFilteredResults() {
        Pageable pageable = PageRequest.of(0, 10);
        when(employeeRepository.findByDepartment("Logistics", pageable)).thenReturn(new PageImpl<>(Arrays.asList(validEmployee)));
        Page<EmployeeDTO> result = employeeService.listEmployees(pageable, null, "Logistics");
        assertEquals(1, result.getTotalElements());
    }

    @Test
    public void testFindByBadgeId_ExistingBadge_ReturnsEmployeeDTO() {
        when(employeeRepository.findByBadgeId("BADGE123")).thenReturn(Optional.of(validEmployee));
        EmployeeDTO result = employeeService.findByBadgeId("BADGE123");
        assertNotNull(result);
        assertEquals("BADGE123", result.getBadgeId());
    }

    @Test
    public void testFindByBadgeId_NonExistentBadge_ThrowsResourceNotFoundException() {
        when(employeeRepository.findByBadgeId("BADGE999")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> employeeService.findByBadgeId("BADGE999"));
    }

    @Test
    public void testFindByBadgeId_NullBadge_ThrowsValidationException() {
        assertThrows(ValidationException.class, () -> employeeService.findByBadgeId(null));
    }
}
