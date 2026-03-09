package employee;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.*;

import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
public class EmployeeServiceTest {

    @MockBean
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
    public void testCreateEmployee_ValidInput_Success() {
        Employee employee = new Employee();
        employee.setBadgeId("EMP001");
        employee.setName("John Doe");
        employee.setRole(Role.WORKER);
        employee.setHireDate(LocalDate.now());
        employee.setStatus(EmployeeStatus.ACTIVE);

        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        Employee result = employeeService.createEmployee(employee);

        assertNotNull(result);
        assertEquals("EMP001", result.getBadgeId());
        assertEquals("John Doe", result.getName());
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    public void testCreateEmployee_NullBadgeId_ThrowsException() {
        Employee employee = new Employee();
        employee.setName("John Doe");
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.createEmployee(employee);
        });
    }

    @Test
    public void testCreateEmployee_DuplicateBadgeId_ThrowsException() {
        Employee employee = new Employee();
        employee.setBadgeId("EMP001");
        when(employeeRepository.findByBadgeId("EMP001")).thenReturn(Optional.of(new Employee()));
        assertThrows(DataIntegrityViolationException.class, () -> {
            employeeService.createEmployee(employee);
        });
    }

    @Test
    public void testCreateEmployee_EmptyName_ThrowsException() {
        Employee employee = new Employee();
        employee.setBadgeId("EMP001");
        employee.setName("");
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.createEmployee(employee);
        });
    }

    @Test
    public void testSoftDeleteEmployee_ValidId_Success() {
        Long employeeId = 1L;
        Employee employee = new Employee();
        employee.setId(employeeId);
        employee.setStatus(EmployeeStatus.ACTIVE);
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);
        employeeService.softDeleteEmployee(employeeId);
        assertEquals(EmployeeStatus.INACTIVE, employee.getStatus());
        verify(employeeRepository, times(1)).save(employee);
    }

    @Test
    public void testSoftDeleteEmployee_NotFound_ThrowsException() {
        Long employeeId = 2L;
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> {
            employeeService.softDeleteEmployee(employeeId);
        });
    }

    @Test
    public void testGetEmployeeById_ValidId_ReturnsEmployee() {
        Long employeeId = 1L;
        Employee employee = new Employee();
        employee.setId(employeeId);
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
        Employee result = employeeService.getEmployeeById(employeeId);
        assertNotNull(result);
        assertEquals(employeeId, result.getId());
    }

    @Test
    public void testGetEmployeeById_NotFound_ThrowsException() {
        Long employeeId = 2L;
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> {
            employeeService.getEmployeeById(employeeId);
        });
    }

    @Test
    public void testUpdateEmployee_ValidInput_Success() {
        Long employeeId = 1L;
        Employee existing = new Employee();
        existing.setId(employeeId);
        existing.setBadgeId("EMP001");
        existing.setName("John Doe");
        existing.setStatus(EmployeeStatus.ACTIVE);
        Employee update = new Employee();
        update.setName("Jane Smith");
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(existing));
        when(employeeRepository.save(any(Employee.class))).thenReturn(existing);
        Employee result = employeeService.updateEmployee(employeeId, update);
        assertEquals("Jane Smith", result.getName());
    }

    @Test
    public void testUpdateEmployee_DuplicateBadgeId_ThrowsException() {
        Long employeeId = 1L;
        Employee existing = new Employee();
        existing.setId(employeeId);
        existing.setBadgeId("EMP001");
        Employee update = new Employee();
        update.setBadgeId("EMP002");
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(existing));
        when(employeeRepository.findByBadgeId("EMP002")).thenReturn(Optional.of(new Employee()));
        assertThrows(DataIntegrityViolationException.class, () -> {
            employeeService.updateEmployee(employeeId, update);
        });
    }

    @Test
    public void testGetAllEmployees_Pagination_Success() {
        List<Employee> employees = Arrays.asList(new Employee(), new Employee());
        when(employeeRepository.findAll(any())).thenReturn(new PageImpl<>(employees));
        Page<Employee> result = employeeService.getAllEmployees(PageRequest.of(0, 10));
        assertEquals(2, result.getContent().size());
    }

    @Test
    public void testGetAllEmployees_EmptyList() {
        when(employeeRepository.findAll(any())).thenReturn(new PageImpl<>(Collections.emptyList()));
        Page<Employee> result = employeeService.getAllEmployees(PageRequest.of(0, 10));
        assertTrue(result.getContent().isEmpty());
    }

    @Test
    public void testCreateEmployee_InvalidRole_ThrowsException() {
        Employee employee = new Employee();
        employee.setBadgeId("EMP003");
        employee.setName("Invalid Role");
        employee.setRole(null);
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.createEmployee(employee);
        });
    }

    @Test
    public void testCreateEmployee_NullEmployee_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.createEmployee(null);
        });
    }

    @Test
    public void testSoftDeleteEmployee_AlreadyInactive_NoOp() {
        Long employeeId = 3L;
        Employee employee = new Employee();
        employee.setId(employeeId);
        employee.setStatus(EmployeeStatus.INACTIVE);
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
        employeeService.softDeleteEmployee(employeeId);
        assertEquals(EmployeeStatus.INACTIVE, employee.getStatus());
        verify(employeeRepository, never()).save(employee);
    }

    @Test
    public void testCreateEmployee_AccessDenied_ThrowsException() {
        Employee employee = new Employee();
        employee.setBadgeId("EMP004");
        employee.setName("Access Denied");
        doThrow(new AccessDeniedException("Forbidden")).when(employeeRepository).save(any(Employee.class));
        assertThrows(AccessDeniedException.class, () -> {
            employeeService.createEmployee(employee);
        });
    }
}
