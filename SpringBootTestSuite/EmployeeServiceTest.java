package com.example.warehouse.test;

import com.example.warehouse.employee.Employee;
import com.example.warehouse.employee.EmployeeRepository;
import com.example.warehouse.employee.EmployeeService;
import com.example.warehouse.employee.EmployeeController;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmployeeServiceTest {
    @Mock
    private EmployeeRepository employeeRepository;
    @InjectMocks
    private EmployeeService employeeService;
    private EmployeeController employeeController;
    private Employee testEmployee;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        employeeController = new EmployeeController(employeeService);
        testEmployee = new Employee(1L, "John Doe", "B123", "WORKER", "Shipping", "A", LocalDate.now(), "ACTIVE");
    }

    @AfterEach
    void tearDown() {
        // Cleanup if needed
    }

    @Test
    void testCreateEmployee_ValidInput_Success() {
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);
        Employee created = employeeService.createEmployee(testEmployee);
        assertNotNull(created);
        assertEquals("John Doe", created.getName());
    }

    @Test
    void testCreateEmployee_DuplicateBadgeId_ThrowsException() {
        when(employeeRepository.findByBadgeId("B123")).thenReturn(Optional.of(testEmployee));
        assertThrows(IllegalArgumentException.class, () -> employeeService.createEmployee(testEmployee));
    }

    @Test
    void testGetEmployeeById_ValidId_ReturnsEmployee() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        Employee found = employeeService.getEmployeeById(1L);
        assertNotNull(found);
        assertEquals(1L, found.getId());
    }

    @Test
    void testGetEmployeeById_InvalidId_ThrowsException() {
        when(employeeRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> employeeService.getEmployeeById(2L));
    }

    @Test
    void testUpdateEmployee_ValidInput_Success() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);
        Employee updated = employeeService.updateEmployee(1L, testEmployee);
        assertEquals("John Doe", updated.getName());
    }

    @Test
    void testUpdateEmployee_NullInput_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> employeeService.updateEmployee(1L, null));
    }

    @Test
    void testDeleteEmployee_SoftDelete_Success() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        doNothing().when(employeeRepository).softDeleteById(1L);
        assertDoesNotThrow(() -> employeeService.softDeleteEmployee(1L));
    }

    @Test
    void testListEmployees_Pagination_Success() {
        Page<Employee> page = new PageImpl<>(Collections.singletonList(testEmployee));
        when(employeeRepository.findAll(any(Pageable.class))).thenReturn(page);
        Page<Employee> result = employeeService.listEmployees(PageRequest.of(0, 10));
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void testListEmployees_EmptyList() {
        Page<Employee> page = new PageImpl<>(Collections.emptyList());
        when(employeeRepository.findAll(any(Pageable.class))).thenReturn(page);
        Page<Employee> result = employeeService.listEmployees(PageRequest.of(0, 10));
        assertEquals(0, result.getTotalElements());
    }

    @Test
    void testController_CreateEmployee_Success() {
        when(employeeService.createEmployee(any(Employee.class))).thenReturn(testEmployee);
        ResponseEntity<Employee> response = employeeController.createEmployee(testEmployee);
        assertEquals(201, response.getStatusCodeValue());
        assertEquals("John Doe", response.getBody().getName());
    }

    @Test
    void testController_DeleteEmployee_NotFound() {
        doThrow(new NoSuchElementException()).when(employeeService).softDeleteEmployee(2L);
        assertThrows(NoSuchElementException.class, () -> employeeController.deleteEmployee(2L));
    }

    // Security tests would use @WithMockUser in integration tests
}
