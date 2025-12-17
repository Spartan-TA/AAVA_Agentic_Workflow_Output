package SpringBootTestSuite;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.*;

import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.example.ems.domain.*;
import com.example.ems.repository.*;
import com.example.ems.service.*;
import com.example.ems.exception.*;

@ExtendWith(SpringExtension.class)
public class EmployeeServiceTest {
    @Mock
    private EmployeeRepository employeeRepository;
    @InjectMocks
    private EmployeeService employeeService;

    private Employee validEmployee;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        validEmployee = Employee.builder()
                .id(1L)
                .name("John Doe")
                .badgeId("BADGE123")
                .role("WORKER")
                .department("Shipping")
                .shiftGroup("A")
                .hireDate(LocalDate.of(2020, 1, 1))
                .status("ACTIVE")
                .build();
    }

    @Test
    void testCreateEmployee_WithValidData_ShouldReturnEmployee() {
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);
        Employee result = employeeService.createEmployee(validEmployee);
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("John Doe");
        verify(employeeRepository).save(any(Employee.class));
    }

    @Test
    void testCreateEmployee_WithNullName_ShouldThrowValidationException() {
        Employee emp = Employee.builder().name(null).badgeId("BADGE124").build();
        assertThrows(ValidationException.class, () -> employeeService.createEmployee(emp));
    }

    @Test
    void testCreateEmployee_WithEmptyName_ShouldThrowValidationException() {
        Employee emp = Employee.builder().name("").badgeId("BADGE125").build();
        assertThrows(ValidationException.class, () -> employeeService.createEmployee(emp));
    }

    @Test
    void testCreateEmployee_WithDuplicateBadgeId_ShouldThrowException() {
        when(employeeRepository.existsByBadgeId("BADGE123")).thenReturn(true);
        Employee emp = Employee.builder().name("Jane").badgeId("BADGE123").build();
        assertThrows(DataIntegrityViolationException.class, () -> employeeService.createEmployee(emp));
    }

    @Test
    void testUpdateEmployee_WithValidId_ShouldUpdateEmployee() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);
        Employee updated = Employee.builder().id(1L).name("John Smith").badgeId("BADGE123").build();
        Employee result = employeeService.updateEmployee(1L, updated);
        assertThat(result.getName()).isEqualTo("John Smith");
    }

    @Test
    void testUpdateEmployee_WithInvalidId_ShouldThrowResourceNotFoundException() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());
        Employee updated = Employee.builder().id(99L).name("Ghost").badgeId("BADGE999").build();
        assertThrows(ResourceNotFoundException.class, () -> employeeService.updateEmployee(99L, updated));
    }

    @Test
    void testDeleteEmployee_ShouldSoftDelete() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        employeeService.deleteEmployee(1L);
        assertThat(validEmployee.getStatus()).isEqualTo("INACTIVE");
        verify(employeeRepository).save(validEmployee);
    }

    @Test
    void testFindByBadgeId_ShouldReturnEmployee() {
        when(employeeRepository.findByBadgeId("BADGE123")).thenReturn(Optional.of(validEmployee));
        Employee result = employeeService.findByBadgeId("BADGE123");
        assertThat(result).isNotNull();
        assertThat(result.getBadgeId()).isEqualTo("BADGE123");
    }

    @Test
    void testFindByBadgeId_WithInvalidBadgeId_ShouldThrowResourceNotFoundException() {
        when(employeeRepository.findByBadgeId("BADGE404")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> employeeService.findByBadgeId("BADGE404"));
    }

    @Test
    void testFindByDepartment_ShouldReturnEmployees() {
        List<Employee> employees = Arrays.asList(validEmployee);
        when(employeeRepository.findByDepartment("Shipping")).thenReturn(employees);
        List<Employee> result = employeeService.findByDepartment("Shipping");
        assertThat(result).hasSize(1);
    }

    @Test
    void testFindByStatus_ShouldReturnEmployees() {
        List<Employee> employees = Arrays.asList(validEmployee);
        when(employeeRepository.findByStatus("ACTIVE")).thenReturn(employees);
        List<Employee> result = employeeService.findByStatus("ACTIVE");
        assertThat(result).hasSize(1);
    }

    @Test
    void testBulkImportFromCSV_WithValidData_ShouldImportEmployees() {
        List<Employee> employees = Arrays.asList(validEmployee);
        when(employeeRepository.saveAll(anyList())).thenReturn(employees);
        List<Employee> result = employeeService.bulkImportFromCSV(employees);
        assertThat(result).hasSize(1);
        verify(employeeRepository).saveAll(anyList());
    }

    @Test
    void testBulkImportFromCSV_WithInvalidData_ShouldThrowValidationException() {
        List<Employee> employees = Arrays.asList(Employee.builder().name(null).badgeId("").build());
        assertThrows(ValidationException.class, () -> employeeService.bulkImportFromCSV(employees));
    }

    // Edge case: Empty list
    @Test
    void testBulkImportFromCSV_WithEmptyList_ShouldReturnEmptyList() {
        List<Employee> employees = Collections.emptyList();
        List<Employee> result = employeeService.bulkImportFromCSV(employees);
        assertThat(result).isEmpty();
    }

    // Edge case: Null input
    @Test
    void testBulkImportFromCSV_WithNullInput_ShouldThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> employeeService.bulkImportFromCSV(null));
    }

    @AfterEach
    void tearDown() {
        // Cleanup if necessary
    }
}
