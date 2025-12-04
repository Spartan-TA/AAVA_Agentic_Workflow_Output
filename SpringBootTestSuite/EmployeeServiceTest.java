package SpringBootTestSuite;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.data.domain.*;

@ExtendWith(MockitoExtension.class)
public class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeService employeeService;

    private EmployeeDTO validEmployeeDTO;
    private Employee validEmployee;

    @BeforeEach
    public void setUp() {
        validEmployeeDTO = new EmployeeDTO();
        validEmployeeDTO.setName("John Doe");
        validEmployeeDTO.setBadgeId("EMP001");
        validEmployeeDTO.setRole(Role.WORKER);
        validEmployeeDTO.setDepartment("Logistics");
        validEmployeeDTO.setHireDate(LocalDate.of(2023, 1, 1));
        validEmployeeDTO.setStatus(Status.ACTIVE);

        validEmployee = new Employee();
        validEmployee.setId(1L);
        validEmployee.setName("John Doe");
        validEmployee.setBadgeId("EMP001");
        validEmployee.setRole(Role.WORKER);
        validEmployee.setDepartment("Logistics");
        validEmployee.setHireDate(LocalDate.of(2023, 1, 1));
        validEmployee.setStatus(Status.ACTIVE);
        validEmployee.setDeleted(false);
    }

    @Test
    public void testCreateEmployee_ValidInput_Success() {
        when(employeeRepository.findByBadgeIdAndDeletedFalse(anyString())).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);

        EmployeeDTO result = employeeService.createEmployee(validEmployeeDTO);

        assertNotNull(result);
        assertEquals("John Doe", result.getName());
        assertEquals("EMP001", result.getBadgeId());
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    public void testCreateEmployee_DuplicateBadgeId_ThrowsException() {
        when(employeeRepository.findByBadgeIdAndDeletedFalse(anyString())).thenReturn(Optional.of(validEmployee));

        assertThrows(IllegalArgumentException.class, () -> employeeService.createEmployee(validEmployeeDTO));
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    public void testCreateEmployee_NullName_ThrowsException() {
        validEmployeeDTO.setName(null);

        assertThrows(Exception.class, () -> employeeService.createEmployee(validEmployeeDTO));
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    public void testCreateEmployee_EmptyBadgeId_ThrowsException() {
        validEmployeeDTO.setBadgeId("");

        assertThrows(Exception.class, () -> employeeService.createEmployee(validEmployeeDTO));
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    public void testGetEmployeeById_ValidId_ReturnsEmployee() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));

        EmployeeDTO result = employeeService.getEmployeeById(1L);

        assertNotNull(result);
        assertEquals("John Doe", result.getName());
        assertEquals("EMP001", result.getBadgeId());
    }

    @Test
    public void testGetEmployeeById_InvalidId_ThrowsEntityNotFoundException() {
        when(employeeRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> employeeService.getEmployeeById(2L));
    }

    @Test
    public void testGetEmployeeById_DeletedEmployee_ThrowsEntityNotFoundException() {
        validEmployee.setDeleted(true);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));

        assertThrows(EntityNotFoundException.class, () -> employeeService.getEmployeeById(1L));
    }

    @Test
    public void testGetAllEmployees_ReturnsPagedResults() {
        Page<Employee> page = new PageImpl<>(Collections.singletonList(validEmployee));
        when(employeeRepository.findAllByDeletedFalse(any(Pageable.class))).thenReturn(page);

        Page<EmployeeDTO> result = employeeService.getAllEmployees(PageRequest.of(0, 10));

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    public void testUpdateEmployee_ValidInput_Success() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);

        validEmployeeDTO.setName("Jane Doe");
        validEmployeeDTO.setRole(Role.SUPERVISOR);
        validEmployeeDTO.setDepartment("Packing");

        EmployeeDTO result = employeeService.updateEmployee(1L, validEmployeeDTO);

        assertNotNull(result);
        assertEquals("Jane Doe", result.getName());
        assertEquals(Role.SUPERVISOR, result.getRole());
        assertEquals("Packing", result.getDepartment());
    }

    @Test
    public void testUpdateEmployee_InvalidId_ThrowsException() {
        when(employeeRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> employeeService.updateEmployee(2L, validEmployeeDTO));
    }

    @Test
    public void testSoftDeleteEmployee_ValidId_MarksAsDeleted() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);

        employeeService.softDeleteEmployee(1L);

        assertTrue(validEmployee.isDeleted());
        verify(employeeRepository, times(1)).save(validEmployee);
    }

    @Test
    public void testSoftDeleteEmployee_InvalidId_ThrowsException() {
        when(employeeRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> employeeService.softDeleteEmployee(2L));
    }
}