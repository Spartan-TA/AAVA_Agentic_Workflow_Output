import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.*;
import org.mockito.*;
import java.util.*;

public class EmployeeRepositoryTest {
    @Mock
    private EmployeeRepository employeeRepository;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testFindById_ValidId() {
        Employee employee = new Employee(1L, "John Doe", "B123", "HR", "Active");
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        Optional<Employee> result = employeeRepository.findById(1L);
        assertTrue(result.isPresent());
        assertEquals("John Doe", result.get().getName());
    }

    @Test
    public void testFindById_InvalidId() {
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());
        Optional<Employee> result = employeeRepository.findById(999L);
        assertFalse(result.isPresent());
    }

    @Test
    public void testSave_ValidEmployee() {
        Employee employee = new Employee(null, "Jane Smith", "B124", "Finance", "Active");
        when(employeeRepository.save(employee)).thenReturn(new Employee(2L, "Jane Smith", "B124", "Finance", "Active"));
        Employee saved = employeeRepository.save(employee);
        assertNotNull(saved.getId());
        assertEquals("Jane Smith", saved.getName());
    }

    @Test
    public void testSave_NullEmployee() {
        when(employeeRepository.save(null)).thenThrow(new IllegalArgumentException("Employee cannot be null"));
        assertThrows(IllegalArgumentException.class, () -> employeeRepository.save(null));
    }

    @Test
    public void testDeleteById_ValidId() {
        doNothing().when(employeeRepository).deleteById(1L);
        employeeRepository.deleteById(1L);
        verify(employeeRepository, times(1)).deleteById(1L);
    }

    @Test
    public void testDeleteById_InvalidId() {
        doThrow(new NoSuchElementException("Employee not found")).when(employeeRepository).deleteById(999L);
        assertThrows(NoSuchElementException.class, () -> employeeRepository.deleteById(999L));
    }

    @Test
    public void testFindAll() {
        List<Employee> employees = Arrays.asList(
            new Employee(1L, "John Doe", "B123", "HR", "Active"),
            new Employee(2L, "Jane Smith", "B124", "Finance", "Active")
        );
        when(employeeRepository.findAll()).thenReturn(employees);
        List<Employee> result = employeeRepository.findAll();
        assertEquals(2, result.size());
    }

    @Test
    public void testFindByBadgeId_ValidBadge() {
        Employee employee = new Employee(1L, "John Doe", "B123", "HR", "Active");
        when(employeeRepository.findByBadgeId("B123")).thenReturn(Optional.of(employee));
        Optional<Employee> result = employeeRepository.findByBadgeId("B123");
        assertTrue(result.isPresent());
        assertEquals("John Doe", result.get().getName());
    }

    @Test
    public void testFindByBadgeId_EmptyBadge() {
        when(employeeRepository.findByBadgeId("")).thenReturn(Optional.empty());
        Optional<Employee> result = employeeRepository.findByBadgeId("");
        assertFalse(result.isPresent());
    }

    @AfterEach
    public void tearDown() {
        // Clean up resources if needed
    }
}
