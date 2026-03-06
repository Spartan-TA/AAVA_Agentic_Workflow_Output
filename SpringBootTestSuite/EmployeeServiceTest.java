package SpringBootTestSuite;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Collections;

public class EmployeeServiceTest {
    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeService employeeService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateEmployee_validInput() {
        EmployeeDTO dto = new EmployeeDTO("BADGE123", "John", "Doe");
        when(employeeRepository.existsByBadgeId("BADGE123")).thenReturn(false);
        Employee saved = new Employee();
        saved.setId(1L);
        saved.setBadgeId("BADGE123");
        saved.setFirstName("John");
        saved.setLastName("Doe");
        when(employeeRepository.save(any(Employee.class))).thenReturn(saved);

        Employee result = employeeService.createEmployee(dto);
        assertEquals("BADGE123", result.getBadgeId());
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
    }

    @Test
    void testCreateEmployee_duplicateBadgeId() {
        EmployeeDTO dto = new EmployeeDTO("BADGE123", "John", "Doe");
        when(employeeRepository.existsByBadgeId("BADGE123")).thenReturn(true);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> employeeService.createEmployee(dto));
        assertEquals("Badge ID already exists", ex.getMessage());
    }

    @Test
    void testCreateEmployee_nullBadgeId() {
        EmployeeDTO dto = new EmployeeDTO(null, "John", "Doe");
        when(employeeRepository.existsByBadgeId(null)).thenReturn(false);
        Employee saved = new Employee();
        saved.setId(1L);
        saved.setBadgeId(null);
        saved.setFirstName("John");
        saved.setLastName("Doe");
        when(employeeRepository.save(any(Employee.class))).thenReturn(saved);
        Employee result = employeeService.createEmployee(dto);
        assertNull(result.getBadgeId());
    }

    @Test
    void testCreateEmployee_emptyBadgeId() {
        EmployeeDTO dto = new EmployeeDTO("", "John", "Doe");
        when(employeeRepository.existsByBadgeId("")).thenReturn(false);
        Employee saved = new Employee();
        saved.setId(1L);
        saved.setBadgeId("");
        saved.setFirstName("John");
        saved.setLastName("Doe");
        when(employeeRepository.save(any(Employee.class))).thenReturn(saved);
        Employee result = employeeService.createEmployee(dto);
        assertEquals("", result.getBadgeId());
    }

    @Test
    void testGetEmployeeById_valid() {
        Employee emp = new Employee();
        emp.setId(1L);
        emp.setBadgeId("BADGE123");
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(emp));
        Employee result = employeeService.getEmployeeById(1L);
        assertEquals(1L, result.getId());
    }

    @Test
    void testGetEmployeeById_notFound() {
        when(employeeRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> employeeService.getEmployeeById(2L));
    }

    @Test
    void testUpdateEmployee_valid() {
        EmployeeDTO dto = new EmployeeDTO("BADGE123", "Jane", "Smith");
        Employee emp = new Employee();
        emp.setId(1L);
        emp.setBadgeId("BADGE123");
        emp.setFirstName("John");
        emp.setLastName("Doe");
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(emp));
        when(employeeRepository.save(any(Employee.class))).thenReturn(emp);
        Employee result = employeeService.updateEmployee(1L, dto);
        assertEquals("Jane", result.getFirstName());
        assertEquals("Smith", result.getLastName());
    }

    @Test
    void testUpdateEmployee_notFound() {
        EmployeeDTO dto = new EmployeeDTO("BADGE123", "Jane", "Smith");
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> employeeService.updateEmployee(99L, dto));
    }

    @Test
    void testDeleteEmployee_valid() {
        Employee emp = new Employee();
        emp.setId(1L);
        emp.setDeleted(false);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(emp));
        when(employeeRepository.save(any(Employee.class))).thenReturn(emp);
        employeeService.deleteEmployee(1L);
        assertTrue(emp.isDeleted());
    }

    @Test
    void testDeleteEmployee_notFound() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> employeeService.deleteEmployee(99L));
    }

    @Test
    void testGetAllEmployees_valid() {
        Pageable pageable = PageRequest.of(0, 10);
        Employee emp = new Employee();
        emp.setId(1L);
        emp.setDeleted(false);
        Page<Employee> page = new PageImpl<>(Collections.singletonList(emp));
        when(employeeRepository.findByDeletedFalse(pageable)).thenReturn(page);
        Page<Employee> result = employeeService.getAllEmployees(pageable);
        assertEquals(1, result.getTotalElements());
    }

    // DTO and Exception classes for test compilation
    static class EmployeeDTO {
        private String badgeId;
        private String firstName;
        private String lastName;
        public EmployeeDTO(String badgeId, String firstName, String lastName) {
            this.badgeId = badgeId;
            this.firstName = firstName;
            this.lastName = lastName;
        }
        public String getBadgeId() { return badgeId; }
        public String getFirstName() { return firstName; }
        public String getLastName() { return lastName; }
    }
    static class ResourceNotFoundException extends RuntimeException {
        public ResourceNotFoundException(String msg) { super(msg); }
    }
    static class Employee {
        private Long id;
        private String badgeId;
        private String firstName;
        private String lastName;
        private boolean deleted;
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getBadgeId() { return badgeId; }
        public void setBadgeId(String badgeId) { this.badgeId = badgeId; }
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        public boolean isDeleted() { return deleted; }
        public void setDeleted(boolean deleted) { this.deleted = deleted; }
    }
    interface EmployeeRepository {
        boolean existsByBadgeId(String badgeId);
        Optional<Employee> findById(Long id);
        Employee save(Employee employee);
        Page<Employee> findByDeletedFalse(Pageable pageable);
    }
}
