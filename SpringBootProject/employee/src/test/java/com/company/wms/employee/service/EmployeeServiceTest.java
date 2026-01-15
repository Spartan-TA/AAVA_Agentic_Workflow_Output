package com.company.wms.employee.service;

import com.company.wms.employee.dto.CreateEmployeeRequest;
import com.company.wms.employee.dto.EmployeeDTO;
import com.company.wms.employee.dto.UpdateEmployeeRequest;
import com.company.wms.employee.entity.Employee;
import com.company.wms.employee.mapper.EmployeeMapper;
import com.company.wms.employee.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for EmployeeService.
 */
class EmployeeServiceTest {
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private EmployeeMapper employeeMapper;
    @InjectMocks
    private EmployeeService employeeService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateEmployee() {
        CreateEmployeeRequest request = new CreateEmployeeRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("john.doe@example.com");
        request.setDepartment("HR");
        request.setPosition("Manager");
        request.setHireDate(LocalDate.now());

        Employee employee = new Employee();
        employee.setId(1L);
        employee.setFirstName("John");
        employee.setLastName("Doe");
        employee.setEmail("john.doe@example.com");
        employee.setDepartment("HR");
        employee.setPosition("Manager");
        employee.setHireDate(LocalDate.now());
        employee.setActive(true);

        EmployeeDTO dto = new EmployeeDTO(1L, "John", "Doe", "john.doe@example.com", "HR", "Manager", LocalDate.now(), true);

        when(employeeMapper.toEntity(request)).thenReturn(employee);
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);
        when(employeeMapper.toDTO(employee)).thenReturn(dto);

        EmployeeDTO result = employeeService.createEmployee(request);
        assertNotNull(result);
        assertEquals("John", result.getFirstName());
    }

    @Test
    void testUpdateEmployee() {
        UpdateEmployeeRequest request = new UpdateEmployeeRequest();
        request.setFirstName("Jane");
        request.setLastName("Smith");
        request.setEmail("jane.smith@example.com");
        request.setDepartment("Finance");
        request.setPosition("Analyst");
        request.setHireDate(LocalDate.now());

        Employee employee = new Employee();
        employee.setId(2L);
        employee.setActive(true);

        EmployeeDTO dto = new EmployeeDTO(2L, "Jane", "Smith", "jane.smith@example.com", "Finance", "Analyst", LocalDate.now(), true);

        when(employeeRepository.findByIdAndActiveTrue(2L)).thenReturn(Optional.of(employee));
        doNothing().when(employeeMapper).updateEntity(employee, request);
        when(employeeRepository.save(employee)).thenReturn(employee);
        when(employeeMapper.toDTO(employee)).thenReturn(dto);

        EmployeeDTO result = employeeService.updateEmployee(2L, request);
        assertNotNull(result);
        assertEquals("Jane", result.getFirstName());
    }

    @Test
    void testDeleteEmployee() {
        Employee employee = new Employee();
        employee.setId(3L);
        employee.setActive(true);
        when(employeeRepository.findByIdAndActiveTrue(3L)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(employee)).thenReturn(employee);
        employeeService.deleteEmployee(3L);
        assertFalse(employee.isActive());
    }

    @Test
    void testGetEmployee() {
        Employee employee = new Employee();
        employee.setId(4L);
        employee.setActive(true);
        EmployeeDTO dto = new EmployeeDTO(4L, "Alice", "Brown", "alice.brown@example.com", "IT", "Developer", LocalDate.now(), true);
        when(employeeRepository.findByIdAndActiveTrue(4L)).thenReturn(Optional.of(employee));
        when(employeeMapper.toDTO(employee)).thenReturn(dto);
        EmployeeDTO result = employeeService.getEmployee(4L);
        assertNotNull(result);
        assertEquals("Alice", result.getFirstName());
    }

    @Test
    void testListEmployees() {
        Employee employee = new Employee();
        employee.setId(5L);
        employee.setActive(true);
        EmployeeDTO dto = new EmployeeDTO(5L, "Bob", "White", "bob.white@example.com", "Ops", "Worker", LocalDate.now(), true);
        Page<Employee> page = new PageImpl<>(Arrays.asList(employee));
        when(employeeRepository.findByActiveTrue(any(PageRequest.class))).thenReturn(page);
        when(employeeMapper.toDTO(employee)).thenReturn(dto);
        Page<EmployeeDTO> result = employeeService.listEmployees(null, null, 0, 10, "id");
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }
}