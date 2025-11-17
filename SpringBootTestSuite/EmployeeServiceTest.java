import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateEmployeeValidInput() {
        EmployeeRequestDto requestDto = new EmployeeRequestDto();
        requestDto.setBadgeId("B123");
        requestDto.setFirstName("John");
        requestDto.setLastName("Doe");
        requestDto.setEmail("john.doe@example.com");
        requestDto.setRole("Worker");
        requestDto.setDepartment("Warehouse");
        requestDto.setShiftGroup("Morning");
        requestDto.setHireDate(LocalDate.now());
        requestDto.setStatus("ACTIVE");

        Employee employee = Employee.builder()
                .badgeId(requestDto.getBadgeId())
                .firstName(requestDto.getFirstName())
                .lastName(requestDto.getLastName())
                .email(requestDto.getEmail())
                .role(requestDto.getRole())
                .department(requestDto.getDepartment())
                .shiftGroup(requestDto.getShiftGroup())
                .hireDate(requestDto.getHireDate())
                .status(requestDto.getStatus())
                .build();

        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        EmployeeResponseDto responseDto = employeeService.createEmployee(requestDto);

        assertNotNull(responseDto);
        assertEquals("B123", responseDto.getBadgeId());
        assertEquals("John", responseDto.getFirstName());
        assertEquals("Doe", responseDto.getLastName());
        assertEquals("john.doe@example.com", responseDto.getEmail());
    }

    @Test
    void testCreateEmployeeNullInput() {
        assertThrows(IllegalArgumentException.class, () -> employeeService.createEmployee(null));
    }

    @Test
    void testUpdateEmployeeValidInput() {
        Long id = 1L;
        EmployeeRequestDto requestDto = new EmployeeRequestDto();
        requestDto.setFirstName("Jane");
        requestDto.setLastName("Smith");
        requestDto.setEmail("jane.smith@example.com");
        requestDto.setRole("Supervisor");
        requestDto.setDepartment("Operations");
        requestDto.setShiftGroup("Evening");
        requestDto.setHireDate(LocalDate.now());
        requestDto.setStatus("ACTIVE");

        Employee existingEmployee = Employee.builder()
                .id(id)
                .badgeId("B123")
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .role("Worker")
                .department("Warehouse")
                .shiftGroup("Morning")
                .hireDate(LocalDate.now())
                .status("ACTIVE")
                .build();

        when(employeeRepository.findById(id)).thenReturn(Optional.of(existingEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(existingEmployee);

        EmployeeResponseDto responseDto = employeeService.updateEmployee(id, requestDto);

        assertNotNull(responseDto);
        assertEquals("Jane", responseDto.getFirstName());
        assertEquals("Smith", responseDto.getLastName());
        assertEquals("jane.smith@example.com", responseDto.getEmail());
    }

    @Test
    void testUpdateEmployeeNotFound() {
        Long id = 1L;
        EmployeeRequestDto requestDto = new EmployeeRequestDto();
        when(employeeRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> employeeService.updateEmployee(id, requestDto));
    }

    @Test
    void testDeleteEmployeeValidInput() {
        Long id = 1L;
        Employee existingEmployee = Employee.builder()
                .id(id)
                .badgeId("B123")
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .role("Worker")
                .department("Warehouse")
                .shiftGroup("Morning")
                .hireDate(LocalDate.now())
                .status("ACTIVE")
                .build();

        when(employeeRepository.findById(id)).thenReturn(Optional.of(existingEmployee));

        employeeService.deleteEmployee(id);

        verify(employeeRepository, times(1)).save(existingEmployee);
        assertEquals("DELETED", existingEmployee.getStatus());
    }

    @Test
    void testDeleteEmployeeNotFound() {
        Long id = 1L;
        when(employeeRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> employeeService.deleteEmployee(id));
    }

    @Test
    void testGetEmployeeByIdValidInput() {
        Long id = 1L;
        Employee existingEmployee = Employee.builder()
                .id(id)
                .badgeId("B123")
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .role("Worker")
                .department("Warehouse")
                .shiftGroup("Morning")
                .hireDate(LocalDate.now())
                .status("ACTIVE")
                .build();

        when(employeeRepository.findById(id)).thenReturn(Optional.of(existingEmployee));

        Optional<EmployeeResponseDto> responseDto = employeeService.getEmployeeById(id);

        assertTrue(responseDto.isPresent());
        assertEquals("B123", responseDto.get().getBadgeId());
    }

    @Test
    void testGetEmployeesPagination() {
        Pageable pageable = PageRequest.of(0, 10);
        Employee existingEmployee = Employee.builder()
                .id(1L)
                .badgeId("B123")
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .role("Worker")
                .department("Warehouse")
                .shiftGroup("Morning")
                .hireDate(LocalDate.now())
                .status("ACTIVE")
                .build();

        Page<Employee> employeePage = new PageImpl<>(Collections.singletonList(existingEmployee));
        when(employeeRepository.findAll(pageable)).thenReturn(employeePage);

        Page<EmployeeResponseDto> responsePage = employeeService.getEmployees(pageable, null);

        assertNotNull(responsePage);
        assertEquals(1, responsePage.getTotalElements());
        assertEquals("B123", responsePage.getContent().get(0).getBadgeId());
    }
}