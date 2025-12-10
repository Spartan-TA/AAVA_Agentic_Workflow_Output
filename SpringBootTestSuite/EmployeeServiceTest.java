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

import javax.validation.ValidationException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmployeeServiceTest {
    @Mock
    private EmployeeRepository employeeRepository;
    @InjectMocks
    private EmployeeServiceImpl employeeService;

    private EmployeeDto validEmployeeDto;
    private Employee validEmployee;

    @BeforeEach
    void setUp() {
        validEmployeeDto = new EmployeeDto();
        validEmployeeDto.setName("John Doe");
        validEmployeeDto.setBadgeId("EMP001");
        validEmployeeDto.setRole("Worker");
        validEmployeeDto.setDepartment("Warehouse");

        validEmployee = new Employee();
        validEmployee.setId(1L);
        validEmployee.setName("John Doe");
        validEmployee.setBadgeId("EMP001");
        validEmployee.setRole("Worker");
        validEmployee.setDepartment("Warehouse");
        validEmployee.setDeleted(false);
    }

    @Test
    void testCreateEmployee_ValidInput() {
        when(employeeRepository.findByBadgeId(anyString())).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);
        Employee result = employeeService.create(validEmployeeDto);
        assertNotNull(result);
        assertEquals("John Doe", result.getName());
        assertEquals("EMP001", result.getBadgeId());
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    void testCreateEmployee_NullName() {
        validEmployeeDto.setName(null);
        assertThrows(ValidationException.class, () -> employeeService.create(validEmployeeDto));
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void testCreateEmployee_EmptyBadgeId() {
        validEmployeeDto.setBadgeId("");
        assertThrows(ValidationException.class, () -> employeeService.create(validEmployeeDto));
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void testCreateEmployee_DuplicateBadgeId() {
        when(employeeRepository.findByBadgeId("EMP001")).thenReturn(Optional.of(validEmployee));
        assertThrows(ValidationException.class, () -> employeeService.create(validEmployeeDto));
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void testCreateEmployee_NullDto() {
        assertThrows(IllegalArgumentException.class, () -> employeeService.create(null));
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void testUpdateEmployee_ValidInput() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);
        EmployeeDto updateDto = new EmployeeDto();
        updateDto.setName("Jane Smith");
        updateDto.setBadgeId("EMP002");
        updateDto.setRole("Supervisor");
        updateDto.setDepartment("Logistics");
        Employee result = employeeService.update(1L, updateDto);
        assertNotNull(result);
        assertEquals("Jane Smith", result.getName());
        assertEquals("EMP002", result.getBadgeId());
    }

    @Test
    void testUpdateEmployee_NonExistentId() {
        when(employeeRepository.findById(2L)).thenReturn(Optional.empty());
        EmployeeDto updateDto = new EmployeeDto();
        updateDto.setName("Jane Smith");
        updateDto.setBadgeId("EMP002");
        assertThrows(ResourceNotFoundException.class, () -> employeeService.update(2L, updateDto));
    }

    @Test
    void testUpdateEmployee_NullFields() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        EmployeeDto updateDto = new EmployeeDto();
        updateDto.setName(null);
        updateDto.setBadgeId(null);
        updateDto.setRole(null);
        updateDto.setDepartment(null);
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);
        Employee result = employeeService.update(1L, updateDto);
        assertNotNull(result);
        assertEquals("John Doe", result.getName());
        assertEquals("EMP001", result.getBadgeId());
    }

    @Test
    void testDeleteEmployee_ValidId() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        doNothing().when(employeeRepository).softDeleteById(1L);
        assertDoesNotThrow(() -> employeeService.delete(1L));
        verify(employeeRepository, times(1)).softDeleteById(1L);
    }

    @Test
    void testDeleteEmployee_NonExistentId() {
        when(employeeRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> employeeService.delete(2L));
    }

    @Test
    void testGetEmployee_ValidId() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        Employee result = employeeService.get(1L);
        assertNotNull(result);
        assertEquals("John Doe", result.getName());
    }

    @Test
    void testGetEmployee_NonExistentId() {
        when(employeeRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> employeeService.get(2L));
    }

    @Test
    void testListEmployees_WithPagination() {
        List<Employee> employees = Arrays.asList(validEmployee);
        Page<Employee> page = new PageImpl<>(employees);
        Pageable pageable = PageRequest.of(0, 10);
        when(employeeRepository.findAll(pageable)).thenReturn(page);
        Page<Employee> result = employeeService.list(pageable);
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void testListEmployees_EmptyResult() {
        Page<Employee> page = new PageImpl<>(Collections.emptyList());
        Pageable pageable = PageRequest.of(0, 10);
        when(employeeRepository.findAll(pageable)).thenReturn(page);
        Page<Employee> result = employeeService.list(pageable);
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
    }
}
