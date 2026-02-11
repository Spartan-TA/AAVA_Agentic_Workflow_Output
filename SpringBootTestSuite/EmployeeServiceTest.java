package com.warehouse.ems.employee;

import com.warehouse.ems.audit.AuditLogService;
import com.warehouse.ems.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmployeeServiceTest {
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private AuditLogService auditLogService;
    @InjectMocks
    private EmployeeService employeeService;

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllEmployees_ReturnsActiveEmployees() {
        List<Employee> employees = Arrays.asList(
            Employee.builder().id(1L).name("John Doe").deleted(false).build(),
            Employee.builder().id(2L).name("Jane Smith").deleted(false).build()
        );
        when(employeeRepository.findAllActive()).thenReturn(employees);

        List<Employee> result = employeeService.getAllEmployees();

        assertThat(result).hasSize(2).extracting("name").contains("John Doe", "Jane Smith");
        verify(employeeRepository).findAllActive();
    }

    @Test
    void testGetEmployeeById_ValidId_ReturnsEmployee() {
        Employee employee = Employee.builder().id(1L).name("John Doe").deleted(false).build();
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        Employee result = employeeService.getEmployeeById(1L);

        assertThat(result).isEqualTo(employee);
        verify(employeeRepository).findById(1L);
    }

    @Test
    void testGetEmployeeById_InvalidId_ThrowsResourceNotFoundException() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.getEmployeeById(99L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Employee not found with id: 99");
        verify(employeeRepository).findById(99L);
    }

    @Test
    void testGetEmployeeById_DeletedEmployee_ThrowsResourceNotFoundException() {
        Employee employee = Employee.builder().id(1L).name("John Doe").deleted(true).build();
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        assertThatThrownBy(() -> employeeService.getEmployeeById(1L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Employee not found with id: 1");
        verify(employeeRepository).findById(1L);
    }

    @Test
    void testCreateEmployee_ValidData_ReturnsEmployee() {
        Employee employee = Employee.builder()
            .name("John Doe")
            .badgeId("BADGE123")
            .role("Worker")
            .department("Logistics")
            .shiftGroup("A")
            .hireDate(LocalDate.now())
            .status("Active")
            .build();
        when(employeeRepository.findByBadgeId("BADGE123")).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> {
            Employee e = invocation.getArgument(0);
            e.setId(1L);
            return e;
        });

        Employee result = employeeService.createEmployee(employee);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getCreatedAt()).isNotNull();
        assertThat(result.getUpdatedAt()).isNotNull();
        verify(employeeRepository).findByBadgeId("BADGE123");
        verify(employeeRepository).save(any(Employee.class));
        verify(auditLogService).logCreate(eq("Employee"), isNull(), any(Employee.class));
    }

    @Test
    void testCreateEmployee_DuplicateBadgeId_ThrowsIllegalArgumentException() {
        Employee employee = Employee.builder()
            .name("John Doe")
            .badgeId("BADGE123")
            .role("Worker")
            .department("Logistics")
            .hireDate(LocalDate.now())
            .status("Active")
            .build();
        when(employeeRepository.findByBadgeId("BADGE123")).thenReturn(Optional.of(employee));

        assertThatThrownBy(() -> employeeService.createEmployee(employee))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Badge ID already exists: BADGE123");
        verify(employeeRepository).findByBadgeId("BADGE123");
    }

    @Test
    void testCreateEmployee_NullName_ThrowsValidationException() {
        Employee employee = Employee.builder()
            .name(null)
            .badgeId("BADGE123")
            .role("Worker")
            .department("Logistics")
            .hireDate(LocalDate.now())
            .status("Active")
            .build();
        when(employeeRepository.findByBadgeId("BADGE123")).thenReturn(Optional.empty());
        // Simulate validation exception
        doThrow(new DataIntegrityViolationException("NotBlank violation")).when(employeeRepository).save(any(Employee.class));

        assertThatThrownBy(() -> employeeService.createEmployee(employee))
            .isInstanceOf(DataIntegrityViolationException.class)
            .hasMessageContaining("NotBlank violation");
        verify(employeeRepository).findByBadgeId("BADGE123");
        verify(employeeRepository).save(any(Employee.class));
    }

    @Test
    void testCreateEmployee_EmptyBadgeId_ThrowsValidationException() {
        Employee employee = Employee.builder()
            .name("John Doe")
            .badgeId("")
            .role("Worker")
            .department("Logistics")
            .hireDate(LocalDate.now())
            .status("Active")
            .build();
        when(employeeRepository.findByBadgeId("")).thenReturn(Optional.empty());
        doThrow(new DataIntegrityViolationException("NotBlank violation")).when(employeeRepository).save(any(Employee.class));

        assertThatThrownBy(() -> employeeService.createEmployee(employee))
            .isInstanceOf(DataIntegrityViolationException.class)
            .hasMessageContaining("NotBlank violation");
        verify(employeeRepository).findByBadgeId("");
        verify(employeeRepository).save(any(Employee.class));
    }

    @Test
    void testCreateEmployee_NameExceedsMaxLength_ThrowsValidationException() {
        String longName = "A".repeat(101);
        Employee employee = Employee.builder()
            .name(longName)
            .badgeId("BADGE123")
            .role("Worker")
            .department("Logistics")
            .hireDate(LocalDate.now())
            .status("Active")
            .build();
        when(employeeRepository.findByBadgeId("BADGE123")).thenReturn(Optional.empty());
        doThrow(new DataIntegrityViolationException("Size violation")).when(employeeRepository).save(any(Employee.class));

        assertThatThrownBy(() -> employeeService.createEmployee(employee))
            .isInstanceOf(DataIntegrityViolationException.class)
            .hasMessageContaining("Size violation");
        verify(employeeRepository).findByBadgeId("BADGE123");
        verify(employeeRepository).save(any(Employee.class));
    }

    @Test
    void testUpdateEmployee_ValidData_ReturnsUpdatedEmployee() {
        Employee existing = Employee.builder()
            .id(1L)
            .name("John Doe")
            .badgeId("BADGE123")
            .role("Worker")
            .department("Logistics")
            .shiftGroup("A")
            .hireDate(LocalDate.now())
            .status("Active")
            .deleted(false)
            .build();
        Employee updated = Employee.builder()
            .name("Jane Smith")
            .role("Supervisor")
            .department("Operations")
            .shiftGroup("B")
            .status("Inactive")
            .build();
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Employee result = employeeService.updateEmployee(1L, updated);

        assertThat(result.getName()).isEqualTo("Jane Smith");
        assertThat(result.getRole()).isEqualTo("Supervisor");
        assertThat(result.getDepartment()).isEqualTo("Operations");
        assertThat(result.getShiftGroup()).isEqualTo("B");
        assertThat(result.getStatus()).isEqualTo("Inactive");
        assertThat(result.getUpdatedAt()).isNotNull();
        verify(employeeRepository).findById(1L);
        verify(employeeRepository).save(any(Employee.class));
        verify(auditLogService).logUpdate(eq("Employee"), eq(existing), any(Employee.class));
    }

    @Test
    void testUpdateEmployee_InvalidId_ThrowsResourceNotFoundException() {
        Employee updated = Employee.builder().name("Jane Smith").build();
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.updateEmployee(99L, updated))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Employee not found with id: 99");
        verify(employeeRepository).findById(99L);
    }

    @Test
    void testDeleteEmployee_ValidId_SoftDeletesEmployee() {
        Employee employee = Employee.builder()
            .id(1L)
            .name("John Doe")
            .deleted(false)
            .updatedAt(LocalDateTime.now())
            .build();
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> invocation.getArgument(0));

        employeeService.deleteEmployee(1L);

        assertThat(employee.getDeleted()).isTrue();
        assertThat(employee.getUpdatedAt()).isNotNull();
        verify(employeeRepository).findById(1L);
        verify(employeeRepository).save(employee);
        verify(auditLogService).logDelete(eq("Employee"), eq(employee), eq(employee));
    }

    @Test
    void testDeleteEmployee_InvalidId_ThrowsResourceNotFoundException() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.deleteEmployee(99L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Employee not found with id: 99");
        verify(employeeRepository).findById(99L);
    }
}
