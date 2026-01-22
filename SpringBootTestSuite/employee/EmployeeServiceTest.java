package com.warehouse.ems.employee;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.*;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.time.LocalDate;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive service layer tests for Employee
 * Tests cover: CRUD operations, business logic, exception handling, pagination
 */
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeService employeeService;

    private Employee employee;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        employee = Employee.builder()
                .id(1L)
                .name("John Doe")
                .badgeId("B12345")
                .role("Operator")
                .department("Logistics")
                .shiftGroup("A")
                .hireDate(LocalDate.of(2020, 1, 15))
                .active(true)
                .deleted(false)
                .build();
    }

    @Test
    @DisplayName("Should create employee successfully")
    void testCreateEmployee() {
        // Arrange
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        // Act
        Employee created = employeeService.create(employee);

        // Assert
        assertThat(created).isNotNull();
        assertThat(created.getName()).isEqualTo("John Doe");
        assertThat(created.getBadgeId()).isEqualTo("B12345");
        verify(employeeRepository, times(1)).save(employee);
    }

    @Test
    @DisplayName("Should throw exception when creating employee with null data")
    void testCreateEmployeeWithNullData() {
        // Arrange
        when(employeeRepository.save(null)).thenThrow(new IllegalArgumentException("Employee cannot be null"));

        // Act & Assert
        assertThatThrownBy(() -> employeeService.create(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Employee cannot be null");
    }

    @Test
    @DisplayName("Should update employee successfully")
    void testUpdateEmployee() {
        // Arrange
        Employee updated = Employee.builder().id(1L).name("Jane Doe").badgeId("B12345").build();
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(updated);

        // Act
        Employee result = employeeService.update(1L, updated);

        // Assert
        assertThat(result.getName()).isEqualTo("Jane Doe");
        verify(employeeRepository, times(1)).findById(1L);
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent employee")
    void testUpdateEmployeeNotFound() {
        // Arrange
        when(employeeRepository.findById(2L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> employeeService.update(2L, employee))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("Employee not found");
    }

    @Test
    @DisplayName("Should delete employee successfully")
    void testDeleteEmployee() {
        // Arrange
        doNothing().when(employeeRepository).deleteById(1L);

        // Act
        employeeService.delete(1L);

        // Assert
        verify(employeeRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent employee")
    void testDeleteEmployeeNotFound() {
        // Arrange
        doThrow(new EmptyResultDataAccessException(1)).when(employeeRepository).deleteById(2L);

        // Act & Assert
        assertThatThrownBy(() -> employeeService.delete(2L))
                .isInstanceOf(EmptyResultDataAccessException.class);
    }

    @Test
    @DisplayName("Should list all employees")
    void testListEmployees() {
        // Arrange
        List<Employee> employees = Arrays.asList(employee, 
            Employee.builder().id(2L).name("Jane").badgeId("B54321").build());
        when(employeeRepository.findAll()).thenReturn(employees);

        // Act
        List<Employee> list = employeeService.list();

        // Assert
        assertThat(list).hasSize(2);
        assertThat(list.get(0).getName()).isEqualTo("John Doe");
        assertThat(list.get(1).getName()).isEqualTo("Jane");
        verify(employeeRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return empty list when no employees exist")
    void testListEmployeesEmpty() {
        // Arrange
        when(employeeRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<Employee> list = employeeService.list();

        // Assert
        assertThat(list).isEmpty();
    }

    @Test
    @DisplayName("Should filter employees by department")
    void testFilterEmployees() {
        // Arrange
        when(employeeRepository.findByDepartmentAndActiveTrue("Logistics"))
                .thenReturn(List.of(employee));

        // Act
        List<Employee> filtered = employeeService.filter("Logistics");

        // Assert
        assertThat(filtered).hasSize(1);
        assertThat(filtered.get(0).getDepartment()).isEqualTo("Logistics");
        verify(employeeRepository, times(1)).findByDepartmentAndActiveTrue("Logistics");
    }

    @Test
    @DisplayName("Should return empty list when filtering with no matches")
    void testFilterEmployeesNoMatches() {
        // Arrange
        when(employeeRepository.findByDepartmentAndActiveTrue("NonExistent"))
                .thenReturn(Collections.emptyList());

        // Act
        List<Employee> filtered = employeeService.filter("NonExistent");

        // Assert
        assertThat(filtered).isEmpty();
    }

    @Test
    @DisplayName("Should get employee by ID")
    void testGetEmployeeById() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        // Act
        Optional<Employee> found = employeeService.getById(1L);

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("John Doe");
    }

    @Test
    @DisplayName("Should return empty when employee not found by ID")
    void testGetEmployeeByIdNotFound() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<Employee> found = employeeService.getById(999L);

        // Assert
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should perform soft delete")
    void testSoftDelete() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        // Act
        employeeService.softDelete(1L);

        // Assert
        verify(employeeRepository, times(1)).findById(1L);
        verify(employeeRepository, times(1)).save(argThat(emp -> 
            emp.isDeleted() && !emp.isActive()));
    }

    @Test
    @DisplayName("Should get paginated employees")
    void testGetPaginatedEmployees() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> page = new PageImpl<>(List.of(employee));
        when(employeeRepository.findAll(pageable)).thenReturn(page);

        // Act
        Page<Employee> result = employeeService.getPaginated(pageable);

        // Assert
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should validate unique badge ID")
    void testValidateUniqueBadgeId() {
        // Arrange
        when(employeeRepository.findByBadgeId("B12345")).thenReturn(Optional.of(employee));

        // Act
        boolean exists = employeeService.badgeIdExists("B12345");

        // Assert
        assertThat(exists).isTrue();
    }
}