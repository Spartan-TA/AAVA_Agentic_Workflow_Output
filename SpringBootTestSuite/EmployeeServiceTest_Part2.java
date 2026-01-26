package com.example.warehouse.employee;

import com.example.warehouse.employee.dto.EmployeeDTO;
import com.example.warehouse.employee.entity.Employee;
import com.example.warehouse.employee.repository.EmployeeRepository;
import com.example.warehouse.employee.service.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@SpringBootTest
class EmployeeServiceTest_Part2 {
    @Autowired
    private EmployeeService employeeService;

    @MockBean
    private EmployeeRepository employeeRepository;

    private Employee employee1;
    private Employee employee2;

    @BeforeEach
    void setUp() {
        employee1 = new Employee(1L, "Alice Smith", "BADGE001", "WORKER", "Receiving", "A", LocalDate.now().minusYears(2), "ACTIVE", false);
        employee2 = new Employee(2L, "Bob Jones", "BADGE002", "SUPERVISOR", "Shipping", "B", LocalDate.now().minusYears(1), "ACTIVE", false);
    }

    @Test
    @DisplayName("Test soft delete employee")
    void testSoftDeleteEmployee() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee1));
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee1);
        employeeService.softDeleteEmployee(1L);
        assertTrue(employee1.isDeleted());
    }

    @Test
    @DisplayName("Test get employee by id")
    void testGetEmployeeById() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee1));
        EmployeeDTO dto = employeeService.getEmployee(1L);
        assertNotNull(dto);
        assertEquals("Alice Smith", dto.getName());
    }

    @Test
    @DisplayName("Test get employee by invalid id throws exception")
    void testGetEmployeeByInvalidId() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> employeeService.getEmployee(99L));
    }

    @Test
    @DisplayName("Test get all employees")
    void testGetAllEmployees() {
        when(employeeRepository.findByDeletedFalse()).thenReturn(Arrays.asList(employee1, employee2));
        List<EmployeeDTO> dtos = employeeService.getAllEmployees();
        assertEquals(2, dtos.size());
    }

    @Test
    @DisplayName("Test get employees by filters")
    void testGetEmployeesByFilters() {
        when(employeeRepository.findByDepartment(eq("Receiving"))).thenReturn(Arrays.asList(employee1));
        List<EmployeeDTO> dtos = employeeService.getEmployeesByFilters("Receiving", null, null);
        assertEquals(1, dtos.size());
        assertEquals("Alice Smith", dtos.get(0).getName());
    }

    @Test
    @DisplayName("Test DTO mapping")
    void testDtoMapping() {
        EmployeeDTO dto = employeeService.toDto(employee1);
        assertEquals(employee1.getName(), dto.getName());
        assertEquals(employee1.getBadgeId(), dto.getBadgeId());
    }
}