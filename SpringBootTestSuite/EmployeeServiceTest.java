import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.warehouse.ems.domain.dto.EmployeeDTO;
import com.warehouse.ems.domain.entity.Employee;
import com.warehouse.ems.repository.EmployeeRepository;
import com.warehouse.ems.service.impl.EmployeeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

public class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    private Employee employee;
    private EmployeeDTO employeeDTO;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        employee = new Employee();
        employee.setId(UUID.randomUUID());
        employee.setBadgeId("12345");
        employee.setFirstName("John");
        employee.setLastName("Doe");
        employee.setEmail("john.doe@example.com");

        employeeDTO = new EmployeeDTO();
        employeeDTO.setBadgeId("12345");
        employeeDTO.setFirstName("John");
        employeeDTO.setLastName("Doe");
        employeeDTO.setEmail("john.doe@example.com");
    }

    @Test
    public void testCreateEmployee() {
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        EmployeeDTO result = employeeService.createEmployee(employeeDTO);

        assertNotNull(result);
        assertEquals(employee.getBadgeId(), result.getBadgeId());
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    public void testGetEmployeeById() {
        when(employeeRepository.findById(any(UUID.class))).thenReturn(Optional.of(employee));

        EmployeeDTO result = employeeService.getEmployeeById(employee.getId());

        assertNotNull(result);
        assertEquals(employee.getBadgeId(), result.getBadgeId());
        verify(employeeRepository, times(1)).findById(any(UUID.class));
    }

    @Test
    public void testGetAllEmployees() {
        Page<Employee> page = new PageImpl<>(Collections.singletonList(employee));
        when(employeeRepository.findByDeletedFalse(any(PageRequest.class))).thenReturn(page);

        Page<EmployeeDTO> result = employeeService.getAllEmployees(PageRequest.of(0, 10));

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(employeeRepository, times(1)).findByDeletedFalse(any(PageRequest.class));
    }

    @Test
    public void testUpdateEmployee() {
        when(employeeRepository.findById(any(UUID.class))).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        EmployeeDTO result = employeeService.updateEmployee(employee.getId(), employeeDTO);

        assertNotNull(result);
        assertEquals(employee.getBadgeId(), result.getBadgeId());
        verify(employeeRepository, times(1)).findById(any(UUID.class));
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    public void testDeleteEmployee() {
        when(employeeRepository.findById(any(UUID.class))).thenReturn(Optional.of(employee));

        employeeService.deleteEmployee(employee.getId());

        verify(employeeRepository, times(1)).findById(any(UUID.class));
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }
}