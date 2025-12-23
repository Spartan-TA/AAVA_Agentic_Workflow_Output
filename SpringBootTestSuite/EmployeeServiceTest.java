package com.company.wems;

import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.data.domain.*;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class EmployeeServiceTest {
    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeService employeeService;

    private Employee employee;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        employee = Employee.builder()
                .id(1L)
                .name("John Doe")
                .badgeId("BADGE123")
                .role("Worker")
                .department("Logistics")
                .shiftGroup("A")
                .hireDate(LocalDate.of(2020, 1, 1))
                .status("Active")
                .deleted(false)
                .build();
    }

    @AfterEach
    public void tearDown() {
        employee = null;
    }

    @Test
    public void testCreateEmployee_Valid() {
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);
        Employee created = employeeService.createEmployee(employee);
        assertEquals("John Doe", created.getName());
        verify(employeeRepository, times(1)).save(employee);
    }

    @Test
    public void testCreateEmployee_Null() {
        assertThrows(IllegalArgumentException.class, () -> employeeService.createEmployee(null));
    }

    @Test
    public void testGetEmployeeById_Found() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        Optional<Employee> found = employeeService.getEmployeeById(1L);
        assertTrue(found.isPresent());
        assertEquals("John Doe", found.get().getName());
    }

    @Test
    public void testGetEmployeeById_NotFound() {
        when(employeeRepository.findById(2L)).thenReturn(Optional.empty());
        Optional<Employee> found = employeeService.getEmployeeById(2L);
        assertFalse(found.isPresent());
    }

    @Test
    public void testGetAllEmployees_Pagination() {
        Pageable pageable = PageRequest.of(0, 2);
        List<Employee> employees = Arrays.asList(employee);
        Page<Employee> page = new PageImpl<>(employees, pageable, 1);
        when(employeeRepository.findAll(pageable)).thenReturn(page);
        Page<Employee> result = employeeService.getAllEmployees(pageable);
        assertEquals(1, result.getTotalElements());
        verify(employeeRepository, times(1)).findAll(pageable);
    }

    @Test
    public void testUpdateEmployee_Valid() {
        Employee updated = Employee.builder().id(1L).name("Jane Smith").badgeId("BADGE123").role("Manager").department("HR").shiftGroup("B").hireDate(LocalDate.of(2021, 5, 10)).status("Inactive").deleted(false).build();
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(updated);
        Employee result = employeeService.updateEmployee(1L, updated);
        assertEquals("Jane Smith", result.getName());
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    public void testUpdateEmployee_NotFound() {
        when(employeeRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> employeeService.updateEmployee(2L, employee));
    }

    @Test
    public void testUpdateEmployee_NullEmployee() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        assertThrows(IllegalArgumentException.class, () -> employeeService.updateEmployee(1L, null));
    }

    @Test
    public void testSoftDeleteEmployee_Valid() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        employeeService.softDeleteEmployee(1L);
        assertTrue(employee.isDeleted());
        verify(employeeRepository, times(1)).save(employee);
    }

    @Test
    public void testSoftDeleteEmployee_NotFound() {
        when(employeeRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> employeeService.softDeleteEmployee(2L));
    }

    @Test
    public void testFilterEmployees_Normal() {
        Pageable pageable = PageRequest.of(0, 2);
        List<Employee> employees = Arrays.asList(employee);
        Page<Employee> page = new PageImpl<>(employees, pageable, 1);
        when(employeeRepository.findAll(any(), eq(pageable))).thenReturn(page);
        Page<Employee> result = employeeService.filterEmployees("Worker", "Logistics", pageable);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    public void testFindByBadgeId_Found() {
        when(employeeRepository.findByBadgeId("BADGE123")).thenReturn(Optional.of(employee));
        Optional<Employee> found = employeeService.findByBadgeId("BADGE123");
        assertTrue(found.isPresent());
        assertEquals("John Doe", found.get().getName());
    }

    @Test
    public void testFindByBadgeId_NotFound() {
        when(employeeRepository.findByBadgeId("BADGE999")).thenReturn(Optional.empty());
        Optional<Employee> found = employeeService.findByBadgeId("BADGE999");
        assertFalse(found.isPresent());
    }

    @Test
    public void testFindByBadgeId_Null() {
        when(employeeRepository.findByBadgeId(null)).thenReturn(Optional.empty());
        Optional<Employee> found = employeeService.findByBadgeId(null);
        assertFalse(found.isPresent());
    }

    @Test
    public void testCreateEmployee_EmptyFields() {
        Employee emp = Employee.builder().name("").badgeId("").role("").department("").shiftGroup("").hireDate(null).status("").deleted(false).build();
        when(employeeRepository.save(any(Employee.class))).thenReturn(emp);
        Employee created = employeeService.createEmployee(emp);
        assertEquals("", created.getName());
        assertEquals("", created.getBadgeId());
    }

    @Test
    public void testCreateEmployee_BoundaryFields() {
        String longString = "x".repeat(255);
        Employee emp = Employee.builder().name(longString).badgeId(longString).role(longString).department(longString).shiftGroup(longString).hireDate(LocalDate.now()).status(longString).deleted(false).build();
        when(employeeRepository.save(any(Employee.class))).thenReturn(emp);
        Employee created = employeeService.createEmployee(emp);
        assertEquals(longString, created.getName());
        assertEquals(longString, created.getBadgeId());
    }
}
