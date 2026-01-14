package com.warehouse.api.service;

import com.warehouse.domain.Employee;
import com.warehouse.repository.EmployeeRepository;
import org.junit.jupiter.api.*;
import org.mockito.*;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmployeeServiceTest_Part2 {
    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeService employeeService;

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    void testUpdateEmployee() {
        Employee updated = new Employee();
        updated.setBadgeId("B2");
        updated.setName("Jane");
        updated.setRole("Supervisor");
        updated.setDepartment("Shipping");
        updated.setHireDate(LocalDate.of(2021, 2, 2));
        updated.setStatus("Inactive");
        updated.setDeleted(true);
        when(employeeRepository.save(any(Employee.class))).thenReturn(updated);
        Employee result = employeeService.updateEmployee(1L, updated);
        assertEquals(1L, result.getId());
        assertEquals("Jane", result.getName());
        verify(employeeRepository).save(updated);
    }

    @Test
    void testUpdateEmployeeNull() {
        when(employeeRepository.save(null)).thenReturn(null);
        Employee result = employeeService.updateEmployee(1L, null);
        assertNull(result);
    }

    @Test
    void testDeleteEmployeeSuccess() {
        Employee toDelete = new Employee();
        toDelete.setId(1L);
        toDelete.setDeleted(false);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(toDelete));
        when(employeeRepository.save(any(Employee.class))).thenReturn(toDelete);
        employeeService.deleteEmployee(1L);
        assertTrue(toDelete.isDeleted());
        verify(employeeRepository).findById(1L);
        verify(employeeRepository).save(toDelete);
    }

    @Test
    void testDeleteEmployeeNotFoundThrowsException() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());
        RuntimeException ex = assertThrows(RuntimeException.class, () -> employeeService.deleteEmployee(99L));
        assertEquals("Employee not found", ex.getMessage());
        verify(employeeRepository).findById(99L);
    }
}
