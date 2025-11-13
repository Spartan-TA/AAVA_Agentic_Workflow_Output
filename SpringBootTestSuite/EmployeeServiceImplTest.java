import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.warehouse.management.domain.dto.request.EmployeeCreateRequest;
import com.warehouse.management.domain.dto.response.EmployeeResponse;
import com.warehouse.management.domain.entity.Employee;
import com.warehouse.management.repository.EmployeeRepository;
import com.warehouse.management.service.impl.EmployeeServiceImpl;
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
import java.util.Optional;

public class EmployeeServiceImplTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCreateEmployee_ValidInput() {
        // Arrange
        EmployeeCreateRequest request = EmployeeCreateRequest.builder()
                .badgeId("12345")
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .build();

        Employee employee = Employee.builder()
                .id(1L)
                .badgeId("12345")
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .build();

        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        // Act
        EmployeeResponse response = employeeService.createEmployee(request);

        // Assert
        assertNotNull(response);
        assertEquals("12345", response.getBadgeId());
        assertEquals("John", response.getFirstName());
        assertEquals("Doe", response.getLastName());
        assertEquals("john.doe@example.com", response.getEmail());
    }

    @Test
    public void testGetEmployeeById_ValidId() {
        // Arrange
        Employee employee = Employee.builder()
                .id(1L)
                .badgeId("12345")
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .build();

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        // Act
        EmployeeResponse response = employeeService.getEmployeeById(1L);

        // Assert
        assertNotNull(response);
        assertEquals("12345", response.getBadgeId());
        assertEquals("John", response.getFirstName());
        assertEquals("Doe", response.getLastName());
        assertEquals("john.doe@example.com", response.getEmail());
    }

    @Test
    public void testGetAllEmployees_NoFilter() {
        // Arrange
        Employee employee = Employee.builder()
                .id(1L)
                .badgeId("12345")
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .build();

        Page<Employee> page = new PageImpl<>(Collections.singletonList(employee));
        Pageable pageable = PageRequest.of(0, 10);

        when(employeeRepository.findAll(pageable)).thenReturn(page);

        // Act
        Page<EmployeeResponse> responsePage = employeeService.getAllEmployees(pageable, null);

        // Assert
        assertNotNull(responsePage);
        assertEquals(1, responsePage.getTotalElements());
        assertEquals("12345", responsePage.getContent().get(0).getBadgeId());
    }

    @Test
    public void testUpdateEmployee_ValidId() {
        // Arrange
        Employee employee = Employee.builder()
                .id(1L)
                .badgeId("12345")
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .build();

        EmployeeCreateRequest request = EmployeeCreateRequest.builder()
                .badgeId("54321")
                .firstName("Jane")
                .lastName("Smith")
                .email("jane.smith@example.com")
                .build();

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        // Act
        EmployeeResponse response = employeeService.updateEmployee(1L, request);

        // Assert
        assertNotNull(response);
        assertEquals("54321", response.getBadgeId());
        assertEquals("Jane", response.getFirstName());
        assertEquals("Smith", response.getLastName());
        assertEquals("jane.smith@example.com", response.getEmail());
    }

    @Test
    public void testSoftDeleteEmployee_ValidId() {
        // Arrange
        Employee employee = Employee.builder()
                .id(1L)
                .badgeId("12345")
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .deleted(false)
                .build();

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        // Act
        employeeService.softDeleteEmployee(1L);

        // Assert
        assertTrue(employee.getDeleted());
        verify(employeeRepository, times(1)).save(employee);
    }
}