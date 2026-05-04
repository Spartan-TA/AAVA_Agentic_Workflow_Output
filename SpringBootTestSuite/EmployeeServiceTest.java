package com.warehouse.management.employee;

import com.warehouse.management.employee.EmployeeService;
import com.warehouse.management.employee.EmployeeRepository;
import com.warehouse.management.employee.Employee;
import com.warehouse.management.employee.EmployeeDTO;
import org.junit.jupiter.api.*;
import org.mockito.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeService employeeService;

    private Employee employee;
    private EmployeeDTO employeeDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        employee = new Employee(1L, "John Doe", "BADGE123", "WORKER", "Logistics", "A", new Date(), "ACTIVE");
        employeeDTO = new EmployeeDTO("John Doe", "BADGE123", "WORKER", "Logistics", "A", new Date(), "ACTIVE");
    }

    @AfterEach
    void tearDown() {
        employee = null;
        employeeDTO = null;
    }

    @Test
    void testCreateEmployee_ValidInput() {
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);
        Employee result = employeeService.create(employeeDTO);
        assertNotNull(result);
        assertEquals("BADGE123", result.getBadgeId());
    }

    @Test
    void testCreateEmployee_NullInput() {
        assertThrows(IllegalArgumentException.class, () -> employeeService.create(null));
    }

    @Test
    void testCreateEmployee_EmptyName() {
        EmployeeDTO emptyNameDTO = new EmployeeDTO("", "BADGE123", "WORKER", "Logistics", "A", new Date(), "ACTIVE");
        assertThrows(IllegalArgumentException.class, () -> employeeService.create(emptyNameDTO));
    }

    @Test
    void testListEmployees_Pagination() {
        List<Employee> employees = Arrays.asList(employee);
        when(employeeRepository.findAll(any())).thenReturn(employees);
        List<Employee> result = employeeService.list(0, 10, "ACTIVE", null);
        assertEquals(1, result.size());
    }

    @Test
    void testUpdateEmployee_ValidInput() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);
        EmployeeDTO updateDTO = new EmployeeDTO("Jane Doe", "BADGE123", "HR", "HR", "B", new Date(), "ACTIVE");
        Employee result = employeeService.update(1L, updateDTO);
        assertEquals("Jane Doe", result.getName());
    }

    @Test
    void testUpdateEmployee_NonExistent() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());
        EmployeeDTO updateDTO = new EmployeeDTO("Jane Doe", "BADGE123", "HR", "HR", "B", new Date(), "ACTIVE");
        assertThrows(NoSuchElementException.class, () -> employeeService.update(99L, updateDTO));
    }

    @Test
    void testSoftDeleteEmployee() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);
        Employee result = employeeService.softDelete(1L);
        assertEquals("DELETED", result.getStatus());
    }

    @Test
    void testFindByBadgeId_Valid() {
        when(employeeRepository.findByBadgeId("BADGE123")).thenReturn(Optional.of(employee));
        Employee result = employeeService.findByBadgeId("BADGE123");
        assertNotNull(result);
        assertEquals("BADGE123", result.getBadgeId());
    }

    @Test
    void testFindByBadgeId_Invalid() {
        when(employeeRepository.findByBadgeId("INVALID")).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> employeeService.findByBadgeId("INVALID"));
    }

    @Test
    void testCreateEmployee_DuplicateBadgeId() {
        when(employeeRepository.findByBadgeId("BADGE123")).thenReturn(Optional.of(employee));
        EmployeeDTO duplicateDTO = new EmployeeDTO("Jane Doe", "BADGE123", "HR", "HR", "B", new Date(), "ACTIVE");
        assertThrows(IllegalArgumentException.class, () -> employeeService.create(duplicateDTO));
    }
}