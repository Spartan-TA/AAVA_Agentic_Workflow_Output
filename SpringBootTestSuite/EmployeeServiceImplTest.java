import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class EmployeeServiceImplTest {
    @Mock
    private EmployeeRepository employeeRepository;
    @InjectMocks
    private EmployeeServiceImpl employeeService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("testCreateEmployee_ValidInput_ReturnsEmployee")
    void testCreateEmployee_ValidInput_ReturnsEmployee() {
        Employee employee = new Employee("John", "Doe", "12345", "john.doe@email.com");
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);
        Employee result = employeeService.createEmployee(employee);
        assertEquals(employee, result);
        verify(employeeRepository).save(employee);
    }

    @Test
    @DisplayName("testCreateEmployee_DuplicateBadgeId_ThrowsException")
    void testCreateEmployee_DuplicateBadgeId_ThrowsException() {
        Employee employee = new Employee("Jane", "Smith", "12345", "jane.smith@email.com");
        when(employeeRepository.findByBadgeId("12345")).thenReturn(employee);
        assertThrows(DuplicateBadgeIdException.class, () -> employeeService.createEmployee(employee));
    }

    @Test
    @DisplayName("testCreateEmployee_NullInput_ThrowsIllegalArgumentException")
    void testCreateEmployee_NullInput_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> employeeService.createEmployee(null));
    }

    @Test
    @DisplayName("testGetEmployeeById_ValidId_ReturnsEmployee")
    void testGetEmployeeById_ValidId_ReturnsEmployee() {
        Employee employee = new Employee("John", "Doe", "12345", "john.doe@email.com");
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        Employee result = employeeService.getEmployeeById(1L);
        assertEquals(employee, result);
    }

    @Test
    @DisplayName("testGetEmployeeById_InvalidId_ThrowsEmployeeNotFoundException")
    void testGetEmployeeById_InvalidId_ThrowsEmployeeNotFoundException() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(EmployeeNotFoundException.class, () -> employeeService.getEmployeeById(99L));
    }

    @Test
    @DisplayName("testGetAllEmployees_Pagination_ReturnsPagedList")
    void testGetAllEmployees_Pagination_ReturnsPagedList() {
        List<Employee> employees = Arrays.asList(new Employee("John", "Doe", "12345", "john.doe@email.com"));
        when(employeeRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(employees));
        Page<Employee> result = employeeService.getAllEmployees(PageRequest.of(0, 10));
        assertEquals(1, result.getTotalElements());
    }

    @Test
    @DisplayName("testUpdateEmployee_ValidInput_UpdatesEmployee")
    void testUpdateEmployee_ValidInput_UpdatesEmployee() {
        Employee employee = new Employee("John", "Doe", "12345", "john.doe@email.com");
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);
        Employee updated = employeeService.updateEmployee(1L, employee);
        assertEquals(employee, updated);
    }

    @Test
    @DisplayName("testUpdateEmployee_NullInput_ThrowsIllegalArgumentException")
    void testUpdateEmployee_NullInput_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> employeeService.updateEmployee(1L, null));
    }

    @Test
    @DisplayName("testDeleteEmployee_ValidId_SoftDeletesEmployee")
    void testDeleteEmployee_ValidId_SoftDeletesEmployee() {
        Employee employee = new Employee("John", "Doe", "12345", "john.doe@email.com");
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        doNothing().when(employeeRepository).save(any(Employee.class));
        employeeService.deleteEmployee(1L);
        assertTrue(employee.isDeleted());
    }

    @Test
    @DisplayName("testDeleteEmployee_InvalidId_ThrowsEmployeeNotFoundException")
    void testDeleteEmployee_InvalidId_ThrowsEmployeeNotFoundException() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(EmployeeNotFoundException.class, () -> employeeService.deleteEmployee(99L));
    }

    @Test
    @DisplayName("testFindByBadgeId_ValidBadgeId_ReturnsEmployee")
    void testFindByBadgeId_ValidBadgeId_ReturnsEmployee() {
        Employee employee = new Employee("John", "Doe", "12345", "john.doe@email.com");
        when(employeeRepository.findByBadgeId("12345")).thenReturn(employee);
        Employee result = employeeService.findByBadgeId("12345");
        assertEquals(employee, result);
    }

    @Test
    @DisplayName("testFindByBadgeId_InvalidBadgeId_ReturnsNull")
    void testFindByBadgeId_InvalidBadgeId_ReturnsNull() {
        when(employeeRepository.findByBadgeId("99999")).thenReturn(null);
        Employee result = employeeService.findByBadgeId("99999");
        assertNull(result);
    }

    @Test
    @DisplayName("testSearchEmployees_EmptyString_ReturnsEmptyList")
    void testSearchEmployees_EmptyString_ReturnsEmptyList() {
        when(employeeRepository.searchEmployees("")).thenReturn(Collections.emptyList());
        List<Employee> result = employeeService.searchEmployees("");
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("testSearchEmployees_InvalidFormat_ReturnsEmptyList")
    void testSearchEmployees_InvalidFormat_ReturnsEmptyList() {
        when(employeeRepository.searchEmployees("!@#$%^&*")).thenReturn(Collections.emptyList());
        List<Employee> result = employeeService.searchEmployees("!@#$%^&*");
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("testCreateEmployee_InvalidEmail_ThrowsValidationException")
    void testCreateEmployee_InvalidEmail_ThrowsValidationException() {
        Employee employee = new Employee("John", "Doe", "12345", "invalid-email");
        assertThrows(ValidationException.class, () -> employeeService.createEmployee(employee));
    }

    @AfterEach
    void tearDown() {
        // Cleanup if needed
    }
}
