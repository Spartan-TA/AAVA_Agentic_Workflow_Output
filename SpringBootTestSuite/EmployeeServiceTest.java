package com.warehouse.employee.service.impl;

import com.warehouse.employee.domain.Employee;
import com.warehouse.employee.dto.EmployeeDTO;
import com.warehouse.employee.repository.EmployeeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive JUnit test suite for EmployeeServiceImpl.
 * Tests service layer business logic with mocked dependencies.
 */
@ExtendWith(MockitoExtension.class)
public class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    private EmployeeDTO testEmployeeDTO;
    private Employee testEmployee;

    @BeforeEach
    public void setUp() {
        testEmployeeDTO = EmployeeDTO.builder()
                .id(1L)
                .name("John Doe")
                .badgeId("EMP001")
                .role("WORKER")
                .department("Warehouse")
                .shiftGroup("Morning")
                .hireDate(LocalDate.of(2023, 1, 15))
                .status("ACTIVE")
                .build();

        testEmployee = Employee.builder()
                .id(1L)
                .name("John Doe")
                .badgeId("EMP001")
                .role("WORKER")
                .department("Warehouse")
                .shiftGroup("Morning")
                .hireDate(LocalDate.of(2023, 1, 15))
                .status("ACTIVE")
                .build();
    }

    // ========== CREATE EMPLOYEE TESTS ==========

    @Test
    public void testCreateEmployee_WithValidData_ReturnsCreatedEmployee() {
        // Arrange
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        EmployeeDTO result = employeeService.createEmployee(testEmployeeDTO);

        // Assert
        assertNotNull(result);
        assertEquals("John Doe", result.getName());
        assertEquals("EMP001", result.getBadgeId());
        assertEquals("WORKER", result.getRole());
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    public void testCreateEmployee_WithMinimalData_Success() {
        // Arrange
        EmployeeDTO minimalDTO = EmployeeDTO.builder()
                .name("Jane Smith")
                .badgeId("EMP002")
                .role("SUPERVISOR")
                .department("Logistics")
                .status("ACTIVE")
                .build();

        Employee minimalEmployee = Employee.builder()
                .id(2L)
                .name("Jane Smith")
                .badgeId("EMP002")
                .role("SUPERVISOR")
                .department("Logistics")
                .status("ACTIVE")
                .build();

        when(employeeRepository.save(any(Employee.class))).thenReturn(minimalEmployee);

        // Act
        EmployeeDTO result = employeeService.createEmployee(minimalDTO);

        // Assert
        assertNotNull(result);
        assertEquals("Jane Smith", result.getName());
        assertNull(result.getShiftGroup());
        assertNull(result.getHireDate());
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    public void testCreateEmployee_WithNullDTO_ThrowsException() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            employeeService.createEmployee(null);
        });
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    public void testCreateEmployee_VerifiesAllFieldsAreMapped() {
        // Arrange
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        EmployeeDTO result = employeeService.createEmployee(testEmployeeDTO);

        // Assert
        assertEquals(testEmployeeDTO.getName(), result.getName());
        assertEquals(testEmployeeDTO.getBadgeId(), result.getBadgeId());
        assertEquals(testEmployeeDTO.getRole(), result.getRole());
        assertEquals(testEmployeeDTO.getDepartment(), result.getDepartment());
        assertEquals(testEmployeeDTO.getShiftGroup(), result.getShiftGroup());
        assertEquals(testEmployeeDTO.getHireDate(), result.getHireDate());
        assertEquals(testEmployeeDTO.getStatus(), result.getStatus());
    }

    // ========== GET EMPLOYEE TESTS ==========

    @Test
    public void testGetEmployee_WithExistingId_ReturnsEmployee() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));

        // Act
        Optional<EmployeeDTO> result = employeeService.getEmployee(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("John Doe", result.get().getName());
        assertEquals("EMP001", result.get().getBadgeId());
        verify(employeeRepository, times(1)).findById(1L);
    }

    @Test
    public void testGetEmployee_WithNonExistingId_ReturnsEmpty() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<EmployeeDTO> result = employeeService.getEmployee(999L);

        // Assert
        assertFalse(result.isPresent());
        verify(employeeRepository, times(1)).findById(999L);
    }

    @Test
    public void testGetEmployee_WithDeletedStatus_ReturnsEmpty() {
        // Arrange
        Employee deletedEmployee = Employee.builder()
                .id(1L)
                .name("Deleted User")
                .badgeId("EMP003")
                .role("WORKER")
                .department("Warehouse")
                .status("DELETED")
                .build();

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(deletedEmployee));

        // Act
        Optional<EmployeeDTO> result = employeeService.getEmployee(1L);

        // Assert
        assertFalse(result.isPresent(), "Deleted employees should not be returned");
        verify(employeeRepository, times(1)).findById(1L);
    }

    @Test
    public void testGetEmployee_WithNullId_ReturnsEmpty() {
        // Arrange
        when(employeeRepository.findById(null)).thenReturn(Optional.empty());

        // Act
        Optional<EmployeeDTO> result = employeeService.getEmployee(null);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    public void testGetEmployee_WithZeroId_ReturnsEmpty() {
        // Arrange
        when(employeeRepository.findById(0L)).thenReturn(Optional.empty());

        // Act
        Optional<EmployeeDTO> result = employeeService.getEmployee(0L);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    public void testGetEmployee_WithNegativeId_ReturnsEmpty() {
        // Arrange
        when(employeeRepository.findById(-1L)).thenReturn(Optional.empty());

        // Act
        Optional<EmployeeDTO> result = employeeService.getEmployee(-1L);

        // Assert
        assertFalse(result.isPresent());
    }

    // ========== UPDATE EMPLOYEE TESTS ==========

    @Test
    public void testUpdateEmployee_WithValidData_ReturnsUpdatedEmployee() {
        // Arrange
        EmployeeDTO updateDTO = EmployeeDTO.builder()
                .name("John Updated")
                .role("SUPERVISOR")
                .department("New Department")
                .shiftGroup("Evening")
                .status("ACTIVE")
                .build();

        Employee updatedEmployee = Employee.builder()
                .id(1L)
                .name("John Updated")
                .badgeId("EMP001")
                .role("SUPERVISOR")
                .department("New Department")
                .shiftGroup("Evening")
                .hireDate(LocalDate.of(2023, 1, 15))
                .status("ACTIVE")
                .build();

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(updatedEmployee);

        // Act
        EmployeeDTO result = employeeService.updateEmployee(1L, updateDTO);

        // Assert
        assertNotNull(result);
        assertEquals("John Updated", result.getName());
        assertEquals("SUPERVISOR", result.getRole());
        assertEquals("New Department", result.getDepartment());
        assertEquals("Evening", result.getShiftGroup());
        verify(employeeRepository, times(1)).findById(1L);
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    public void testUpdateEmployee_WithNonExistingId_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.updateEmployee(999L, testEmployeeDTO);
        });
        verify(employeeRepository, times(1)).findById(999L);
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    public void testUpdateEmployee_DoesNotChangeBadgeId() {
        // Arrange
        EmployeeDTO updateDTO = EmployeeDTO.builder()
                .name("John Updated")
                .badgeId("NEWBADGE") // Attempt to change badgeId
                .role("SUPERVISOR")
                .department("Warehouse")
                .status("ACTIVE")
                .build();

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        EmployeeDTO result = employeeService.updateEmployee(1L, updateDTO);

        // Assert
        assertEquals("EMP001", result.getBadgeId(), "BadgeId should not be changed");
        assertNotEquals("NEWBADGE", result.getBadgeId());
    }

    @Test
    public void testUpdateEmployee_DoesNotChangeHireDate() {
        // Arrange
        LocalDate newHireDate = LocalDate.of(2024, 1, 1);
        EmployeeDTO updateDTO = EmployeeDTO.builder()
                .name("John Updated")
                .role("SUPERVISOR")
                .department("Warehouse")
                .hireDate(newHireDate) // Attempt to change hireDate
                .status("ACTIVE")
                .build();

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        EmployeeDTO result = employeeService.updateEmployee(1L, updateDTO);

        // Assert
        assertEquals(LocalDate.of(2023, 1, 15), result.getHireDate(), "HireDate should not be changed");
        assertNotEquals(newHireDate, result.getHireDate());
    }

    @Test
    public void testUpdateEmployee_WithNullDTO_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));

        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            employeeService.updateEmployee(1L, null);
        });
    }

    @Test
    public void testUpdateEmployee_WithPartialData_UpdatesOnlyProvidedFields() {
        // Arrange
        EmployeeDTO partialUpdateDTO = EmployeeDTO.builder()
                .name("John Partially Updated")
                .role("WORKER")
                .department("Warehouse")
                .status("ACTIVE")
                .build();

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        EmployeeDTO result = employeeService.updateEmployee(1L, partialUpdateDTO);

        // Assert
        assertEquals("John Partially Updated", result.getName());
        assertEquals("EMP001", result.getBadgeId()); // Should remain unchanged
    }

    // ========== DELETE EMPLOYEE TESTS ==========

    @Test
    public void testDeleteEmployee_WithExistingId_SetsStatusToDeleted() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        employeeService.deleteEmployee(1L);

        // Assert
        verify(employeeRepository, times(1)).findById(1L);
        verify(employeeRepository, times(1)).save(argThat(employee -> 
            "DELETED".equals(employee.getStatus())
        ));
    }

    @Test
    public void testDeleteEmployee_WithNonExistingId_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.deleteEmployee(999L);
        });
        verify(employeeRepository, times(1)).findById(999L);
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    public void testDeleteEmployee_WithNullId_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(null)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.deleteEmployee(null);
        });
    }

    @Test
    public void testDeleteEmployee_AlreadyDeleted_StillSetsStatusToDeleted() {
        // Arrange
        Employee alreadyDeletedEmployee = Employee.builder()
                .id(1L)
                .name("Already Deleted")
                .badgeId("EMP004")
                .role("WORKER")
                .department("Warehouse")
                .status("DELETED")
                .build();

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(alreadyDeletedEmployee));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        employeeService.deleteEmployee(1L);

        // Assert
        verify(employeeRepository, times(1)).save(argThat(employee -> 
            "DELETED".equals(employee.getStatus())
        ));
    }

    // ========== LIST EMPLOYEES TESTS ==========

    @Test
    public void testListEmployees_WithoutStatusFilter_ReturnsAllActive() {
        // Arrange
        Employee employee1 = testEmployee;
        Employee employee2 = Employee.builder()
                .id(2L)
                .name("Jane Smith")
                .badgeId("EMP002")
                .role("SUPERVISOR")
                .department("Logistics")
                .status("ACTIVE")
                .build();

        List<Employee> employees = Arrays.asList(employee1, employee2);
        Page<Employee> employeePage = new PageImpl<>(employees);
        Pageable pageable = PageRequest.of(0, 10);

        when(employeeRepository.findAllActive(pageable)).thenReturn(employeePage);

        // Act
        Page<EmployeeDTO> result = employeeService.listEmployees(null, pageable);

        // Assert
        assertEquals(2, result.getTotalElements());
        assertEquals("John Doe", result.getContent().get(0).getName());
        assertEquals("Jane Smith", result.getContent().get(1).getName());
        verify(employeeRepository, times(1)).findAllActive(pageable);
        verify(employeeRepository, never()).findByStatus(anyString(), any(Pageable.class));
    }

    @Test
    public void testListEmployees_WithStatusFilter_ReturnsFilteredEmployees() {
        // Arrange
        List<Employee> activeEmployees = Arrays.asList(testEmployee);
        Page<Employee> employeePage = new PageImpl<>(activeEmployees);
        Pageable pageable = PageRequest.of(0, 10);

        when(employeeRepository.findByStatus("ACTIVE", pageable)).thenReturn(employeePage);

        // Act
        Page<EmployeeDTO> result = employeeService.listEmployees("ACTIVE", pageable);

        // Assert
        assertEquals(1, result.getTotalElements());
        assertEquals("ACTIVE", result.getContent().get(0).getStatus());
        verify(employeeRepository, times(1)).findByStatus("ACTIVE", pageable);
        verify(employeeRepository, never()).findAllActive(any(Pageable.class));
    }

    @Test
    public void testListEmployees_WithInactiveStatus_ReturnsInactiveEmployees() {
        // Arrange
        Employee inactiveEmployee = Employee.builder()
                .id(3L)
                .name("Inactive User")
                .badgeId("EMP005")
                .role("WORKER")
                .department("Warehouse")
                .status("INACTIVE")
                .build();

        List<Employee> inactiveEmployees = Arrays.asList(inactiveEmployee);
        Page<Employee> employeePage = new PageImpl<>(inactiveEmployees);
        Pageable pageable = PageRequest.of(0, 10);

        when(employeeRepository.findByStatus("INACTIVE", pageable)).thenReturn(employeePage);

        // Act
        Page<EmployeeDTO> result = employeeService.listEmployees("INACTIVE", pageable);

        // Assert
        assertEquals(1, result.getTotalElements());
        assertEquals("INACTIVE", result.getContent().get(0).getStatus());
    }

    @Test
    public void testListEmployees_WithEmptyResult_ReturnsEmptyPage() {
        // Arrange
        Page<Employee> emptyPage = new PageImpl<>(Arrays.asList());
        Pageable pageable = PageRequest.of(0, 10);

        when(employeeRepository.findAllActive(pageable)).thenReturn(emptyPage);

        // Act
        Page<EmployeeDTO> result = employeeService.listEmployees(null, pageable);

        // Assert
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }

    @Test
    public void testListEmployees_WithPagination_ReturnsCorrectPage() {
        // Arrange
        List<Employee> employees = Arrays.asList(testEmployee);
        Page<Employee> employeePage = new PageImpl<>(employees, PageRequest.of(1, 5), 10);
        Pageable pageable = PageRequest.of(1, 5);

        when(employeeRepository.findAllActive(pageable)).thenReturn(employeePage);

        // Act
        Page<EmployeeDTO> result = employeeService.listEmployees(null, pageable);

        // Assert
        assertEquals(10, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        assertEquals(1, result.getNumber());
    }

    @Test
    public void testListEmployees_WithSorting_ReturnsSortedResults() {
        // Arrange
        Employee employee1 = testEmployee;
        Employee employee2 = Employee.builder()
                .id(2L)
                .name("Alice Smith")
                .badgeId("EMP002")
                .role("WORKER")
                .department("Warehouse")
                .status("ACTIVE")
                .build();

        List<Employee> sortedEmployees = Arrays.asList(employee2, employee1);
        Page<Employee> employeePage = new PageImpl<>(sortedEmployees);
        Pageable pageable = PageRequest.of(0, 10, Sort.by("name").ascending());

        when(employeeRepository.findAllActive(pageable)).thenReturn(employeePage);

        // Act
        Page<EmployeeDTO> result = employeeService.listEmployees(null, pageable);

        // Assert
        assertEquals(2, result.getTotalElements());
        assertEquals("Alice Smith", result.getContent().get(0).getName());
        assertEquals("John Doe", result.getContent().get(1).getName());
    }

    // ========== EDGE CASE TESTS ==========

    @Test
    public void testListEmployees_WithNullPageable_ThrowsException() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            employeeService.listEmployees(null, null);
        });
    }

    @Test
    public void testCreateEmployee_WithAllNullOptionalFields_Success() {
        // Arrange
        EmployeeDTO dtoWithNulls = EmployeeDTO.builder()
                .name("Test Employee")
                .badgeId("EMP006")
                .role("WORKER")
                .department("Warehouse")
                .shiftGroup(null)
                .hireDate(null)
                .status("ACTIVE")
                .build();

        Employee employeeWithNulls = Employee.builder()
                .id(6L)
                .name("Test Employee")
                .badgeId("EMP006")
                .role("WORKER")
                .department("Warehouse")
                .status("ACTIVE")
                .build();

        when(employeeRepository.save(any(Employee.class))).thenReturn(employeeWithNulls);

        // Act
        EmployeeDTO result = employeeService.createEmployee(dtoWithNulls);

        // Assert
        assertNotNull(result);
        assertNull(result.getShiftGroup());
        assertNull(result.getHireDate());
    }

    @Test
    public void testGetEmployee_VerifiesRepositoryCalledOnce() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));

        // Act
        employeeService.getEmployee(1L);

        // Assert
        verify(employeeRepository, times(1)).findById(1L);
        verifyNoMoreInteractions(employeeRepository);
    }

    @Test
    public void testUpdateEmployee_VerifiesRepositoryCalledTwice() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        employeeService.updateEmployee(1L, testEmployeeDTO);

        // Assert
        verify(employeeRepository, times(1)).findById(1L);
        verify(employeeRepository, times(1)).save(any(Employee.class));
        verifyNoMoreInteractions(employeeRepository);
    }

    @Test
    public void testDeleteEmployee_VerifiesRepositoryCalledTwice() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        employeeService.deleteEmployee(1L);

        // Assert
        verify(employeeRepository, times(1)).findById(1L);
        verify(employeeRepository, times(1)).save(any(Employee.class));
        verifyNoMoreInteractions(employeeRepository);
    }
}