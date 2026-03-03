import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class EmployeeServiceTest {
    @Mock
    private EmployeeRepository repository;

    @InjectMocks
    private EmployeeService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Should create employee with valid data")
    void testCreateEmployee_NormalCase() {
        Employee employee = new Employee(null, "B123", "John Doe", "WORKER", "Shipping", "A", LocalDate.now(), EmployeeStatus.ACTIVE, false);
        when(repository.save(any(Employee.class))).thenReturn(employee);

        Employee result = service.createEmployee(employee);

        assertNotNull(result);
        assertEquals("B123", result.getBadgeId());
        verify(repository, times(1)).save(any(Employee.class));
    }

    @Test
    @DisplayName("Should throw ValidationException for duplicate badgeId")
    void testCreateEmployee_DuplicateBadgeId() {
        Employee employee = new Employee(null, "B123", "John Doe", "WORKER", "Shipping", "A", LocalDate.now(), EmployeeStatus.ACTIVE, false);
        when(repository.findByBadgeId("B123")).thenReturn(Optional.of(employee));

        assertThrows(ValidationException.class, () -> service.createEmployee(employee));
    }

    @Test
    @DisplayName("Should update employee details")
    void testUpdateEmployee_NormalCase() {
        Employee existing = new Employee(1L, "B123", "John Doe", "WORKER", "Shipping", "A", LocalDate.now(), EmployeeStatus.ACTIVE, false);
        Employee updated = new Employee(1L, "B123", "Jane Doe", "SUPERVISOR", "Receiving", "B", LocalDate.now(), EmployeeStatus.ACTIVE, false);

        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any(Employee.class))).thenReturn(updated);

        Employee result = service.updateEmployee(1L, updated);

        assertEquals("Jane Doe", result.getName());
        assertEquals("SUPERVISOR", result.getRole());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when updating non-existent employee")
    void testUpdateEmployee_ResourceNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        Employee updated = new Employee(99L, "B999", "Ghost", "WORKER", "Shipping", "A", LocalDate.now(), EmployeeStatus.ACTIVE, false);

        assertThrows(ResourceNotFoundException.class, () -> service.updateEmployee(99L, updated));
    }

    @Test
    @DisplayName("Should soft delete employee")
    void testSoftDeleteEmployee_NormalCase() {
        Employee existing = new Employee(1L, "B123", "John Doe", "WORKER", "Shipping", "A", LocalDate.now(), EmployeeStatus.ACTIVE, false);
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any(Employee.class))).thenReturn(existing);

        service.softDeleteEmployee(1L);

        assertTrue(existing.isDeleted());
        verify(repository, times(1)).save(existing);
    }

    @Test
    @DisplayName("Should list active employees")
    void testListEmployees_NormalCase() {
        List<Employee> employees = Arrays.asList(
            new Employee(1L, "B123", "John Doe", "WORKER", "Shipping", "A", LocalDate.now(), EmployeeStatus.ACTIVE, false),
            new Employee(2L, "B124", "Jane Doe", "SUPERVISOR", "Receiving", "B", LocalDate.now(), EmployeeStatus.ACTIVE, false)
        );
        when(repository.findAllByDeletedFalse()).thenReturn(employees);

        List<Employee> result = service.listEmployees();

        assertEquals(2, result.size());
        assertFalse(result.get(0).isDeleted());
    }

    @Test
    @DisplayName("Should find employee by badgeId")
    void testFindByBadgeId_NormalCase() {
        Employee employee = new Employee(1L, "B123", "John Doe", "WORKER", "Shipping", "A", LocalDate.now(), EmployeeStatus.ACTIVE, false);
        when(repository.findByBadgeId("B123")).thenReturn(Optional.of(employee));

        Employee result = service.findByBadgeId("B123");

        assertNotNull(result);
        assertEquals("B123", result.getBadgeId());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException for unknown badgeId")
    void testFindByBadgeId_ResourceNotFound() {
        when(repository.findByBadgeId("B999")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.findByBadgeId("B999"));
    }

    @Test
    @DisplayName("Should handle null input for createEmployee")
    void testCreateEmployee_NullInput() {
        assertThrows(ValidationException.class, () -> service.createEmployee(null));
    }

    @Test
    @DisplayName("Should handle empty department filter")
    void testFindByDepartmentAndDeletedFalse_EmptyDepartment() {
        when(repository.findByDepartmentAndDeletedFalse("")).thenReturn(Collections.emptyList());

        List<Employee> result = service.findByDepartmentAndDeletedFalse("");

        assertTrue(result.isEmpty());
    }
}