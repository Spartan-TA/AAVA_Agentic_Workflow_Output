package com.warehouse.employee.management.service;

import com.warehouse.employee.management.dto.*;
import com.warehouse.employee.management.entity.Employee;
import com.warehouse.employee.management.repository.EmployeeRepository;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.data.domain.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmployeeServiceTest {
    @Mock
    EmployeeRepository employeeRepository;

    @InjectMocks
    EmployeeService employeeService;

    Employee employee;
    EmployeeCreateRequest createRequest;
    EmployeeUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        employee = Employee.builder()
                .id(1L)
                .badgeId("BID1")
                .firstName("John")
                .lastName("Doe")
                .email("john@ex.com")
                .role("WORKER")
                .department("Logistics")
                .shiftGroup("A")
                .hireDate(LocalDate.of(2020, 1, 1))
                .status("ACTIVE")
                .deleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        createRequest = EmployeeCreateRequest.builder()
                .badgeId("BID1")
                .firstName("John")
                .lastName("Doe")
                .email("john@ex.com")
                .role("WORKER")
                .department("Logistics")
                .shiftGroup("A")
                .hireDate(LocalDate.of(2020, 1, 1))
                .status("ACTIVE")
                .build();
        updateRequest = EmployeeUpdateRequest.builder()
                .firstName("Johnny")
                .lastName("Doey")
                .email("johnny@ex.com")
                .role("SUPERVISOR")
                .department("Warehouse")
                .shiftGroup("B")
                .status("INACTIVE")
                .build();
    }

    @AfterEach
    void tearDown() {
        Mockito.framework().clearInlineMocks();
    }

    @Test
    void testCreateEmployee_WithValidData_ReturnsEmployee() {
        when(employeeRepository.findByBadgeId("BID1")).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenAnswer(inv -> {
            Employee e = inv.getArgument(0);
            e.setId(1L);
            return e;
        });
        EmployeeResponse response = employeeService.createEmployee(createRequest);
        assertEquals("BID1", response.getBadgeId());
        assertEquals("John", response.getFirstName());
        assertNotNull(response.getId());
    }

    @Test
    void testCreateEmployee_DuplicateBadgeId_ThrowsException() {
        when(employeeRepository.findByBadgeId("BID1")).thenReturn(Optional.of(employee));
        assertThrows(IllegalArgumentException.class, () -> employeeService.createEmployee(createRequest));
    }

    @Test
    void testGetEmployeeById_Found() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        Optional<EmployeeResponse> resp = employeeService.getEmployeeById(1L);
        assertTrue(resp.isPresent());
        assertEquals("John", resp.get().getFirstName());
    }

    @Test
    void testGetEmployeeById_Deleted() {
        Employee deletedEmp = Employee.builder().id(2L).deleted(true).build();
        when(employeeRepository.findById(2L)).thenReturn(Optional.of(deletedEmp));
        Optional<EmployeeResponse> resp = employeeService.getEmployeeById(2L);
        assertFalse(resp.isPresent());
    }

    @Test
    void testGetEmployeeById_NotFound() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());
        Optional<EmployeeResponse> resp = employeeService.getEmployeeById(99L);
        assertFalse(resp.isPresent());
    }

    @Test
    void testGetAllEmployees_ReturnsPage() {
        Page<Employee> page = new PageImpl<>(List.of(employee));
        when(employeeRepository.findAllActive(any(Pageable.class))).thenReturn(page);
        Page<EmployeeResponse> result = employeeService.getAllEmployees(PageRequest.of(0, 10));
        assertEquals(1, result.getTotalElements());
        assertEquals("John", result.getContent().get(0).getFirstName());
    }

    @Test
    void testSearchEmployees_ReturnsPage() {
        Page<Employee> page = new PageImpl<>(List.of(employee));
        when(employeeRepository.searchEmployees(eq("john"), any(Pageable.class))).thenReturn(page);
        Page<EmployeeResponse> result = employeeService.searchEmployees("john", PageRequest.of(0, 10));
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void testUpdateEmployee_FoundAndUpdated() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(inv -> inv.getArgument(0));
        Optional<EmployeeResponse> resp = employeeService.updateEmployee(1L, updateRequest);
        assertTrue(resp.isPresent());
        assertEquals("Johnny", resp.get().getFirstName());
        assertEquals("INACTIVE", resp.get().getStatus());
    }

    @Test
    void testUpdateEmployee_NotFound() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());
        Optional<EmployeeResponse> resp = employeeService.updateEmployee(99L, updateRequest);
        assertFalse(resp.isPresent());
    }

    @Test
    void testUpdateEmployee_Deleted() {
        Employee deletedEmp = Employee.builder().id(2L).deleted(true).build();
        when(employeeRepository.findById(2L)).thenReturn(Optional.of(deletedEmp));
        Optional<EmployeeResponse> resp = employeeService.updateEmployee(2L, updateRequest);
        assertFalse(resp.isPresent());
    }

    @Test
    void testDeleteEmployee_FoundAndDeleted() {
        Employee emp = Employee.builder().id(1L).deleted(false).build();
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(emp));
        when(employeeRepository.save(any(Employee.class))).thenReturn(emp);
        boolean result = employeeService.deleteEmployee(1L);
        assertTrue(result);
        assertTrue(emp.getDeleted());
    }

    @Test
    void testDeleteEmployee_NotFound() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());
        boolean result = employeeService.deleteEmployee(99L);
        assertFalse(result);
    }

    @Test
    void testDeleteEmployee_AlreadyDeleted() {
        Employee emp = Employee.builder().id(2L).deleted(true).build();
        when(employeeRepository.findById(2L)).thenReturn(Optional.of(emp));
        boolean result = employeeService.deleteEmployee(2L);
        assertFalse(result);
    }

    @Test
    void testMapToResponse_AllFields() {
        Employee emp = Employee.builder()
                .id(10L)
                .badgeId("BID10")
                .firstName("A")
                .lastName("B")
                .email("a@b.com")
                .role("ADMIN")
                .department("Dep")
                .shiftGroup("SG")
                .hireDate(LocalDate.of(2022, 1, 1))
                .status("ACTIVE")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        // Use reflection to call private method
        try {
            var method = EmployeeService.class.getDeclaredMethod("mapToResponse", Employee.class);
            method.setAccessible(true);
            EmployeeResponse resp = (EmployeeResponse) method.invoke(employeeService, emp);
            assertEquals("BID10", resp.getBadgeId());
            assertEquals("A", resp.getFirstName());
        } catch (Exception e) {
            fail(e);
        }
    }
}
