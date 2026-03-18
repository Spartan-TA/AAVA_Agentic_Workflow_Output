package com.company.wms.employee.service;

import com.company.wms.audit.service.AuditService;
import com.company.wms.common.exception.DuplicateBadgeIdException;
import com.company.wms.common.exception.EmployeeNotFoundException;
import com.company.wms.employee.dto.EmployeeCreateDTO;
import com.company.wms.employee.dto.EmployeeDTO;
import com.company.wms.employee.dto.EmployeeFilterDTO;
import com.company.wms.employee.dto.EmployeeUpdateDTO;
import com.company.wms.employee.entity.Employee;
import com.company.wms.employee.entity.EmployeeRole;
import com.company.wms.employee.entity.EmployeeStatus;
import com.company.wms.employee.mapper.EmployeeMapper;
import com.company.wms.employee.repository.EmployeeRepository;
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
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive JUnit test suite for EmployeeService
 * Covers all CRUD operations, validation, edge cases, and exception scenarios
 */
@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private EmployeeMapper employeeMapper;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private EmployeeService employeeService;

    private Employee testEmployee;
    private EmployeeDTO testEmployeeDTO;
    private EmployeeCreateDTO testCreateDTO;
    private EmployeeUpdateDTO testUpdateDTO;

    @BeforeEach
    void setUp() {
        // Setup test data
        testEmployee = Employee.builder()
                .id(1L)
                .badgeId("EMP001")
                .name("John Doe")
                .role(EmployeeRole.WORKER)
                .department("Warehouse")
                .shiftGroup("Day Shift")
                .hireDate(LocalDate.of(2024, 1, 1))
                .status(EmployeeStatus.ACTIVE)
                .deleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testEmployeeDTO = EmployeeDTO.builder()
                .id(1L)
                .badgeId("EMP001")
                .name("John Doe")
                .role(EmployeeRole.WORKER)
                .department("Warehouse")
                .shiftGroup("Day Shift")
                .hireDate(LocalDate.of(2024, 1, 1))
                .status(EmployeeStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testCreateDTO = new EmployeeCreateDTO();
        testCreateDTO.setBadgeId("EMP001");
        testCreateDTO.setName("John Doe");
        testCreateDTO.setRole(EmployeeRole.WORKER);
        testCreateDTO.setDepartment("Warehouse");
        testCreateDTO.setShiftGroup("Day Shift");
        testCreateDTO.setHireDate(LocalDate.of(2024, 1, 1));

        testUpdateDTO = new EmployeeUpdateDTO();
        testUpdateDTO.setName("John Updated");
        testUpdateDTO.setDepartment("Logistics");
    }

    // ==================== CREATE EMPLOYEE TESTS ====================

    @Test
    void testCreateEmployee_ValidInput_Success() {
        // Arrange
        when(employeeRepository.findByBadgeIdAndDeletedFalse("EMP001"))
                .thenReturn(Optional.empty());
        when(employeeMapper.toEntity(testCreateDTO)).thenReturn(testEmployee);
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);
        when(employeeMapper.toDTO(testEmployee)).thenReturn(testEmployeeDTO);

        // Act
        EmployeeDTO result = employeeService.createEmployee(testCreateDTO);

        // Assert
        assertNotNull(result);
        assertEquals("EMP001", result.getBadgeId());
        assertEquals("John Doe", result.getName());
        assertEquals(EmployeeRole.WORKER, result.getRole());
        assertEquals(EmployeeStatus.ACTIVE, result.getStatus());
        verify(employeeRepository).save(any(Employee.class));
        verify(auditService).logCreate(eq("Employee"), eq(1L), any(Employee.class));
    }

    @Test
    void testCreateEmployee_DuplicateBadgeId_ThrowsException() {
        // Arrange
        when(employeeRepository.findByBadgeIdAndDeletedFalse("EMP001"))
                .thenReturn(Optional.of(testEmployee));

        // Act & Assert
        DuplicateBadgeIdException exception = assertThrows(
                DuplicateBadgeIdException.class,
                () -> employeeService.createEmployee(testCreateDTO)
        );

        assertTrue(exception.getMessage().contains("Badge ID already exists"));
        verify(employeeRepository, never()).save(any(Employee.class));
        verify(auditService, never()).logCreate(anyString(), anyLong(), any());
    }

    @Test
    void testCreateEmployee_NullBadgeId_ThrowsException() {
        // Arrange
        testCreateDTO.setBadgeId(null);

        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            employeeService.createEmployee(testCreateDTO);
        });
    }

    @Test
    void testCreateEmployee_EmptyName_ThrowsException() {
        // Arrange
        testCreateDTO.setName("");
        when(employeeRepository.findByBadgeIdAndDeletedFalse("EMP001"))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.createEmployee(testCreateDTO);
        });
    }

    @Test
    void testCreateEmployee_FutureHireDate_ThrowsException() {
        // Arrange
        testCreateDTO.setHireDate(LocalDate.now().plusDays(10));
        when(employeeRepository.findByBadgeIdAndDeletedFalse("EMP001"))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.createEmployee(testCreateDTO);
        });
    }

    @Test
    void testCreateEmployee_MaxLengthBadgeId_Success() {
        // Arrange
        String maxBadgeId = "A".repeat(50); // Max 50 characters
        testCreateDTO.setBadgeId(maxBadgeId);
        testEmployee.setBadgeId(maxBadgeId);

        when(employeeRepository.findByBadgeIdAndDeletedFalse(maxBadgeId))
                .thenReturn(Optional.empty());
        when(employeeMapper.toEntity(testCreateDTO)).thenReturn(testEmployee);
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);
        when(employeeMapper.toDTO(testEmployee)).thenReturn(testEmployeeDTO);

        // Act
        EmployeeDTO result = employeeService.createEmployee(testCreateDTO);

        // Assert
        assertNotNull(result);
        verify(employeeRepository).save(any(Employee.class));
    }

    // ==================== LIST EMPLOYEES TESTS ====================

    @Test
    void testListEmployees_ValidFilter_ReturnsPagedResults() {
        // Arrange
        EmployeeFilterDTO filter = new EmployeeFilterDTO();
        filter.setDepartment("Warehouse");
        filter.setStatus(EmployeeStatus.ACTIVE);

        Pageable pageable = PageRequest.of(0, 20);
        List<Employee> employees = Arrays.asList(testEmployee);
        Page<Employee> employeePage = new PageImpl<>(employees, pageable, 1);

        when(employeeRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(employeePage);
        when(employeeMapper.toDTO(testEmployee)).thenReturn(testEmployeeDTO);

        // Act
        Page<EmployeeDTO> result = employeeService.listEmployees(filter, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("EMP001", result.getContent().get(0).getBadgeId());
        verify(employeeRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void testListEmployees_EmptyFilter_ReturnsAllActiveEmployees() {
        // Arrange
        EmployeeFilterDTO filter = new EmployeeFilterDTO();
        Pageable pageable = PageRequest.of(0, 20);
        List<Employee> employees = Arrays.asList(testEmployee);
        Page<Employee> employeePage = new PageImpl<>(employees, pageable, 1);

        when(employeeRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(employeePage);
        when(employeeMapper.toDTO(testEmployee)).thenReturn(testEmployeeDTO);

        // Act
        Page<EmployeeDTO> result = employeeService.listEmployees(filter, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void testListEmployees_SearchByName_ReturnsMatchingEmployees() {
        // Arrange
        EmployeeFilterDTO filter = new EmployeeFilterDTO();
        filter.setSearch("John");

        Pageable pageable = PageRequest.of(0, 20);
        List<Employee> employees = Arrays.asList(testEmployee);
        Page<Employee> employeePage = new PageImpl<>(employees, pageable, 1);

        when(employeeRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(employeePage);
        when(employeeMapper.toDTO(testEmployee)).thenReturn(testEmployeeDTO);

        // Act
        Page<EmployeeDTO> result = employeeService.listEmployees(filter, pageable);

        // Assert
        assertNotNull(result);
        assertTrue(result.getContent().get(0).getName().contains("John"));
    }

    @Test
    void testListEmployees_NoResults_ReturnsEmptyPage() {
        // Arrange
        EmployeeFilterDTO filter = new EmployeeFilterDTO();
        Pageable pageable = PageRequest.of(0, 20);
        Page<Employee> emptyPage = new PageImpl<>(Arrays.asList(), pageable, 0);

        when(employeeRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(emptyPage);

        // Act
        Page<EmployeeDTO> result = employeeService.listEmployees(filter, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }

    // ==================== GET EMPLOYEE BY ID TESTS ====================

    @Test
    void testGetEmployeeById_ValidId_ReturnsEmployee() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeMapper.toDTO(testEmployee)).thenReturn(testEmployeeDTO);

        // Act
        EmployeeDTO result = employeeService.getEmployeeById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("EMP001", result.getBadgeId());
        verify(employeeRepository).findById(1L);
    }

    @Test
    void testGetEmployeeById_NonExistentId_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        EmployeeNotFoundException exception = assertThrows(
                EmployeeNotFoundException.class,
                () -> employeeService.getEmployeeById(999L)
        );

        assertTrue(exception.getMessage().contains("Employee not found"));
        verify(employeeRepository).findById(999L);
    }

    @Test
    void testGetEmployeeById_DeletedEmployee_ThrowsException() {
        // Arrange
        testEmployee.setDeleted(true);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));

        // Act & Assert
        EmployeeNotFoundException exception = assertThrows(
                EmployeeNotFoundException.class,
                () -> employeeService.getEmployeeById(1L)
        );

        assertTrue(exception.getMessage().contains("Employee not found"));
    }

    @Test
    void testGetEmployeeById_NullId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.getEmployeeById(null);
        });
    }

    @Test
    void testGetEmployeeById_NegativeId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.getEmployeeById(-1L);
        });
    }

    // ==================== UPDATE EMPLOYEE TESTS ====================

    @Test
    void testUpdateEmployee_ValidUpdate_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);
        when(employeeMapper.toDTO(testEmployee)).thenReturn(testEmployeeDTO);
        doNothing().when(employeeMapper).updateEntity(testUpdateDTO, testEmployee);

        // Act
        EmployeeDTO result = employeeService.updateEmployee(1L, testUpdateDTO);

        // Assert
        assertNotNull(result);
        verify(employeeRepository).save(any(Employee.class));
        verify(auditService).logUpdate(eq("Employee"), eq(1L), any(), any());
    }

    @Test
    void testUpdateEmployee_NonExistentEmployee_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        EmployeeNotFoundException exception = assertThrows(
                EmployeeNotFoundException.class,
                () -> employeeService.updateEmployee(999L, testUpdateDTO)
        );

        assertTrue(exception.getMessage().contains("Employee not found"));
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void testUpdateEmployee_DeletedEmployee_ThrowsException() {
        // Arrange
        testEmployee.setDeleted(true);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));

        // Act & Assert
        EmployeeNotFoundException exception = assertThrows(
                EmployeeNotFoundException.class,
                () -> employeeService.updateEmployee(1L, testUpdateDTO)
        );

        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void testUpdateEmployee_PartialUpdate_Success() {
        // Arrange
        EmployeeUpdateDTO partialUpdate = new EmployeeUpdateDTO();
        partialUpdate.setName("Updated Name");
        // Other fields are null

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);
        when(employeeMapper.toDTO(testEmployee)).thenReturn(testEmployeeDTO);
        doNothing().when(employeeMapper).updateEntity(partialUpdate, testEmployee);

        // Act
        EmployeeDTO result = employeeService.updateEmployee(1L, partialUpdate);

        // Assert
        assertNotNull(result);
        verify(employeeRepository).save(any(Employee.class));
    }

    @Test
    void testUpdateEmployee_NullUpdateDTO_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.updateEmployee(1L, null);
        });
    }

    // ==================== DELETE EMPLOYEE TESTS ====================

    @Test
    void testDeleteEmployee_ValidId_SoftDeleteSuccess() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        employeeService.deleteEmployee(1L);

        // Assert
        verify(employeeRepository).save(argThat(employee -> 
            employee.getDeleted() && employee.getStatus() == EmployeeStatus.TERMINATED
        ));
        verify(auditService).logDelete("Employee", 1L);
    }

    @Test
    void testDeleteEmployee_NonExistentEmployee_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        EmployeeNotFoundException exception = assertThrows(
                EmployeeNotFoundException.class,
                () -> employeeService.deleteEmployee(999L)
        );

        assertTrue(exception.getMessage().contains("Employee not found"));
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void testDeleteEmployee_AlreadyDeleted_ThrowsException() {
        // Arrange
        testEmployee.setDeleted(true);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));

        // Act & Assert
        EmployeeNotFoundException exception = assertThrows(
                EmployeeNotFoundException.class,
                () -> employeeService.deleteEmployee(1L)
        );

        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void testDeleteEmployee_NullId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.deleteEmployee(null);
        });
    }

    // ==================== EDGE CASE TESTS ====================

    @Test
    void testCreateEmployee_SpecialCharactersInName_Success() {
        // Arrange
        testCreateDTO.setName("O'Brien-Smith Jr.");
        testEmployee.setName("O'Brien-Smith Jr.");

        when(employeeRepository.findByBadgeIdAndDeletedFalse("EMP001"))
                .thenReturn(Optional.empty());
        when(employeeMapper.toEntity(testCreateDTO)).thenReturn(testEmployee);
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);
        when(employeeMapper.toDTO(testEmployee)).thenReturn(testEmployeeDTO);

        // Act
        EmployeeDTO result = employeeService.createEmployee(testCreateDTO);

        // Assert
        assertNotNull(result);
        verify(employeeRepository).save(any(Employee.class));
    }

    @Test
    void testListEmployees_LargePage_ReturnsCorrectly() {
        // Arrange
        EmployeeFilterDTO filter = new EmployeeFilterDTO();
        Pageable pageable = PageRequest.of(0, 1000);
        List<Employee> employees = Arrays.asList(testEmployee);
        Page<Employee> employeePage = new PageImpl<>(employees, pageable, 1);

        when(employeeRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(employeePage);
        when(employeeMapper.toDTO(testEmployee)).thenReturn(testEmployeeDTO);

        // Act
        Page<EmployeeDTO> result = employeeService.listEmployees(filter, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void testUpdateEmployee_ChangeStatus_Success() {
        // Arrange
        testUpdateDTO.setStatus(EmployeeStatus.ON_LEAVE);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);
        when(employeeMapper.toDTO(testEmployee)).thenReturn(testEmployeeDTO);
        doNothing().when(employeeMapper).updateEntity(testUpdateDTO, testEmployee);

        // Act
        EmployeeDTO result = employeeService.updateEmployee(1L, testUpdateDTO);

        // Assert
        assertNotNull(result);
        verify(employeeRepository).save(any(Employee.class));
    }
}
