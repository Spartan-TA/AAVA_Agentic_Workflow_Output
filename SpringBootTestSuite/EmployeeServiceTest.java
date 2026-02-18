package SpringBootTestSuite;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.dto.EmployeeDTO;
import com.example.entity.Employee;
import com.example.repository.EmployeeRepository;
import com.example.service.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;

import java.time.LocalDate;
import java.util.*;

class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeService employeeService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void create_ShouldReturnSavedEmployee() {
        EmployeeDTO dto = new EmployeeDTO("John", "B123", "Worker", "Logistics", "A", LocalDate.now(), "ACTIVE");
        Employee saved = new Employee(1L, "John", "B123", "Worker", "Logistics", "A", LocalDate.now(), "ACTIVE", false);

        when(employeeRepository.save(any(Employee.class))).thenReturn(saved);

        Employee result = employeeService.create(dto);

        assertNotNull(result);
        assertEquals("John", result.getName());
        verify(employeeRepository).save(any(Employee.class));
    }

    @Test
    void getById_ShouldReturnEmployee_WhenFound() {
        Employee emp = new Employee(1L, "Jane", "B124", "Manager", "Ops", "B", LocalDate.now(), "ACTIVE", false);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(emp));

        Employee result = employeeService.getById(1L);

        assertNotNull(result);
        assertEquals("Jane", result.getName());
    }

    @Test
    void getById_ShouldThrowException_WhenNotFound() {
        when(employeeRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> employeeService.getById(2L));
    }

    @Test
    void findAll_ShouldReturnPageOfEmployees() {
        List<Employee> employees = Arrays.asList(
            new Employee(1L, "A", "B1", "Role", "Dept", "S1", LocalDate.now(), "ACTIVE", false)
        );
        Page<Employee> page = new PageImpl<>(employees);
        Pageable pageable = PageRequest.of(0, 10);

        when(employeeRepository.findAll(pageable)).thenReturn(page);

        Page<Employee> result = employeeService.findAll(pageable);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void update_ShouldUpdateEmployee() {
        EmployeeDTO dto = new EmployeeDTO("Updated", "B999", "Lead", "IT", "C", LocalDate.now(), "INACTIVE");
        Employee existing = new Employee(1L, "Old", "B1", "Role", "Dept", "S1", LocalDate.now(), "ACTIVE", false);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(employeeRepository.save(any(Employee.class))).thenReturn(existing);

        Employee result = employeeService.update(1L, dto);

        assertEquals("Updated", result.getName());
    }

    @Test
    void update_ShouldThrowException_WhenNotFound() {
        EmployeeDTO dto = new EmployeeDTO("X", "Y", "Z", "D", "E", LocalDate.now(), "ACTIVE");
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> employeeService.update(99L, dto));
    }

    @Test
    void delete_ShouldMarkEmployeeAsDeleted() {
        Employee emp = new Employee(1L, "Del", "B2", "Role", "Dept", "S2", LocalDate.now(), "ACTIVE", false);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(emp));
        when(employeeRepository.save(any(Employee.class))).thenReturn(emp);

        employeeService.delete(1L);

        assertTrue(emp.isDeleted());
    }

    @Test
    void delete_ShouldThrowException_WhenNotFound() {
        when(employeeRepository.findById(100L)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> employeeService.delete(100L));
    }

    @Test
    void create_ShouldHandleNullInput() {
        assertThrows(IllegalArgumentException.class, () -> employeeService.create(null));
    }
}