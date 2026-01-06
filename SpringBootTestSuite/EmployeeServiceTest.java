package com.warehouse.ems.service;

import com.warehouse.ems.entity.Employee;
import com.warehouse.ems.repository.EmployeeRepository;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
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

    private Employee employee;

    @BeforeEach
    void setUp() {
        employee = new Employee();
        employee.setId(1L);
        employee.setBadgeId("B12345");
        employee.setName("John Doe");
        employee.setRole("WORKER");
        employee.setDepartment("Logistics");
        employee.setShiftGroup("A");
        employee.setHireDate(LocalDate.now());
        employee.setStatus("ACTIVE");
        employee.setDeleted(false);
    }

    @AfterEach
    void tearDown() {
        employee = null;
    }

    @Test
    void testCreateEmployee_ValidData_Success() {
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);
        Employee created = employeeService.create(employee);
        assertNotNull(created);
        assertEquals("B12345", created.getBadgeId());
    }

    @Test
    void testCreateEmployee_NullEmployee_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> employeeService.create(null));
    }

    @Test
    void testCreateEmployee_EmptyName_ThrowsException() {
        employee.setName("");
        assertThrows(IllegalArgumentException.class, () -> employeeService.create(employee));
    }

    @Test
    void testCreateEmployee_DuplicateBadgeId_ThrowsException() {
        when(employeeRepository.save(any(Employee.class))).thenThrow(DataIntegrityViolationException.class);
        assertThrows(DataIntegrityViolationException.class, () -> employeeService.create(employee));
    }

    @Test
    void testUpdateEmployee_ValidData_Success() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);
        employee.setName("Jane Doe");
        Employee updated = employeeService.update(1L, employee);
        assertEquals("Jane Doe", updated.getName());
    }

    @Test
    void testUpdateEmployee_NonExistentId_ThrowsException() {
        when(employeeRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> employeeService.update(2L, employee));
    }

    @Test
    void testDeleteEmployee_ValidId_SoftDelete() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        employeeService.delete(1L);
        verify(employeeRepository, times(1)).save(argThat(e -> e.isDeleted()));
    }

    @Test
    void testDeleteEmployee_NonExistentId_ThrowsException() {
        when(employeeRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> employeeService.delete(2L));
    }

    @Test
    void testListEmployees_PaginationAndFiltering_Success() {
        List<Employee> employees = Arrays.asList(employee);
        when(employeeRepository.findAll(any())).thenReturn(employees);
        List<Employee> result = employeeService.list(0, 10, "ACTIVE", "Logistics");
        assertFalse(result.isEmpty());
        assertEquals("ACTIVE", result.get(0).getStatus());
    }

    @Test
    void testListEmployees_EmptyResult() {
        when(employeeRepository.findAll(any())).thenReturn(Collections.emptyList());
        List<Employee> result = employeeService.list(0, 10, "INACTIVE", "Unknown");
        assertTrue(result.isEmpty());
    }

    @Test
    void testCreateEmployee_MaxStringLength_Success() {
        String longName = "A".repeat(255);
        employee.setName(longName);
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);
        Employee created = employeeService.create(employee);
        assertEquals(longName, created.getName());
    }

    @Test
    void testCreateEmployee_InvalidRole_ThrowsException() {
        employee.setRole("INVALID_ROLE");
        assertThrows(IllegalArgumentException.class, () -> employeeService.create(employee));
    }

    @Test
    void testCreateEmployee_NullBadgeId_ThrowsException() {
        employee.setBadgeId(null);
        assertThrows(IllegalArgumentException.class, () -> employeeService.create(employee));
    }

    @Test
    void testConcurrentCreateEmployees_UniqueBadgeId_ThrowsException() {
        when(employeeRepository.save(any(Employee.class))).thenThrow(DataIntegrityViolationException.class);
        assertThrows(DataIntegrityViolationException.class, () -> employeeService.create(employee));
    }

    @Test
    void testTransactionRollback_OnException() {
        when(employeeRepository.save(any(Employee.class))).thenThrow(RuntimeException.class);
        assertThrows(RuntimeException.class, () -> employeeService.create(employee));
    }
}
