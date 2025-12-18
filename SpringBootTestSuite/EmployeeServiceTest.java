package com.example.warehouseems;

import com.example.warehouseems.employee.EmployeeService;
import com.example.warehouseems.employee.Employee;
import com.example.warehouseems.employee.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmployeeServiceTest {
    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeService employeeService;

    private Employee validEmployee;

    @BeforeEach
    void setUp() {
        validEmployee = new Employee();
        validEmployee.setId(1L);
        validEmployee.setName("John Doe");
        validEmployee.setBadgeId("BADGE123");
        validEmployee.setDepartment("Logistics");
        validEmployee.setRole("Worker");
        validEmployee.setStatus("ACTIVE");
    }

    @Test
    void createEmployee_validInput_employeeCreated() {
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);
        Employee created = employeeService.createEmployee(validEmployee);
        assertNotNull(created);
        assertEquals("John Doe", created.getName());
        verify(employeeRepository).save(validEmployee);
    }

    @Test
    void createEmployee_nullFields_throwsException() {
        Employee emp = new Employee();
        emp.setName(null);
        emp.setBadgeId(null);
        assertThrows(IllegalArgumentException.class, () -> employeeService.createEmployee(emp));
    }

    @Test
    void createEmployee_emptyFields_throwsException() {
        Employee emp = new Employee();
        emp.setName("");
        emp.setBadgeId("");
        assertThrows(IllegalArgumentException.class, () -> employeeService.createEmployee(emp));
    }

    @Test
    void createEmployee_duplicateBadgeId_throwsException() {
        when(employeeRepository.existsByBadgeId("BADGE123")).thenReturn(true);
        assertThrows(IllegalStateException.class, () -> employeeService.createEmployee(validEmployee));
    }

    @Test
    void findById_existingId_returnsEmployee() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        Employee found = employeeService.findById(1L);
        assertNotNull(found);
        assertEquals(1L, found.getId());
    }

    @Test
    void findById_nonExistingId_throwsException() {
        when(employeeRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> employeeService.findById(2L));
    }

    @Test
    void findAll_withPagination_returnsPage() {
        List<Employee> employees = Arrays.asList(validEmployee);
        Page<Employee> page = new PageImpl<>(employees);
        when(employeeRepository.findAll(any(Pageable.class))).thenReturn(page);
        Page<Employee> result = employeeService.findAll(PageRequest.of(0, 10));
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void updateEmployee_validUpdate_employeeUpdated() {
        Employee updated = new Employee();
        updated.setId(1L);
        updated.setName("Jane Doe");
        updated.setBadgeId("BADGE123");
        updated.setDepartment("Logistics");
        updated.setRole("Supervisor");
        updated.setStatus("ACTIVE");
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(updated);
        Employee result = employeeService.updateEmployee(1L, updated);
        assertEquals("Jane Doe", result.getName());
        verify(employeeRepository).save(updated);
    }

    @Test
    void updateEmployee_nonExistingId_throwsException() {
        Employee updated = new Employee();
        updated.setId(2L);
        when(employeeRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> employeeService.updateEmployee(2L, updated));
    }

    @Test
    void deleteEmployee_softDelete_employeeStatusSetToInactive() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);
        employeeService.deleteEmployee(1L);
        assertEquals("INACTIVE", validEmployee.getStatus());
        verify(employeeRepository).save(validEmployee);
    }

    @Test
    void deleteEmployee_nonExistingId_throwsException() {
        when(employeeRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> employeeService.deleteEmployee(2L));
    }

    @Test
    void searchEmployees_byName_returnsResults() {
        List<Employee> employees = Arrays.asList(validEmployee);
        when(employeeRepository.findByNameContainingIgnoreCase(eq("John"))).thenReturn(employees);
        List<Employee> result = employeeService.searchByName("John");
        assertEquals(1, result.size());
    }

    @Test
    void searchEmployees_byDepartment_returnsResults() {
        List<Employee> employees = Arrays.asList(validEmployee);
        when(employeeRepository.findByDepartment(eq("Logistics"))).thenReturn(employees);
        List<Employee> result = employeeService.searchByDepartment("Logistics");
        assertEquals(1, result.size());
    }

    @Test
    void searchEmployees_byRole_returnsResults() {
        List<Employee> employees = Arrays.asList(validEmployee);
        when(employeeRepository.findByRole(eq("Worker"))).thenReturn(employees);
        List<Employee> result = employeeService.searchByRole("Worker");
        assertEquals(1, result.size());
    }

    @Test
    void searchEmployees_byStatus_returnsResults() {
        List<Employee> employees = Arrays.asList(validEmployee);
        when(employeeRepository.findByStatus(eq("ACTIVE"))).thenReturn(employees);
        List<Employee> result = employeeService.searchByStatus("ACTIVE");
        assertEquals(1, result.size());
    }

    @Test
    void searchEmployees_emptyQuery_returnsEmptyList() {
        when(employeeRepository.findByNameContainingIgnoreCase("")).thenReturn(Collections.emptyList());
        List<Employee> result = employeeService.searchByName("");
        assertTrue(result.isEmpty());
    }
}
