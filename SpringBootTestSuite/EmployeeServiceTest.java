package com.company.wem.employee;

import com.company.wem.employee.dto.EmployeeDTO;
import com.company.wem.employee.entity.Employee;
import com.company.wem.employee.repository.EmployeeRepository;
import com.company.wem.employee.service.EmployeeService;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.dao.DataIntegrityViolationException;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeService employeeService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateEmployee_ValidInput_Success() {
        EmployeeDTO dto = new EmployeeDTO("EMP001", "John Doe", "WORKER", "Warehouse", "A", LocalDate.now(), "ACTIVE");
        Employee employee = new Employee(1L, "EMP001", "John Doe", "WORKER", "Warehouse", "A", LocalDate.now(), "ACTIVE");
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);
        Employee result = employeeService.create(dto);
        assertNotNull(result);
        assertEquals("EMP001", result.getBadgeId());
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    void testCreateEmployee_NullBadgeId_ThrowsException() {
        EmployeeDTO dto = new EmployeeDTO(null, "John Doe", "WORKER", "Warehouse", "A", LocalDate.now(), "ACTIVE");
        assertThrows(IllegalArgumentException.class, () -> employeeService.create(dto));
    }

    @Test
    void testCreateEmployee_DuplicateBadgeId_ThrowsException() {
        EmployeeDTO dto = new EmployeeDTO("EMP001", "John Doe", "WORKER", "Warehouse", "A", LocalDate.now(), "ACTIVE");
        when(employeeRepository.findByBadgeId("EMP001")).thenReturn(Optional.of(new Employee()));
        assertThrows(DataIntegrityViolationException.class, () -> employeeService.create(dto));
    }

    @Test
    void testCreateEmployee_NullName_ThrowsException() {
        EmployeeDTO dto = new EmployeeDTO("EMP002", null, "WORKER", "Warehouse", "A", LocalDate.now(), "ACTIVE");
        assertThrows(IllegalArgumentException.class, () -> employeeService.create(dto));
    }

    @Test
    void testCreateEmployee_MaxLengthName_Success() {
        String longName = "A".repeat(255);
        EmployeeDTO dto = new EmployeeDTO("EMP003", longName, "WORKER", "Warehouse", "A", LocalDate.now(), "ACTIVE");
        Employee employee = new Employee(2L, "EMP003", longName, "WORKER", "Warehouse", "A", LocalDate.now(), "ACTIVE");
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);
        Employee result = employeeService.create(dto);
        assertEquals(longName, result.getName());
    }

    @Test
    void testCreateEmployee_InvalidRole_ThrowsException() {
        EmployeeDTO dto = new EmployeeDTO("EMP004", "Jane Doe", "INVALID_ROLE", "Warehouse", "A", LocalDate.now(), "ACTIVE");
        assertThrows(IllegalArgumentException.class, () -> employeeService.create(dto));
    }

    @Test
    void testCreateEmployee_SpecialCharactersInName_Success() {
        EmployeeDTO dto = new EmployeeDTO("EMP005", "J@ne D'oe!", "WORKER", "Warehouse", "A", LocalDate.now(), "ACTIVE");
        Employee employee = new Employee(3L, "EMP005", "J@ne D'oe!", "WORKER", "Warehouse", "A", LocalDate.now(), "ACTIVE");
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);
        Employee result = employeeService.create(dto);
        assertEquals("J@ne D'oe!", result.getName());
    }

    @Test
    void testCreateEmployee_SQLInjectionAttemptInName_ThrowsException() {
        EmployeeDTO dto = new EmployeeDTO("EMP006", "Robert'); DROP TABLE Employees;--", "WORKER", "Warehouse", "A", LocalDate.now(), "ACTIVE");
        assertThrows(IllegalArgumentException.class, () -> employeeService.create(dto));
    }

    @Test
    void testCreateEmployee_XSSAttemptInName_ThrowsException() {
        EmployeeDTO dto = new EmployeeDTO("EMP007", "<script>alert('xss')</script>", "WORKER", "Warehouse", "A", LocalDate.now(), "ACTIVE");
        assertThrows(IllegalArgumentException.class, () -> employeeService.create(dto));
    }

    @Test
    void testSoftDeleteEmployee_Success() {
        Employee employee = new Employee(4L, "EMP008", "Mark Smith", "WORKER", "Warehouse", "A", LocalDate.now(), "ACTIVE");
        when(employeeRepository.findById(4L)).thenReturn(Optional.of(employee));
        employee.setStatus("DELETED");
        when(employeeRepository.save(employee)).thenReturn(employee);
        Employee result = employeeService.softDelete(4L);
        assertEquals("DELETED", result.getStatus());
    }

    @Test
    void testSoftDeleteEmployee_NotFound_ThrowsException() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> employeeService.softDelete(99L));
    }

    @Test
    void testGetEmployees_PaginationFiltering_Success() {
        List<Employee> employees = Arrays.asList(
                new Employee(5L, "EMP009", "Alice", "WORKER", "Warehouse", "A", LocalDate.now(), "ACTIVE"),
                new Employee(6L, "EMP010", "Bob", "SUPERVISOR", "Warehouse", "B", LocalDate.now(), "ACTIVE")
        );
        when(employeeRepository.findAll(any())).thenReturn(employees);
        List<Employee> result = employeeService.getEmployees("Warehouse", "ACTIVE", 0, 10);
        assertEquals(2, result.size());
        assertAll(
                () -> assertEquals("EMP009", result.get(0).getBadgeId()),
                () -> assertEquals("EMP010", result.get(1).getBadgeId())
        );
    }

    @Test
    void testUpdateEmployee_ValidInput_Success() {
        Employee existing = new Employee(7L, "EMP011", "Charlie", "WORKER", "Warehouse", "A", LocalDate.now(), "ACTIVE");
        EmployeeDTO dto = new EmployeeDTO("EMP011", "Charles", "SUPERVISOR", "Warehouse", "A", LocalDate.now(), "ACTIVE");
        when(employeeRepository.findById(7L)).thenReturn(Optional.of(existing));
        existing.setName("Charles");
        existing.setRole("SUPERVISOR");
        when(employeeRepository.save(existing)).thenReturn(existing);
        Employee result = employeeService.update(7L, dto);
        assertEquals("Charles", result.getName());
        assertEquals("SUPERVISOR", result.getRole());
    }

    @Test
    void testUpdateEmployee_NotFound_ThrowsException() {
        EmployeeDTO dto = new EmployeeDTO("EMP012", "Dana", "WORKER", "Warehouse", "A", LocalDate.now(), "ACTIVE");
        when(employeeRepository.findById(100L)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> employeeService.update(100L, dto));
    }

    @Test
    void testUpdateEmployee_DuplicateBadgeId_ThrowsException() {
        Employee existing = new Employee(8L, "EMP013", "Eve", "WORKER", "Warehouse", "A", LocalDate.now(), "ACTIVE");
        EmployeeDTO dto = new EmployeeDTO("EMP001", "Eve", "WORKER", "Warehouse", "A", LocalDate.now(), "ACTIVE");
        when(employeeRepository.findById(8L)).thenReturn(Optional.of(existing));
        when(employeeRepository.findByBadgeId("EMP001")).thenReturn(Optional.of(new Employee()));
        assertThrows(DataIntegrityViolationException.class, () -> employeeService.update(8L, dto));
    }
}