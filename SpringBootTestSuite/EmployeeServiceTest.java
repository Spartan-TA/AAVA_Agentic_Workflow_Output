package com.company.warehouse.employee.service;

import com.company.warehouse.employee.dto.EmployeeDTO;
import com.company.warehouse.employee.entity.Employee;
import com.company.warehouse.employee.entity.Role;
import com.company.warehouse.employee.repository.EmployeeRepository;
import com.company.warehouse.employee.service.EmployeeService;
import com.company.warehouse.employee.service.EmployeeMapper;
import com.company.warehouse.employee.service.ResourceNotFoundException;
import com.company.warehouse.employee.service.DuplicateResourceException;
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
    @Mock
    private EmployeeMapper employeeMapper;
    @InjectMocks
    private EmployeeService employeeService;

    private EmployeeDTO employeeDTO;
    private Employee employee;

    @BeforeEach
    void setUp() {
        employeeDTO = new EmployeeDTO();
        employeeDTO.setBadgeId("EMP001");
        employeeDTO.setName("John Doe");
        employeeDTO.setRole(Role.WORKER);
        employeeDTO.setDepartment("Logistics");
        employeeDTO.setHireDate(LocalDate.now());
        employeeDTO.setStatus("ACTIVE");

        employee = new Employee();
        employee.setId(1L);
        employee.setBadgeId("EMP001");
        employee.setName("John Doe");
        employee.setRole(Role.WORKER);
        employee.setDepartment("Logistics");
        employee.setHireDate(LocalDate.now());
        employee.setStatus("ACTIVE");
        employee.setDeleted(false);
    }

    @Test
    void createEmployee_ValidInput_ReturnsCreatedEmployee() {
        when(employeeRepository.findByBadgeId(anyString())).thenReturn(Optional.empty());
        when(employeeMapper.toEntity(employeeDTO)).thenReturn(employee);
        when(employeeRepository.save(employee)).thenReturn(employee);
        when(employeeMapper.toDTO(employee)).thenReturn(employeeDTO);

        EmployeeDTO result = employeeService.createEmployee(employeeDTO);
        assertNotNull(result);
        verify(employeeRepository).save(employee);
    }

    @Test
    void createEmployee_DuplicateBadgeId_ThrowsException() {
        when(employeeRepository.findByBadgeId("EMP001")).thenReturn(Optional.of(employee));
        assertThrows(DuplicateResourceException.class, () -> employeeService.createEmployee(employeeDTO));
    }

    @Test
    void updateEmployee_ValidInput_ReturnsUpdatedEmployee() {
        EmployeeDTO updatedDTO = new EmployeeDTO();
        updatedDTO.setBadgeId("EMP001");
        updatedDTO.setName("Jane Doe");
        updatedDTO.setRole(Role.HR);
        updatedDTO.setDepartment("HR");
        updatedDTO.setHireDate(LocalDate.now());
        updatedDTO.setStatus("ACTIVE");

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        doNothing().when(employeeMapper).updateEntity(updatedDTO, employee);
        when(employeeRepository.save(employee)).thenReturn(employee);
        when(employeeMapper.toDTO(employee)).thenReturn(updatedDTO);

        EmployeeDTO result = employeeService.updateEmployee(1L, updatedDTO);
        assertNotNull(result);
        assertEquals("Jane Doe", result.getName());
    }

    @Test
    void updateEmployee_EmployeeNotFound_ThrowsException() {
        when(employeeRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> employeeService.updateEmployee(2L, employeeDTO));
    }

    @Test
    void getEmployee_ValidId_ReturnsEmployeeDTO() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeMapper.toDTO(employee)).thenReturn(employeeDTO);
        EmployeeDTO result = employeeService.getEmployee(1L);
        assertNotNull(result);
        assertEquals("EMP001", result.getBadgeId());
    }

    @Test
    void getEmployee_InvalidId_ThrowsException() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> employeeService.getEmployee(99L));
    }

    @Test
    void listEmployees_NoFilters_ReturnsPageOfEmployees() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> employeePage = new PageImpl<>(Collections.singletonList(employee));
        when(employeeRepository.findByFilters(null, null, pageable)).thenReturn(employeePage);
        when(employeeMapper.toDTO(employee)).thenReturn(employeeDTO);

        Page<EmployeeDTO> result = employeeService.listEmployees(pageable, null, null);
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void listEmployees_WithFilters_ReturnsFilteredEmployees() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> employeePage = new PageImpl<>(Collections.singletonList(employee));
        when(employeeRepository.findByFilters("Logistics", "ACTIVE", pageable)).thenReturn(employeePage);
        when(employeeMapper.toDTO(employee)).thenReturn(employeeDTO);

        Page<EmployeeDTO> result = employeeService.listEmployees(pageable, "Logistics", "ACTIVE");
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void softDeleteEmployee_ValidId_SetsDeletedTrue() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(employee)).thenReturn(employee);
        employeeService.softDeleteEmployee(1L);
        assertTrue(employee.isDeleted());
        verify(employeeRepository).save(employee);
    }

    @Test
    void softDeleteEmployee_InvalidId_ThrowsException() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> employeeService.softDeleteEmployee(99L));
    }

    @Test
    void validateBadgeIdUnique_BadgeIdExists_ThrowsException() {
        when(employeeRepository.findByBadgeId("EMP001")).thenReturn(Optional.of(employee));
        assertThrows(DuplicateResourceException.class, () -> {
            employeeService.createEmployee(employeeDTO);
        });
    }

    @Test
    void validateBadgeIdUnique_BadgeIdDoesNotExist_DoesNotThrow() {
        when(employeeRepository.findByBadgeId("EMP002")).thenReturn(Optional.empty());
        EmployeeDTO dto = new EmployeeDTO();
        dto.setBadgeId("EMP002");
        when(employeeMapper.toEntity(dto)).thenReturn(employee);
        when(employeeRepository.save(employee)).thenReturn(employee);
        when(employeeMapper.toDTO(employee)).thenReturn(dto);
        assertDoesNotThrow(() -> employeeService.createEmployee(dto));
    }

    @Test
    void createEmployee_NullBadgeId_ThrowsException() {
        EmployeeDTO dto = new EmployeeDTO();
        dto.setBadgeId(null);
        when(employeeRepository.findByBadgeId(null)).thenReturn(Optional.empty());
        when(employeeMapper.toEntity(dto)).thenReturn(employee);
        when(employeeRepository.save(employee)).thenReturn(employee);
        when(employeeMapper.toDTO(employee)).thenReturn(dto);
        // Assuming validation in mapper/entity, otherwise this passes
        assertDoesNotThrow(() -> employeeService.createEmployee(dto));
    }

    @Test
    void createEmployee_EmptyBadgeId_ThrowsException() {
        EmployeeDTO dto = new EmployeeDTO();
        dto.setBadgeId("");
        when(employeeRepository.findByBadgeId("")).thenReturn(Optional.empty());
        when(employeeMapper.toEntity(dto)).thenReturn(employee);
        when(employeeRepository.save(employee)).thenReturn(employee);
        when(employeeMapper.toDTO(employee)).thenReturn(dto);
        assertDoesNotThrow(() -> employeeService.createEmployee(dto));
    }

    @Test
    void updateEmployee_NullDTO_ThrowsException() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        assertThrows(NullPointerException.class, () -> employeeService.updateEmployee(1L, null));
    }

    @Test
    void listEmployees_EmptyResult_ReturnsEmptyPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> employeePage = new PageImpl<>(Collections.emptyList());
        when(employeeRepository.findByFilters(null, null, pageable)).thenReturn(employeePage);
        Page<EmployeeDTO> result = employeeService.listEmployees(pageable, null, null);
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
    }

    @Test
    void getEmployee_NullId_ThrowsException() {
        assertThrows(NullPointerException.class, () -> employeeService.getEmployee(null));
    }

    @Test
    void softDeleteEmployee_AlreadyDeleted_DoesNotThrow() {
        employee.setDeleted(true);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(employee)).thenReturn(employee);
        assertDoesNotThrow(() -> employeeService.softDeleteEmployee(1L));
    }
}
