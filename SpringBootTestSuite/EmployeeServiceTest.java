package com.example.ems.employee;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDate;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
public class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeService employeeService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetAllEmployeesReturnsList() {
        Employee employee = new Employee("John", "Doe", "john.doe@example.com", "Developer", LocalDate.of(2020, 1, 1));
        employee.setId(1L);
        List<Employee> employees = Arrays.asList(employee);
        Mockito.when(employeeRepository.findAll()).thenReturn(employees);

        List<EmployeeDto> result = employeeService.getAllEmployees();
        assertEquals(1, result.size());
        assertEquals("John", result.get(0).getFirstName());
    }

    @Test
    public void testGetAllEmployeesReturnsEmptyList() {
        Mockito.when(employeeRepository.findAll()).thenReturn(Collections.emptyList());
        List<EmployeeDto> result = employeeService.getAllEmployees();
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetEmployeeByIdReturnsEmployeeDto() {
        Employee employee = new Employee("John", "Doe", "john.doe@example.com", "Developer", LocalDate.of(2020, 1, 1));
        employee.setId(1L);
        Mockito.when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        EmployeeDto result = employeeService.getEmployeeById(1L);
        assertEquals("John", result.getFirstName());
        assertEquals(1L, result.getId());
    }

    @Test
    public void testGetEmployeeByIdThrowsNotFound() {
        Mockito.when(employeeRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(EmployeeNotFoundException.class, () -> employeeService.getEmployeeById(99L));
    }

    @Test
    public void testGetEmployeeByIdWithNullId() {
        Mockito.when(employeeRepository.findById(null)).thenReturn(Optional.empty());
        assertThrows(EmployeeNotFoundException.class, () -> employeeService.getEmployeeById(null));
    }

    @Test
    public void testCreateEmployeeSuccess() {
        EmployeeDto dto = new EmployeeDto(null, "Jane", "Smith", "jane.smith@example.com", "Manager", LocalDate.of(2021, 5, 15));
        Mockito.when(employeeRepository.existsByEmail("jane.smith@example.com")).thenReturn(false);
        Employee saved = new Employee("Jane", "Smith", "jane.smith@example.com", "Manager", LocalDate.of(2021, 5, 15));
        saved.setId(2L);
        Mockito.when(employeeRepository.save(any(Employee.class))).thenReturn(saved);

        EmployeeDto result = employeeService.createEmployee(dto);
        assertEquals("Jane", result.getFirstName());
        assertEquals(2L, result.getId());
    }

    @Test
    public void testCreateEmployeeThrowsDuplicate() {
        EmployeeDto dto = new EmployeeDto(null, "Jane", "Smith", "jane.smith@example.com", "Manager", LocalDate.of(2021, 5, 15));
        Mockito.when(employeeRepository.existsByEmail("jane.smith@example.com")).thenReturn(true);
        assertThrows(DuplicateEmployeeException.class, () -> employeeService.createEmployee(dto));
    }

    @Test
    public void testCreateEmployeeWithNullDto() {
        assertThrows(NullPointerException.class, () -> employeeService.createEmployee(null));
    }

    @Test
    public void testCreateEmployeeWithEmptyEmail() {
        EmployeeDto dto = new EmployeeDto(null, "Jane", "Smith", "", "Manager", LocalDate.of(2021, 5, 15));
        Mockito.when(employeeRepository.existsByEmail("")).thenReturn(false);
        Employee saved = new Employee("Jane", "Smith", "", "Manager", LocalDate.of(2021, 5, 15));
        saved.setId(2L);
        Mockito.when(employeeRepository.save(any(Employee.class))).thenReturn(saved);

        EmployeeDto result = employeeService.createEmployee(dto);
        assertEquals("", result.getEmail());
    }

    @Test
    public void testUpdateEmployeeSuccess() {
        Employee existing = new Employee("John", "Doe", "john.doe@example.com", "Developer", LocalDate.of(2020, 1, 1));
        existing.setId(1L);
        EmployeeDto dto = new EmployeeDto(1L, "Johnny", "Doe", "john.doe@example.com", "Lead", LocalDate.of(2020, 1, 1));
        Mockito.when(employeeRepository.findById(1L)).thenReturn(Optional.of(existing));
        Mockito.when(employeeRepository.save(any(Employee.class))).thenReturn(existing);

        EmployeeDto result = employeeService.updateEmployee(1L, dto);
        assertEquals("Johnny", result.getFirstName());
        assertEquals("Lead", result.getRole());
    }

    @Test
    public void testUpdateEmployeeThrowsNotFound() {
        EmployeeDto dto = new EmployeeDto(99L, "Ghost", "User", "ghost@example.com", "None", LocalDate.of(2021, 1, 1));
        Mockito.when(employeeRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(EmployeeNotFoundException.class, () -> employeeService.updateEmployee(99L, dto));
    }

    @Test
    public void testUpdateEmployeeWithNullDto() {
        assertThrows(NullPointerException.class, () -> employeeService.updateEmployee(1L, null));
    }

    @Test
    public void testDeleteEmployeeSuccess() {
        Mockito.when(employeeRepository.existsById(1L)).thenReturn(true);
        Mockito.doNothing().when(employeeRepository).deleteById(1L);
        assertDoesNotThrow(() -> employeeService.deleteEmployee(1L));
        Mockito.verify(employeeRepository).deleteById(1L);
    }

    @Test
    public void testDeleteEmployeeThrowsNotFound() {
        Mockito.when(employeeRepository.existsById(99L)).thenReturn(false);
        assertThrows(EmployeeNotFoundException.class, () -> employeeService.deleteEmployee(99L));
    }

    @Test
    public void testDeleteEmployeeWithNullId() {
        Mockito.when(employeeRepository.existsById(null)).thenReturn(false);
        assertThrows(EmployeeNotFoundException.class, () -> employeeService.deleteEmployee(null));
    }
}