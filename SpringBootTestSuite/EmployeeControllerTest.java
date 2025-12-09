import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.http.ResponseEntity;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;

public class EmployeeControllerTest {
    @Mock
    private EmployeeService employeeService;
    @InjectMocks
    private EmployeeController employeeController;

    private AutoCloseable closeable;

    @BeforeEach
    public void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    public void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    public void testCreateEmployeeValid() {
        EmployeeDTO dto = new EmployeeDTO("John Doe", "B123", "WORKER", "Shipping", "A", new Date(), "ACTIVE");
        when(employeeService.createEmployee(any(EmployeeDTO.class))).thenReturn(dto);
        ResponseEntity<EmployeeDTO> response = employeeController.createEmployee(dto);
        assertEquals(201, response.getStatusCodeValue());
        assertEquals(dto, response.getBody());
    }

    @Test
    public void testCreateEmployeeNullInput() {
        assertThrows(IllegalArgumentException.class, () -> employeeController.createEmployee(null));
    }

    @Test
    public void testGetEmployeeByIdValid() {
        EmployeeDTO dto = new EmployeeDTO("Jane Doe", "B124", "HR", "Admin", "B", new Date(), "ACTIVE");
        when(employeeService.getEmployeeById("B124")).thenReturn(dto);
        ResponseEntity<EmployeeDTO> response = employeeController.getEmployeeById("B124");
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(dto, response.getBody());
    }

    @Test
    public void testGetEmployeeByIdNotFound() {
        when(employeeService.getEmployeeById("X999")).thenReturn(null);
        ResponseEntity<EmployeeDTO> response = employeeController.getEmployeeById("X999");
        assertEquals(404, response.getStatusCodeValue());
        assertNull(response.getBody());
    }

    @Test
    public void testDeleteEmployeeValid() {
        doNothing().when(employeeService).deleteEmployee("B123");
        ResponseEntity<Void> response = employeeController.deleteEmployee("B123");
        assertEquals(204, response.getStatusCodeValue());
    }

    @Test
    public void testDeleteEmployeeInvalidId() {
        doThrow(new IllegalArgumentException("Invalid badgeId")).when(employeeService).deleteEmployee("BAD_ID");
        assertThrows(IllegalArgumentException.class, () -> employeeController.deleteEmployee("BAD_ID"));
    }
}