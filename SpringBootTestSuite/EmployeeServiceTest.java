import com.warehouse.ems.employee.dto.EmployeeCreateDto;
import com.warehouse.ems.employee.dto.EmployeeDto;
import com.warehouse.ems.employee.dto.EmployeeUpdateDto;
import com.warehouse.ems.employee.entity.Employee;
import com.warehouse.ems.employee.repository.EmployeeRepository;
import com.warehouse.ems.employee.service.EmployeeService;
import com.warehouse.ems.common.exception.ResourceNotFoundException;
import com.warehouse.ems.common.exception.DuplicateResourceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeService employeeService;

    private Employee employee;
    private EmployeeCreateDto employeeCreateDto;
    private EmployeeUpdateDto employeeUpdateDto;

    @BeforeEach
    void setUp() {
        employee = new Employee();
        employee.setId(1L);
        employee.setBadgeId("12345");
        employee.setName("John Doe");
        employee.setEmail("john.doe@example.com");
        employee.setPhone("123-456-7890");
        employee.setRole("Manager");
        employee.setDepartment("Operations");
        employee.setHireDate(LocalDate.of(2020, 1, 1));
        employee.setStatus("Active");

        employeeCreateDto = new EmployeeCreateDto();
        employeeCreateDto.setBadgeId("12345");
        employeeCreateDto.setName("John Doe");
        employeeCreateDto.setEmail("john.doe@example.com");
        employeeCreateDto.setPhone("123-456-7890");
        employeeCreateDto.setRole("Manager");
        employeeCreateDto.setDepartment("Operations");
        employeeCreateDto.setHireDate(LocalDate.of(2020, 1, 1));
        employeeCreateDto.setStatus("Active");

        employeeUpdateDto = new EmployeeUpdateDto();
        employeeUpdateDto.setName("John Updated");
        employeeUpdateDto.setEmail("john.updated@example.com");
    }

    @Test
    void testCreateEmployee() {
        when(employeeRepository.existsByBadgeId(employeeCreateDto.getBadgeId())).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        EmployeeDto result = employeeService.createEmployee(employeeCreateDto);

        assertNotNull(result);
        assertEquals(employee.getBadgeId(), result.getBadgeId());
        assertEquals(employee.getName(), result.getName());
    }

    @Test
    void testCreateEmployeeDuplicateBadgeId() {
        when(employeeRepository.existsByBadgeId(employeeCreateDto.getBadgeId())).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> employeeService.createEmployee(employeeCreateDto));
    }

    @Test
    void testGetEmployeeById() {
        when(employeeRepository.findById(employee.getId())).thenReturn(Optional.of(employee));

        EmployeeDto result = employeeService.getEmployeeById(employee.getId());

        assertNotNull(result);
        assertEquals(employee.getId(), result.getId());
    }

    @Test
    void testGetEmployeeByIdNotFound() {
        when(employeeRepository.findById(employee.getId())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> employeeService.getEmployeeById(employee.getId()));
    }

    @Test
    void testUpdateEmployee() {
        when(employeeRepository.findById(employee.getId())).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        EmployeeDto result = employeeService.updateEmployee(employee.getId(), employeeUpdateDto);

        assertNotNull(result);
        assertEquals(employeeUpdateDto.getName(), result.getName());
        assertEquals(employeeUpdateDto.getEmail(), result.getEmail());
    }

    @Test
    void testUpdateEmployeeNotFound() {
        when(employeeRepository.findById(employee.getId())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> employeeService.updateEmployee(employee.getId(), employeeUpdateDto));
    }

    @Test
    void testDeleteEmployee() {
        when(employeeRepository.findById(employee.getId())).thenReturn(Optional.of(employee));

        employeeService.deleteEmployee(employee.getId());

        verify(employeeRepository, times(1)).save(employee);
        assertTrue(employee.getDeleted());
    }

    @Test
    void testDeleteEmployeeNotFound() {
        when(employeeRepository.findById(employee.getId())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> employeeService.deleteEmployee(employee.getId()));
    }
}
