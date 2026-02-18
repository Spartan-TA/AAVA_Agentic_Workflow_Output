package SpringBootTestSuite;

import com.example.controller.EmployeeController;
import com.example.dto.EmployeeDTO;
import com.example.entity.Employee;
import com.example.service.EmployeeService;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmployeeControllerTest {

    @Mock
    private EmployeeService employeeService;

    @InjectMocks
    private EmployeeController employeeController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createEmployee_ShouldReturnCreatedEmployee() {
        EmployeeDTO dto = new EmployeeDTO("John", "B123", "Worker", "Logistics", "A", LocalDate.now(), "ACTIVE");
        Employee emp = new Employee(1L, "John", "B123", "Worker", "Logistics", "A", LocalDate.now(), "ACTIVE", false);

        when(employeeService.create(dto)).thenReturn(emp);

        ResponseEntity<Employee> response = employeeController.createEmployee(dto);

        assertEquals(201, response.getStatusCodeValue());
        assertEquals("John", response.getBody().getName());
    }

    @Test
    void getEmployeeById_ShouldReturnEmployee() {
        Employee emp = new Employee(1L, "Jane", "B124", "Manager", "Ops", "B", LocalDate.now(), "ACTIVE", false);
        when(employeeService.getById(1L)).thenReturn(emp);

        ResponseEntity<Employee> response = employeeController.getEmployeeById(1L);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Jane", response.getBody().getName());
    }

    @Test
    void getEmployeeById_ShouldReturnNotFound() {
        when(employeeService.getById(2L)).thenThrow(new NoSuchElementException());
        ResponseEntity<Employee> response = employeeController.getEmployeeById(2L);

        assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    void getAllEmployees_ShouldReturnPage() {
        List<Employee> employees = Arrays.asList(
            new Employee(1L, "A", "B1", "Role", "Dept", "S1", LocalDate.now(), "ACTIVE", false)
        );
        Page<Employee> page = new PageImpl<>(employees);
        when(employeeService.findAll(any(Pageable.class))).thenReturn(page);

        ResponseEntity<Page<Employee>> response = employeeController.getAllEmployees(PageRequest.of(0, 10));

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().getTotalElements());
    }

    @Test
    void updateEmployee_ShouldReturnUpdatedEmployee() {
        EmployeeDTO dto = new EmployeeDTO("Updated", "B999", "Lead", "IT", "C", LocalDate.now(), "INACTIVE");
        Employee emp = new Employee(1L, "Updated", "B999", "Lead", "IT", "C", LocalDate.now(), "INACTIVE", false);

        when(employeeService.update(1L, dto)).thenReturn(emp);

        ResponseEntity<Employee> response = employeeController.updateEmployee(1L, dto);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Updated", response.getBody().getName());
    }

    @Test
    void deleteEmployee_ShouldReturnNoContent() {
        doNothing().when(employeeService).delete(1L);

        ResponseEntity<Void> response = employeeController.deleteEmployee(1L);

        assertEquals(204, response.getStatusCodeValue());
    }
}