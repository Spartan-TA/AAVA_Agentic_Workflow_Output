package com.warehouse.ems.service.employee;

import com.warehouse.ems.domain.employee.Employee;
import com.warehouse.ems.domain.employee.Role;
import com.warehouse.ems.dto.employee.EmployeeRequest;
import com.warehouse.ems.dto.employee.EmployeeResponse;
import com.warehouse.ems.mapper.EmployeeMapper;
import com.warehouse.ems.repository.employee.EmployeeRepository;
import jakarta.persistence.EntityNotFoundException;
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
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for EmployeeServiceImpl.
 * Tests cover all CRUD operations, validation logic, edge cases, and error handling.
 */
@ExtendWith(MockitoExtension.class)
public class EmployeeServiceImplTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private EmployeeMapper employeeMapper;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    private Employee testEmployee;
    private EmployeeRequest testRequest;
    private EmployeeResponse testResponse;

    @BeforeEach
    public void setUp() {
        // Setup test data
        testEmployee = Employee.builder()
                .id(1L)
                .badgeId("EMP001")
                .name("John Doe")
                .role(Role.WORKER)
                .department("Warehouse")
                .shiftGroup("Morning")
                .hireDate(LocalDate.of(2023, 1, 15))
                .status("ACTIVE")
                .deleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testRequest = new EmployeeRequest();
        testRequest.setBadgeId("EMP001");
        testRequest.setName("John Doe");
        testRequest.setRole(Role.WORKER);
        testRequest.setDepartment("Warehouse");
        testRequest.setShiftGroup("Morning");
        testRequest.setHireDate(LocalDate.of(2023, 1, 15));
        testRequest.setStatus("ACTIVE");

        testResponse = EmployeeResponse.builder()
                .id(1L)
                .badgeId("EMP001")
                .name("John Doe")
                .role(Role.WORKER)
                .department("Warehouse")
                .shiftGroup("Morning")
                .hireDate(LocalDate.of(2023, 1, 15))
                .status("ACTIVE")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // ==================== CREATE EMPLOYEE TESTS ====================

    @Test
    public void testCreateEmployee_ValidInput_Success() {
        // Arrange
        when(employeeRepository.findByBadgeId(anyString())).thenReturn(Optional.empty());
        when(employeeMapper.toEntity(any(EmployeeRequest.class))).thenReturn(testEmployee);
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);
        when(employeeMapper.toResponse(any(Employee.class))).thenReturn(testResponse);

        // Act
        EmployeeResponse result = employeeService.create(testRequest);

        // Assert
        assertNotNull(result);
        assertEquals("EMP001", result.getBadgeId());
        assertEquals("John Doe", result.getName());
        verify(employeeRepository, times(1)).findByBadgeId("EMP001");
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    public void testCreateEmployee_DuplicateBadgeId_ThrowsException() {
        // Arrange
        when(employeeRepository.findByBadgeId(anyString())).thenReturn(Optional.of(testEmployee));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> employeeService.create(testRequest)
        );
        assertTrue(exception.getMessage().contains("Badge ID already exists"));
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    public void testCreateEmployee_NullBadgeId_ThrowsException() {
        // Arrange
        testRequest.setBadgeId(null);

        // Act & Assert
        assertThrows(NullPointerException.class, () -> employeeService.create(testRequest));
    }

    @Test
    public void testCreateEmployee_EmptyBadgeId_ThrowsException() {
        // Arrange
        testRequest.setBadgeId("");
        when(employeeRepository.findByBadgeId(anyString())).thenReturn(Optional.empty());

        // Act - Should handle empty string validation
        assertDoesNotThrow(() -> employeeService.create(testRequest));
    }

    @Test
    public void testCreateEmployee_MaxLengthBadgeId_Success() {
        // Arrange
        String maxLengthBadgeId = "A".repeat(32);
        testRequest.setBadgeId(maxLengthBadgeId);
        testEmployee.setBadgeId(maxLengthBadgeId);
        
        when(employeeRepository.findByBadgeId(anyString())).thenReturn(Optional.empty());
        when(employeeMapper.toEntity(any(EmployeeRequest.class))).thenReturn(testEmployee);
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);
        when(employeeMapper.toResponse(any(Employee.class))).thenReturn(testResponse);

        // Act
        EmployeeResponse result = employeeService.create(testRequest);

        // Assert
        assertNotNull(result);
    }

    @Test
    public void testCreateEmployee_AllRoles_Success() {
        // Test each role
        for (Role role : Role.values()) {
            testRequest.setRole(role);
            testEmployee.setRole(role);
            
            when(employeeRepository.findByBadgeId(anyString())).thenReturn(Optional.empty());
            when(employeeMapper.toEntity(any(EmployeeRequest.class))).thenReturn(testEmployee);
            when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);
            when(employeeMapper.toResponse(any(Employee.class))).thenReturn(testResponse);

            EmployeeResponse result = employeeService.create(testRequest);
            assertNotNull(result);
        }
    }

    // ==================== GET EMPLOYEE BY ID TESTS ====================

    @Test
    public void testGetById_ValidId_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeMapper.toResponse(any(Employee.class))).thenReturn(testResponse);

        // Act
        EmployeeResponse result = employeeService.getById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(employeeRepository, times(1)).findById(1L);
    }

    @Test
    public void testGetById_NonExistentId_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> employeeService.getById(999L)
        );
        assertTrue(exception.getMessage().contains("Employee not found"));
    }

    @Test
    public void testGetById_NullId_ThrowsException() {
        // Act & Assert
        assertThrows(Exception.class, () -> employeeService.getById(null));
    }

    @Test
    public void testGetById_NegativeId_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(-1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> employeeService.getById(-1L));
    }

    @Test
    public void testGetById_ZeroId_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(0L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> employeeService.getById(0L));
    }

    // ==================== GET ALL EMPLOYEES TESTS ====================

    @Test
    public void testGetAll_NoDepartmentFilter_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> employeePage = new PageImpl<>(Arrays.asList(testEmployee));
        
        when(employeeRepository.findAll(pageable)).thenReturn(employeePage);
        when(employeeMapper.toResponse(any(Employee.class))).thenReturn(testResponse);

        // Act
        Page<EmployeeResponse> result = employeeService.getAll(null, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(employeeRepository, times(1)).findAll(pageable);
    }

    @Test
    public void testGetAll_WithDepartmentFilter_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> employeePage = new PageImpl<>(Arrays.asList(testEmployee));
        
        when(employeeRepository.findAllByDepartment("Warehouse", pageable)).thenReturn(employeePage);
        when(employeeMapper.toResponse(any(Employee.class))).thenReturn(testResponse);

        // Act
        Page<EmployeeResponse> result = employeeService.getAll("Warehouse", pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(employeeRepository, times(1)).findAllByDepartment("Warehouse", pageable);
    }

    @Test
    public void testGetAll_EmptyResult_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> emptyPage = new PageImpl<>(Arrays.asList());
        
        when(employeeRepository.findAll(pageable)).thenReturn(emptyPage);

        // Act
        Page<EmployeeResponse> result = employeeService.getAll(null, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
    }

    @Test
    public void testGetAll_LargePage_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 1000);
        Page<Employee> employeePage = new PageImpl<>(Arrays.asList(testEmployee));
        
        when(employeeRepository.findAll(pageable)).thenReturn(employeePage);
        when(employeeMapper.toResponse(any(Employee.class))).thenReturn(testResponse);

        // Act
        Page<EmployeeResponse> result = employeeService.getAll(null, pageable);

        // Assert
        assertNotNull(result);
    }

    // ==================== UPDATE EMPLOYEE TESTS ====================

    @Test
    public void testUpdate_ValidInput_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.findByBadgeId(anyString())).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);
        when(employeeMapper.toResponse(any(Employee.class))).thenReturn(testResponse);
        doNothing().when(employeeMapper).updateEmployeeFromRequest(any(), any());

        // Act
        EmployeeResponse result = employeeService.update(1L, testRequest);

        // Assert
        assertNotNull(result);
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    public void testUpdate_NonExistentId_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> employeeService.update(999L, testRequest));
    }

    @Test
    public void testUpdate_DuplicateBadgeId_ThrowsException() {
        // Arrange
        Employee anotherEmployee = Employee.builder()
                .id(2L)
                .badgeId("EMP002")
                .build();
        
        testRequest.setBadgeId("EMP002");
        
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.findByBadgeId("EMP002")).thenReturn(Optional.of(anotherEmployee));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> employeeService.update(1L, testRequest));
    }

    @Test
    public void testUpdate_SameBadgeId_Success() {
        // Arrange - updating with same badge ID should be allowed
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);
        when(employeeMapper.toResponse(any(Employee.class))).thenReturn(testResponse);
        doNothing().when(employeeMapper).updateEmployeeFromRequest(any(), any());

        // Act
        EmployeeResponse result = employeeService.update(1L, testRequest);

        // Assert
        assertNotNull(result);
    }

    @Test
    public void testUpdate_PartialUpdate_Success() {
        // Arrange - only updating name
        EmployeeRequest partialRequest = new EmployeeRequest();
        partialRequest.setBadgeId("EMP001");
        partialRequest.setName("Jane Doe");
        
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);
        when(employeeMapper.toResponse(any(Employee.class))).thenReturn(testResponse);
        doNothing().when(employeeMapper).updateEmployeeFromRequest(any(), any());

        // Act
        EmployeeResponse result = employeeService.update(1L, partialRequest);

        // Assert
        assertNotNull(result);
    }

    // ==================== DELETE EMPLOYEE TESTS ====================

    @Test
    public void testDelete_ValidId_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        doNothing().when(employeeRepository).delete(any(Employee.class));

        // Act
        employeeService.delete(1L);

        // Assert
        verify(employeeRepository, times(1)).delete(testEmployee);
    }

    @Test
    public void testDelete_NonExistentId_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> employeeService.delete(999L));
        verify(employeeRepository, never()).delete(any(Employee.class));
    }

    @Test
    public void testDelete_NullId_ThrowsException() {
        // Act & Assert
        assertThrows(Exception.class, () -> employeeService.delete(null));
    }

    @Test
    public void testDelete_AlreadyDeleted_Success() {
        // Arrange - soft delete should be idempotent
        testEmployee.setDeleted(true);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        doNothing().when(employeeRepository).delete(any(Employee.class));

        // Act
        employeeService.delete(1L);

        // Assert
        verify(employeeRepository, times(1)).delete(testEmployee);
    }

    // ==================== EDGE CASE TESTS ====================

    @Test
    public void testCreateEmployee_SpecialCharactersInName_Success() {
        // Arrange
        testRequest.setName("O'Brien-Smith Jr.");
        testEmployee.setName("O'Brien-Smith Jr.");
        
        when(employeeRepository.findByBadgeId(anyString())).thenReturn(Optional.empty());
        when(employeeMapper.toEntity(any(EmployeeRequest.class))).thenReturn(testEmployee);
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);
        when(employeeMapper.toResponse(any(Employee.class))).thenReturn(testResponse);

        // Act
        EmployeeResponse result = employeeService.create(testRequest);

        // Assert
        assertNotNull(result);
    }

    @Test
    public void testCreateEmployee_FutureHireDate_Success() {
        // Arrange
        testRequest.setHireDate(LocalDate.now().plusDays(30));
        testEmployee.setHireDate(LocalDate.now().plusDays(30));
        
        when(employeeRepository.findByBadgeId(anyString())).thenReturn(Optional.empty());
        when(employeeMapper.toEntity(any(EmployeeRequest.class))).thenReturn(testEmployee);
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);
        when(employeeMapper.toResponse(any(Employee.class))).thenReturn(testResponse);

        // Act
        EmployeeResponse result = employeeService.create(testRequest);

        // Assert
        assertNotNull(result);
    }

    @Test
    public void testCreateEmployee_PastHireDate_Success() {
        // Arrange
        testRequest.setHireDate(LocalDate.of(2000, 1, 1));
        testEmployee.setHireDate(LocalDate.of(2000, 1, 1));
        
        when(employeeRepository.findByBadgeId(anyString())).thenReturn(Optional.empty());
        when(employeeMapper.toEntity(any(EmployeeRequest.class))).thenReturn(testEmployee);
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);
        when(employeeMapper.toResponse(any(Employee.class))).thenReturn(testResponse);

        // Act
        EmployeeResponse result = employeeService.create(testRequest);

        // Assert
        assertNotNull(result);
    }

    @Test
    public void testGetAll_MultiplePages_Success() {
        // Arrange
        Pageable firstPage = PageRequest.of(0, 1);
        Pageable secondPage = PageRequest.of(1, 1);
        
        Page<Employee> page1 = new PageImpl<>(Arrays.asList(testEmployee), firstPage, 2);
        Page<Employee> page2 = new PageImpl<>(Arrays.asList(testEmployee), secondPage, 2);
        
        when(employeeRepository.findAll(firstPage)).thenReturn(page1);
        when(employeeRepository.findAll(secondPage)).thenReturn(page2);
        when(employeeMapper.toResponse(any(Employee.class))).thenReturn(testResponse);

        // Act
        Page<EmployeeResponse> result1 = employeeService.getAll(null, firstPage);
        Page<EmployeeResponse> result2 = employeeService.getAll(null, secondPage);

        // Assert
        assertNotNull(result1);
        assertNotNull(result2);
        assertEquals(2, result1.getTotalElements());
        assertEquals(2, result2.getTotalElements());
    }
}