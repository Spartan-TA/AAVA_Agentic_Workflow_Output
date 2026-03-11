package SpringBootTestSuite;

import com.example.ems.entity.Employee;
import com.example.ems.service.EmployeeService;
import com.example.ems.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class EmployeeServiceTest {

    @Autowired
    private EmployeeService employeeService;

    @MockBean
    private EmployeeRepository employeeRepository;

    private Employee employee;

    @BeforeEach
    void setUp() {
        employee = new Employee();
        employee.setId(1L);
        employee.setBadgeId("BADGE123");
        employee.setName("John Doe");
        employee.setRole("WORKER");
        employee.setDepartment("Logistics");
        employee.setShiftGroup("A");
        employee.setHireDate(LocalDate.now());
        employee.setStatus("ACTIVE");
    }

    @Test
    void testGetAllEmployeesReturnsList() {
        Mockito.when(employeeRepository.findAll()).thenReturn(Arrays.asList(employee));
        assertEquals(1, employeeService.getAllEmployees().size());
    }

    @Test
    void testGetEmployeeByIdSuccess() {
        Mockito.when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        Employee found = employeeService.getEmployeeById(1L);
        assertEquals("John Doe", found.getName());
    }

    @Test
    void testGetEmployeeByIdNotFoundThrowsException() {
        Mockito.when(employeeRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> employeeService.getEmployeeById(2L));
    }

    @Test
    void testGetEmployeeByBadgeIdSuccess() {
        Mockito.when(employeeRepository.findByBadgeId("BADGE123")).thenReturn(Optional.of(employee));
        Employee found = employeeService.getEmployeeByBadgeId("BADGE123");
        assertEquals("John Doe", found.getName());
    }

    @Test
    void testGetEmployeeByBadgeIdDuplicateThrowsException() {
        Mockito.when(employeeRepository.findByBadgeId("BADGE123")).thenReturn(Optional.of(employee));
        Employee duplicate = new Employee();
        duplicate.setBadgeId("BADGE123");
        Mockito.when(employeeRepository.save(duplicate)).thenThrow(new RuntimeException("Duplicate badgeId"));
        assertThrows(RuntimeException.class, () -> employeeService.createEmployee(duplicate));
    }

    @Test
    void testCreateEmployeeSuccess() {
        Mockito.when(employeeRepository.save(employee)).thenReturn(employee);
        Employee created = employeeService.createEmployee(employee);
        assertEquals("BADGE123", created.getBadgeId());
    }

    @Test
    void testCreateEmployeeNullFieldsThrowsException() {
        Employee invalid = new Employee();
        assertThrows(RuntimeException.class, () -> employeeService.createEmployee(invalid));
    }

    @Test
    void testUpdateEmployeeSuccess() {
        Mockito.when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        employee.setName("Jane Doe");
        Mockito.when(employeeRepository.save(employee)).thenReturn(employee);
        Employee updated = employeeService.updateEmployee(1L, employee);
        assertEquals("Jane Doe", updated.getName());
    }

    @Test
    void testUpdateEmployeeNotFoundThrowsException() {
        Mockito.when(employeeRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> employeeService.updateEmployee(2L, employee));
    }

    @Test
    void testDeleteEmployeeSuccess() {
        Mockito.when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        assertDoesNotThrow(() -> employeeService.deleteEmployee(1L));
    }

    @Test
    void testDeleteEmployeeNotFoundThrowsException() {
        Mockito.when(employeeRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> employeeService.deleteEmployee(2L));
    }

    @Test
    void testGetEmployeeByBadgeIdNullThrowsException() {
        assertThrows(RuntimeException.class, () -> employeeService.getEmployeeByBadgeId(null));
    }

    @Test
    void testCreateEmployeeInvalidRoleThrowsException() {
        Employee invalid = new Employee();
        invalid.setBadgeId("BADGE999");
        invalid.setRole("INVALID_ROLE");
        assertThrows(RuntimeException.class, () -> employeeService.createEmployee(invalid));
    }
}
