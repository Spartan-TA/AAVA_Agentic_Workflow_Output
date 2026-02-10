package com.company.warehousemgmt.service;

import com.company.warehousemgmt.domain.Employee;
import com.company.warehousemgmt.dto.EmployeeDTO;
import com.company.warehousemgmt.exception.NotFoundException;
import com.company.warehousemgmt.repository.EmployeeRepository;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Comprehensive JUnit test suite for EmployeeService
 * Tests cover normal cases, boundary conditions, and edge cases
 */
@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeService employeeService;

    private Employee testEmployee;
    private EmployeeDTO testEmployeeDTO;

    @BeforeEach
    void setUp() {
        // Arrange: Set up test data
        testEmployee = new Employee();
        testEmployee.setId(1L);
        testEmployee.setBadgeId("EMP001");
        testEmployee.setName("John Doe");
        testEmployee.setRole("WORKER");
        testEmployee.setDepartment("Warehouse");
        testEmployee.setShiftGroup("A");
        testEmployee.setHireDate(LocalDate.of(2023, 1, 15));
        testEmployee.setStatus("ACTIVE");
        testEmployee.setDeleted(false);

        testEmployeeDTO = new EmployeeDTO();
        testEmployeeDTO.setId(1L);
        testEmployeeDTO.setBadgeId("EMP001");
        testEmployeeDTO.setName("John Doe");
        testEmployeeDTO.setRole("WORKER");
        testEmployeeDTO.setDepartment("Warehouse");
        testEmployeeDTO.setShiftGroup("A");
        testEmployeeDTO.setHireDate(LocalDate.of(2023, 1, 15));
        testEmployeeDTO.setStatus("ACTIVE");
    }

    // ========== getById Tests ==========

    @Test
    void testGetById_WithValidId_ReturnsEmployeeDTO() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));

        // Act
        EmployeeDTO result = employeeService.getById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("EMP001", result.getBadgeId());
        assertEquals("John Doe", result.getName());
        verify(employeeRepository, times(1)).findById(1L);
    }

    @Test
    void testGetById_WithNonExistentId_ThrowsNotFoundException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> employeeService.getById(999L));
        verify(employeeRepository, times(1)).findById(999L);
    }

    @Test
    void testGetById_WithNullId_ThrowsIllegalArgumentException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> employeeService.getById(null));
        verify(employeeRepository, never()).findById(anyLong());
    }

    @Test
    void testGetById_WithNegativeId_ThrowsIllegalArgumentException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> employeeService.getById(-1L));
        verify(employeeRepository, never()).findById(anyLong());
    }

    @Test
    void testGetById_WithZeroId_ThrowsIllegalArgumentException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> employeeService.getById(0L));
        verify(employeeRepository, never()).findById(anyLong());
    }

    @Test
    void testGetById_WithMaxLongValue_HandlesGracefully() {
        // Arrange
        when(employeeRepository.findById(Long.MAX_VALUE)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> employeeService.getById(Long.MAX_VALUE));
        verify(employeeRepository, times(1)).findById(Long.MAX_VALUE);
    }

    // ========== getAll Tests ==========

    @Test
    void testGetAll_WithValidPageable_ReturnsPageOfEmployees() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<Employee> employees = Arrays.asList(testEmployee);
        Page<Employee> page = new PageImpl<>(employees, pageable, 1);
        when(employeeRepository.findAll(pageable)).thenReturn(page);

        // Act
        Page<EmployeeDTO> result = employeeService.getAll(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("EMP001", result.getContent().get(0).getBadgeId());
        verify(employeeRepository, times(1)).findAll(pageable);
    }

    @Test
    void testGetAll_WithEmptyResult_ReturnsEmptyPage() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> emptyPage = new PageImpl<>(Arrays.asList(), pageable, 0);
        when(employeeRepository.findAll(pageable)).thenReturn(emptyPage);

        // Act
        Page<EmployeeDTO> result = employeeService.getAll(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
        verify(employeeRepository, times(1)).findAll(pageable);
    }

    @Test
    void testGetAll_WithNullPageable_ThrowsIllegalArgumentException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> employeeService.getAll(null));
        verify(employeeRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    void testGetAll_WithLargePageSize_HandlesGracefully() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 1000);
        List<Employee> employees = Arrays.asList(testEmployee);
        Page<Employee> page = new PageImpl<>(employees, pageable, 1);
        when(employeeRepository.findAll(pageable)).thenReturn(page);

        // Act
        Page<EmployeeDTO> result = employeeService.getAll(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(employeeRepository, times(1)).findAll(pageable);
    }

    // ========== create Tests ==========

    @Test
    void testCreate_WithValidEmployeeDTO_ReturnsCreatedEmployee() {
        // Arrange
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);
        when(employeeRepository.existsByBadgeId("EMP001")).thenReturn(false);

        // Act
        EmployeeDTO result = employeeService.create(testEmployeeDTO);

        // Assert
        assertNotNull(result);
        assertEquals("EMP001", result.getBadgeId());
        assertEquals("John Doe", result.getName());
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    void testCreate_WithNullEmployeeDTO_ThrowsIllegalArgumentException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> employeeService.create(null));
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void testCreate_WithNullBadgeId_ThrowsIllegalArgumentException() {
        // Arrange
        testEmployeeDTO.setBadgeId(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> employeeService.create(testEmployeeDTO));
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void testCreate_WithEmptyBadgeId_ThrowsIllegalArgumentException() {
        // Arrange
        testEmployeeDTO.setBadgeId("");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> employeeService.create(testEmployeeDTO));
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void testCreate_WithDuplicateBadgeId_ThrowsIllegalArgumentException() {
        // Arrange
        when(employeeRepository.existsByBadgeId("EMP001")).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> employeeService.create(testEmployeeDTO));
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void testCreate_WithNullName_ThrowsIllegalArgumentException() {
        // Arrange
        testEmployeeDTO.setName(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> employeeService.create(testEmployeeDTO));
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void testCreate_WithEmptyName_ThrowsIllegalArgumentException() {
        // Arrange
        testEmployeeDTO.setName("");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> employeeService.create(testEmployeeDTO));
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void testCreate_WithInvalidRole_ThrowsIllegalArgumentException() {
        // Arrange
        testEmployeeDTO.setRole("INVALID_ROLE");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> employeeService.create(testEmployeeDTO));
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void testCreate_WithFutureHireDate_ThrowsIllegalArgumentException() {
        // Arrange
        testEmployeeDTO.setHireDate(LocalDate.now().plusDays(1));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> employeeService.create(testEmployeeDTO));
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void testCreate_WithVeryLongName_HandlesGracefully() {
        // Arrange
        String longName = "A".repeat(256);
        testEmployeeDTO.setName(longName);
        when(employeeRepository.existsByBadgeId("EMP001")).thenReturn(false);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> employeeService.create(testEmployeeDTO));
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    // ========== update Tests ==========

    @Test
    void testUpdate_WithValidIdAndDTO_ReturnsUpdatedEmployee() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);
        testEmployeeDTO.setName("Jane Doe");

        // Act
        EmployeeDTO result = employeeService.update(1L, testEmployeeDTO);

        // Assert
        assertNotNull(result);
        assertEquals("Jane Doe", result.getName());
        verify(employeeRepository, times(1)).findById(1L);
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    void testUpdate_WithNonExistentId_ThrowsNotFoundException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> employeeService.update(999L, testEmployeeDTO));
        verify(employeeRepository, times(1)).findById(999L);
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void testUpdate_WithNullId_ThrowsIllegalArgumentException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> employeeService.update(null, testEmployeeDTO));
        verify(employeeRepository, never()).findById(anyLong());
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void testUpdate_WithNullDTO_ThrowsIllegalArgumentException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> employeeService.update(1L, null));
        verify(employeeRepository, never()).findById(anyLong());
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void testUpdate_WithChangedBadgeIdToDuplicate_ThrowsIllegalArgumentException() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.existsByBadgeId("EMP002")).thenReturn(true);
        testEmployeeDTO.setBadgeId("EMP002");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> employeeService.update(1L, testEmployeeDTO));
        verify(employeeRepository, times(1)).findById(1L);
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void testUpdate_WithDeletedEmployee_ThrowsIllegalArgumentException() {
        // Arrange
        testEmployee.setDeleted(true);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> employeeService.update(1L, testEmployeeDTO));
        verify(employeeRepository, times(1)).findById(1L);
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    // ========== softDelete Tests ==========

    @Test
    void testSoftDelete_WithValidId_MarksEmployeeAsDeleted() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        employeeService.softDelete(1L);

        // Assert
        assertTrue(testEmployee.isDeleted());
        verify(employeeRepository, times(1)).findById(1L);
        verify(employeeRepository, times(1)).save(testEmployee);
    }

    @Test
    void testSoftDelete_WithNonExistentId_ThrowsNotFoundException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> employeeService.softDelete(999L));
        verify(employeeRepository, times(1)).findById(999L);
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void testSoftDelete_WithNullId_ThrowsIllegalArgumentException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> employeeService.softDelete(null));
        verify(employeeRepository, never()).findById(anyLong());
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void testSoftDelete_WithAlreadyDeletedEmployee_ThrowsIllegalArgumentException() {
        // Arrange
        testEmployee.setDeleted(true);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> employeeService.softDelete(1L));
        verify(employeeRepository, times(1)).findById(1L);
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void testSoftDelete_WithNegativeId_ThrowsIllegalArgumentException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> employeeService.softDelete(-1L));
        verify(employeeRepository, never()).findById(anyLong());
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    // ========== Edge Case Tests ==========

    @Test
    void testCreate_WithSpecialCharactersInName_HandlesGracefully() {
        // Arrange
        testEmployeeDTO.setName("John O'Brien-Smith");
        when(employeeRepository.existsByBadgeId("EMP001")).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        EmployeeDTO result = employeeService.create(testEmployeeDTO);

        // Assert
        assertNotNull(result);
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    void testCreate_WithUnicodeCharactersInName_HandlesGracefully() {
        // Arrange
        testEmployeeDTO.setName("JosÃ© GarcÃ­a");
        when(employeeRepository.existsByBadgeId("EMP001")).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        EmployeeDTO result = employeeService.create(testEmployeeDTO);

        // Assert
        assertNotNull(result);
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    void testCreate_WithWhitespaceOnlyName_ThrowsIllegalArgumentException() {
        // Arrange
        testEmployeeDTO.setName("   ");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> employeeService.create(testEmployeeDTO));
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void testGetAll_WithMultiplePages_ReturnsCorrectPage() {
        // Arrange
        Pageable pageable = PageRequest.of(1, 10);
        List<Employee> employees = Arrays.asList(testEmployee);
        Page<Employee> page = new PageImpl<>(employees, pageable, 25);
        when(employeeRepository.findAll(pageable)).thenReturn(page);

        // Act
        Page<EmployeeDTO> result = employeeService.getAll(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(25, result.getTotalElements());
        assertEquals(1, result.getNumber());
        verify(employeeRepository, times(1)).findAll(pageable);
    }
}