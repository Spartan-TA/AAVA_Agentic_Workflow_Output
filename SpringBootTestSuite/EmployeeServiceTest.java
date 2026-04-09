package SpringBootTestSuite;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class EmployeeServiceTest {
    @Mock
    private EmployeeRepository repository;

    @InjectMocks
    private EmployeeService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Create employee with valid data returns created employee")
    void testCreateEmployee_WithValidData_ReturnsCreatedEmployee() {
        EmployeeCreateDto dto = EmployeeCreateDto.builder()
            .name("John Doe")
            .badgeId("EMP001")
            .role("Worker")
            .department("Warehouse")
            .shiftGroup("Morning")
            .hireDate(LocalDate.now())
            .status("ACTIVE")
            .build();

        Employee employee = Employee.builder()
            .id(1L)
            .name("John Doe")
            .badgeId("EMP001")
            .build();

        when(repository.save(any(Employee.class))).thenReturn(employee);

        EmployeeDto result = service.createEmployee(dto, "admin");

        assertNotNull(result);
        assertEquals("John Doe", result.getName());
        assertEquals("EMP001", result.getBadgeId());
        verify(repository, times(1)).save(any(Employee.class));
    }

    @Test
    @DisplayName("Create employee with null name throws IllegalArgumentException")
    void testCreateEmployee_WithNullName_ThrowsIllegalArgumentException() {
        EmployeeCreateDto dto = EmployeeCreateDto.builder()
            .name(null)
            .badgeId("EMP001")
            .build();

        assertThrows(IllegalArgumentException.class, () -> {
            service.createEmployee(dto, "admin");
        });
    }

    @Test
    @DisplayName("Create employee with empty badge ID throws IllegalArgumentException")
    void testCreateEmployee_WithEmptyBadgeId_ThrowsIllegalArgumentException() {
        EmployeeCreateDto dto = EmployeeCreateDto.builder()
            .name("John Doe")
            .badgeId("")
            .build();

        assertThrows(IllegalArgumentException.class, () -> {
            service.createEmployee(dto, "admin");
        });
    }

    @Test
    @DisplayName("Update employee with valid data returns updated employee")
    void testUpdateEmployee_WithValidData_ReturnsUpdatedEmployee() {
        EmployeeUpdateDto dto = EmployeeUpdateDto.builder()
            .name("Jane Doe")
            .department("Logistics")
            .build();

        Employee employee = Employee.builder()
            .id(1L)
            .name("Jane Doe")
            .department("Logistics")
            .build();

        when(repository.findById(1L)).thenReturn(Optional.of(employee));
        when(repository.save(any(Employee.class))).thenReturn(employee);

        EmployeeDto result = service.updateEmployee(1L, dto, "admin");

        assertNotNull(result);
        assertEquals("Jane Doe", result.getName());
        assertEquals("Logistics", result.getDepartment());
        verify(repository, times(1)).save(any(Employee.class));
    }

    @Test
    @DisplayName("Update employee with null DTO throws IllegalArgumentException")
    void testUpdateEmployee_WithNullDto_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> {
            service.updateEmployee(1L, null, "admin");
        });
    }

    @Test
    @DisplayName("Get employee by ID with valid ID returns employee")
    void testGetEmployeeById_WithValidId_ReturnsEmployee() {
        Employee employee = Employee.builder()
            .id(1L)
            .name("John Doe")
            .build();

        when(repository.findById(1L)).thenReturn(Optional.of(employee));

        EmployeeDto result = service.getEmployeeById(1L);

        assertNotNull(result);
        assertEquals("John Doe", result.getName());
        verify(repository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Get employee by ID with non-existent ID throws EntityNotFoundException")
    void testGetEmployeeById_WithNonExistentId_ThrowsEntityNotFoundException() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            service.getEmployeeById(999L);
        });
    }

    @Test
    @DisplayName("Get all employees with valid page and size returns page of employees")
    void testGetAllEmployees_WithValidPageAndSize_ReturnsPageOfEmployees() {
        List<Employee> employees = Arrays.asList(
            Employee.builder().id(1L).name("John Doe").build(),
            Employee.builder().id(2L).name("Jane Doe").build()
        );
        Page<Employee> employeePage = new PageImpl<>(employees);
        when(repository.findAll(any(Pageable.class))).thenReturn(employeePage);

        Page<EmployeeDto> result = service.getAllEmployees(0, 2);

        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        verify(repository, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @DisplayName("Delete employee with valid ID deletes employee")
    void testDeleteEmployee_WithValidId_DeletesEmployee() {
        Employee employee = Employee.builder().id(1L).build();
        when(repository.findById(1L)).thenReturn(Optional.of(employee));
        doNothing().when(repository).delete(employee);

        service.deleteEmployee(1L, "admin");

        verify(repository, times(1)).delete(employee);
    }

    @Test
    @DisplayName("Delete employee with non-existent ID throws EntityNotFoundException")
    void testDeleteEmployee_WithNonExistentId_ThrowsEntityNotFoundException() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            service.deleteEmployee(999L, "admin");
        });
    }

    @Test
    @DisplayName("Search employees with valid parameters returns list")
    void testSearchEmployees_WithValidParameters_ReturnsList() {
        List<Employee> employees = Arrays.asList(
            Employee.builder().id(1L).name("John Doe").department("Warehouse").status("ACTIVE").build()
        );
        when(repository.findByDepartment("Warehouse")).thenReturn(employees);

        List<EmployeeDto> result = service.searchEmployees("John Doe", "Warehouse", "ACTIVE");

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(repository, times(1)).findByDepartment("Warehouse");
    }

    @Test
    @DisplayName("Search employees with null parameters returns empty list")
    void testSearchEmployees_WithNullParameters_ReturnsEmptyList() {
        when(repository.findByDepartment(null)).thenReturn(Collections.emptyList());

        List<EmployeeDto> result = service.searchEmployees(null, null, null);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Create employee with boundary hire date returns created employee")
    void testCreateEmployee_WithBoundaryHireDate_ReturnsCreatedEmployee() {
        EmployeeCreateDto dto = EmployeeCreateDto.builder()
            .name("Boundary Test")
            .badgeId("EMP002")
            .hireDate(LocalDate.of(1900, 1, 1))
            .build();

        Employee employee = Employee.builder()
            .id(2L)
            .name("Boundary Test")
            .badgeId("EMP002")
            .hireDate(LocalDate.of(1900, 1, 1))
            .build();

        when(repository.save(any(Employee.class))).thenReturn(employee);

        EmployeeDto result = service.createEmployee(dto, "admin");

        assertNotNull(result);
        assertEquals(LocalDate.of(1900, 1, 1), result.getHireDate());
    }

    @Test
    @DisplayName("Update employee with empty department throws IllegalArgumentException")
    void testUpdateEmployee_WithEmptyDepartment_ThrowsIllegalArgumentException() {
        EmployeeUpdateDto dto = EmployeeUpdateDto.builder()
            .department("")
            .build();

        assertThrows(IllegalArgumentException.class, () -> {
            service.updateEmployee(1L, dto, "admin");
        });
    }

    @Test
    @DisplayName("Get all employees with negative page throws IllegalArgumentException")
    void testGetAllEmployees_WithNegativePage_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> {
            service.getAllEmployees(-1, 10);
        });
    }

    @Test
    @DisplayName("Get all employees with zero size throws IllegalArgumentException")
    void testGetAllEmployees_WithZeroSize_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> {
            service.getAllEmployees(0, 0);
        });
    }
}
