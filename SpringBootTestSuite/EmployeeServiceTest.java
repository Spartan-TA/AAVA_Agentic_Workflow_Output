package com.wms.ems.employee;

import com.wms.ems.common.ResourceNotFoundException;
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
import java.util.Collections;
import java.util.Optional;

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
    private EmployeeDTO employeeDTO;

    @BeforeEach
    void setUp() {
        employeeDTO = EmployeeDTO.builder()
                .badgeId("B123")
                .name("John Doe")
                .role("Worker")
                .department("Packing")
                .shiftGroup("A")
                .hireDate(LocalDate.of(2020, 1, 1))
                .status("Active")
                .build();

        employee = Employee.builder()
                .id(1L)
                .badgeId("B123")
                .name("John Doe")
                .role("Worker")
                .department("Packing")
                .shiftGroup("A")
                .hireDate(LocalDate.of(2020, 1, 1))
                .status("Active")
                .deleted(false)
                .build();
    }

    // Test getAllEmployees normal case
    @Test
    void testGetAllEmployees_Normal_ReturnsPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> page = new PageImpl<>(Collections.singletonList(employee));
        when(employeeRepository.findAll(pageable)).thenReturn(page);
        Page<Employee> result = employeeService.getAllEmployees(pageable);
        assertEquals(1, result.getTotalElements());
        verify(employeeRepository).findAll(pageable);
    }

    // Test getEmployeeById normal case
    @Test
    void testGetEmployeeById_ValidId_ReturnsEmployee() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        Employee result = employeeService.getEmployeeById(1L);
        assertEquals("John Doe", result.getName());
        verify(employeeRepository).findById(1L);
    }

    // Test getEmployeeById not found
    @Test
    void testGetEmployeeById_NotFound_ThrowsException() {
        when(employeeRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> employeeService.getEmployeeById(2L));
        verify(employeeRepository).findById(2L);
    }

    // Test getEmployeeByBadgeId normal case
    @Test
    void testGetEmployeeByBadgeId_ValidBadgeId_ReturnsEmployee() {
        when(employeeRepository.findByBadgeId("B123")).thenReturn(Optional.of(employee));
        Employee result = employeeService.getEmployeeByBadgeId("B123");
        assertEquals("John Doe", result.getName());
        verify(employeeRepository).findByBadgeId("B123");
    }

    // Test getEmployeeByBadgeId not found
    @Test
    void testGetEmployeeByBadgeId_NotFound_ThrowsException() {
        when(employeeRepository.findByBadgeId("B999")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> employeeService.getEmployeeByBadgeId("B999"));
        verify(employeeRepository).findByBadgeId("B999");
    }

    // Test createEmployee normal case
    @Test
    void testCreateEmployee_ValidDTO_ReturnsSavedEmployee() {
        when(employeeRepository.existsByBadgeId("B123")).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);
        Employee result = employeeService.createEmployee(employeeDTO);
        assertEquals("John Doe", result.getName());
        verify(employeeRepository).existsByBadgeId("B123");
        verify(employeeRepository).save(any(Employee.class));
    }

    // Test createEmployee duplicate badgeId
    @Test
    void testCreateEmployee_DuplicateBadgeId_ThrowsException() {
        when(employeeRepository.existsByBadgeId("B123")).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> employeeService.createEmployee(employeeDTO));
        verify(employeeRepository).existsByBadgeId("B123");
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    // Test updateEmployee normal case
    @Test
    void testUpdateEmployee_ValidId_ReturnsUpdatedEmployee() {
        EmployeeDTO updateDTO = EmployeeDTO.builder()
                .badgeId("B123")
                .name("Jane Smith")
                .role("Supervisor")
                .department("Shipping")
                .shiftGroup("B")
                .hireDate(LocalDate.of(2021, 2, 2))
                .status("Inactive")
                .build();
        Employee updatedEmployee = Employee.builder()
                .id(1L)
                .badgeId("B123")
                .name("Jane Smith")
                .role("Supervisor")
                .department("Shipping")
                .shiftGroup("B")
                .hireDate(LocalDate.of(2021, 2, 2))
                .status("Inactive")
                .deleted(false)
                .build();
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(updatedEmployee);
        Employee result = employeeService.updateEmployee(1L, updateDTO);
        assertEquals("Jane Smith", result.getName());
        assertEquals("Supervisor", result.getRole());
        verify(employeeRepository).findById(1L);
        verify(employeeRepository).save(employee);
    }

    // Test updateEmployee not found
    @Test
    void testUpdateEmployee_NotFound_ThrowsException() {
        when(employeeRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> employeeService.updateEmployee(2L, employeeDTO));
        verify(employeeRepository).findById(2L);
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    // Test deleteEmployee normal case
    @Test
    void testDeleteEmployee_ValidId_DeletesEmployee() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        doNothing().when(employeeRepository).delete(employee);
        employeeService.deleteEmployee(1L);
        verify(employeeRepository).findById(1L);
        verify(employeeRepository).delete(employee);
    }

    // Test deleteEmployee not found
    @Test
    void testDeleteEmployee_NotFound_ThrowsException() {
        when(employeeRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> employeeService.deleteEmployee(2L));
        verify(employeeRepository).findById(2L);
        verify(employeeRepository, never()).delete(any(Employee.class));
    }

    // Test filterEmployees normal case
    @Test
    void testFilterEmployees_ValidParams_ReturnsPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> page = new PageImpl<>(Collections.singletonList(employee));
        when(employeeRepository.findByDepartmentAndStatus("Packing", "Active", pageable)).thenReturn(page);
        Page<Employee> result = employeeService.filterEmployees("Packing", "Active", pageable);
        assertEquals(1, result.getTotalElements());
        verify(employeeRepository).findByDepartmentAndStatus("Packing", "Active", pageable);
    }

    // Test filterEmployees empty result
    @Test
    void testFilterEmployees_NoMatch_ReturnsEmptyPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> page = new PageImpl<>(Collections.emptyList());
        when(employeeRepository.findByDepartmentAndStatus("Unknown", "Inactive", pageable)).thenReturn(page);
        Page<Employee> result = employeeService.filterEmployees("Unknown", "Inactive", pageable);
        assertEquals(0, result.getTotalElements());
        verify(employeeRepository).findByDepartmentAndStatus("Unknown", "Inactive", pageable);
    }

    // Test searchEmployeesByName normal case
    @Test
    void testSearchEmployeesByName_ValidName_ReturnsPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> page = new PageImpl<>(Collections.singletonList(employee));
        when(employeeRepository.searchByName("John", pageable)).thenReturn(page);
        Page<Employee> result = employeeService.searchEmployeesByName("John", pageable);
        assertEquals(1, result.getTotalElements());
        verify(employeeRepository).searchByName("John", pageable);
    }

    // Test searchEmployeesByName empty result
    @Test
    void testSearchEmployeesByName_NoMatch_ReturnsEmptyPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> page = new PageImpl<>(Collections.emptyList());
        when(employeeRepository.searchByName("ZZZ", pageable)).thenReturn(page);
        Page<Employee> result = employeeService.searchEmployeesByName("ZZZ", pageable);
        assertEquals(0, result.getTotalElements());
        verify(employeeRepository).searchByName("ZZZ", pageable);
    }

    // Test createEmployee with null badgeId
    @Test
    void testCreateEmployee_NullBadgeId_ThrowsException() {
        EmployeeDTO dto = EmployeeDTO.builder()
                .badgeId(null)
                .name("John Doe")
                .role("Worker")
                .department("Packing")
                .hireDate(LocalDate.of(2020, 1, 1))
                .status("Active")
                .build();
        when(employeeRepository.existsByBadgeId(null)).thenReturn(false);
        assertThrows(NullPointerException.class, () -> employeeService.createEmployee(dto));
    }

    // Test createEmployee with empty badgeId
    @Test
    void testCreateEmployee_EmptyBadgeId_ThrowsException() {
        EmployeeDTO dto = EmployeeDTO.builder()
                .badgeId("")
                .name("John Doe")
                .role("Worker")
                .department("Packing")
                .hireDate(LocalDate.of(2020, 1, 1))
                .status("Active")
                .build();
        when(employeeRepository.existsByBadgeId("")).thenReturn(false);
        Employee result = employeeService.createEmployee(dto);
        assertNotNull(result);
        assertEquals("", result.getBadgeId());
    }

    // Test updateEmployee with null name
    @Test
    void testUpdateEmployee_NullName_UpdatesEmployee() {
        EmployeeDTO dto = EmployeeDTO.builder()
                .badgeId("B123")
                .name(null)
                .role("Worker")
                .department("Packing")
                .hireDate(LocalDate.of(2020, 1, 1))
                .status("Active")
                .build();
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);
        Employee result = employeeService.updateEmployee(1L, dto);
        assertNull(result.getName());
    }

    // Test updateEmployee with empty role
    @Test
    void testUpdateEmployee_EmptyRole_UpdatesEmployee() {
        EmployeeDTO dto = EmployeeDTO.builder()
                .badgeId("B123")
                .name("John Doe")
                .role("")
                .department("Packing")
                .hireDate(LocalDate.of(2020, 1, 1))
                .status("Active")
                .build();
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);
        Employee result = employeeService.updateEmployee(1L, dto);
        assertEquals("", result.getRole());
    }
}
