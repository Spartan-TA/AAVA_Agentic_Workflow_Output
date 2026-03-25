package SpringBootTestSuite;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

// Assume these imports exist
import com.example.ems.dto.EmployeeDTO;
import com.example.ems.entity.Employee;
import com.example.ems.exception.EntityNotFoundException;
import com.example.ems.repository.EmployeeRepository;
import com.example.ems.service.impl.EmployeeServiceImpl;

@ExtendWith(MockitoExtension.class)
public class EmployeeServiceTest {
    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    private EmployeeDTO validDto;
    private Employee employee;

    @BeforeEach
    public void setUp() {
        validDto = new EmployeeDTO();
        validDto.setName("John Doe");
        validDto.setBadgeId("EMP001");
        validDto.setRole("WORKER");
        validDto.setDepartment("Logistics");
        validDto.setShiftGroup("A");
        validDto.setHireDate(LocalDate.now());
        validDto.setStatus("ACTIVE");

        employee = new Employee();
        employee.setId(1L);
        employee.setName("John Doe");
        employee.setBadgeId("EMP001");
        employee.setRole("WORKER");
        employee.setDepartment("Logistics");
        employee.setShiftGroup("A");
        employee.setHireDate(LocalDate.now());
        employee.setStatus("ACTIVE");
        employee.setDeleted(false);
    }

    @Test
    public void testCreateEmployee_WithValidData_ReturnsCreatedEmployee() {
        when(employeeRepository.existsByBadgeIdAndDeletedFalse("EMP001")).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        EmployeeDTO result = employeeService.createEmployee(validDto);

        assertNotNull(result);
        assertEquals("John Doe", result.getName());
        assertEquals("EMP001", result.getBadgeId());
        verify(employeeRepository).save(any(Employee.class));
    }

    @Test
    public void testCreateEmployee_WithDuplicateBadgeId_ThrowsIllegalArgumentException() {
        when(employeeRepository.existsByBadgeIdAndDeletedFalse("EMP001")).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> employeeService.createEmployee(validDto));
    }

    @Test
    public void testGetEmployee_WithValidId_ReturnsEmployee() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        EmployeeDTO result = employeeService.getEmployee(1L);
        assertNotNull(result);
        assertEquals("EMP001", result.getBadgeId());
    }

    @Test
    public void testGetEmployee_WithInvalidId_ThrowsEntityNotFoundException() {
        when(employeeRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> employeeService.getEmployee(2L));
    }

    @Test
    public void testListEmployees_ReturnsPagedEmployees() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Employee> employees = Arrays.asList(employee);
        Page<Employee> page = new PageImpl<>(employees);
        when(employeeRepository.findAllByDeletedFalse(pageable)).thenReturn(page);
        Page<EmployeeDTO> result = employeeService.listEmployees(pageable);
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    public void testListEmployeesByStatus_ReturnsPagedEmployees() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Employee> employees = Arrays.asList(employee);
        Page<Employee> page = new PageImpl<>(employees);
        when(employeeRepository.findAllByDeletedFalseAndStatus("ACTIVE", pageable)).thenReturn(page);
        Page<EmployeeDTO> result = employeeService.listEmployeesByStatus("ACTIVE", pageable);
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    public void testUpdateEmployee_WithValidId_UpdatesEmployee() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);
        EmployeeDTO updatedDto = new EmployeeDTO();
        updatedDto.setName("Jane Smith");
        updatedDto.setBadgeId("EMP001");
        updatedDto.setRole("SUPERVISOR");
        EmployeeDTO result = employeeService.updateEmployee(1L, updatedDto);
        assertNotNull(result);
        assertEquals("Jane Smith", result.getName());
        assertEquals("SUPERVISOR", result.getRole());
    }

    @Test
    public void testUpdateEmployee_WithInvalidId_ThrowsEntityNotFoundException() {
        when(employeeRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> employeeService.updateEmployee(2L, validDto));
    }

    @Test
    public void testDeleteEmployee_SetsDeletedAndInactive() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        employeeService.deleteEmployee(1L);
        assertTrue(employee.isDeleted());
        assertEquals("INACTIVE", employee.getStatus());
        verify(employeeRepository).save(employee);
    }

    @Test
    public void testDeleteEmployee_WithInvalidId_ThrowsEntityNotFoundException() {
        when(employeeRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> employeeService.deleteEmployee(2L));
    }

    @Test
    public void testCreateEmployee_WithNullDto_ThrowsException() {
        assertThrows(NullPointerException.class, () -> employeeService.createEmployee(null));
    }

    @Test
    public void testCreateEmployee_WithEmptyName_ThrowsException() {
        EmployeeDTO dto = new EmployeeDTO();
        dto.setName("");
        dto.setBadgeId("EMP002");
        dto.setRole("WORKER");
        when(employeeRepository.existsByBadgeIdAndDeletedFalse("EMP002")).thenReturn(false);
        assertThrows(IllegalArgumentException.class, () -> employeeService.createEmployee(dto));
    }
}
