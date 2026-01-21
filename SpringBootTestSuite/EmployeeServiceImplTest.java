package SpringBootTestSuite;

import com.example.warehouse.Employee;
import com.example.warehouse.EmployeeDTO;
import com.example.warehouse.EmployeeRepository;
import com.example.warehouse.EmployeeMapper;
import com.example.warehouse.EmployeeServiceImpl;
import com.example.warehouse.exception.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmployeeServiceImplTest {
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private EmployeeMapper employeeMapper;
    @InjectMocks
    private EmployeeServiceImpl employeeService;

    private Employee employee;
    private EmployeeDTO employeeDTO;
    private UUID employeeId;
    private UUID tenantId;

    @BeforeEach
    void setUp() {
        employeeId = UUID.randomUUID();
        tenantId = UUID.randomUUID();
        employee = Employee.builder()
                .id(employeeId)
                .tenantId(tenantId)
                .name("John Doe")
                .badgeId("BADGE123")
                .email("john.doe@example.com")
                .phone("1234567890")
                .role("WORKER")
                .department("Logistics")
                .shiftGroup("A")
                .hireDate(LocalDate.now().minusYears(1))
                .terminationDate(null)
                .status("ACTIVE")
                .deleted(false)
                .createdAt(LocalDateTime.now().minusYears(1))
                .updatedAt(LocalDateTime.now())
                .build();
        employeeDTO = EmployeeDTO.builder()
                .name("John Doe")
                .badgeId("BADGE123")
                .email("john.doe@example.com")
                .phone("1234567890")
                .role("WORKER")
                .department("Logistics")
                .shiftGroup("A")
                .hireDate(LocalDate.now().minusYears(1))
                .terminationDate(null)
                .status("ACTIVE")
                .build();
    }

    @Test
    void createEmployee_Success() {
        when(employeeRepository.existsByBadgeIdAndTenantId(anyString(), any(UUID.class))).thenReturn(false);
        when(employeeMapper.toEntity(any(EmployeeDTO.class))).thenReturn(employee);
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);
        when(employeeMapper.toDTO(any(Employee.class))).thenReturn(employeeDTO);

        EmployeeDTO result = employeeService.createEmployee(employeeDTO);
        assertNotNull(result);
        assertEquals(employeeDTO.getName(), result.getName());
        verify(employeeRepository).save(any(Employee.class));
    }

    @Test
    void createEmployee_DuplicateBadgeId_ThrowsException() {
        when(employeeRepository.existsByBadgeIdAndTenantId(anyString(), any(UUID.class))).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> employeeService.createEmployee(employeeDTO));
    }

    @Test
    void getAllEmployees_NoFilters_ReturnsPage() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Employee> employees = Collections.singletonList(employee);
        Page<Employee> page = new PageImpl<>(employees);
        when(employeeRepository.findAllByTenantIdAndFilters(any(UUID.class), any(), any(), any(), any(Pageable.class))).thenReturn(page);
        when(employeeMapper.toDTO(any(Employee.class))).thenReturn(employeeDTO);

        Page<EmployeeDTO> result = employeeService.getAllEmployees(tenantId, null, null, null, pageable);
        assertEquals(1, result.getTotalElements());
        verify(employeeRepository).findAllByTenantIdAndFilters(any(), any(), any(), any(), any());
    }

    @Test
    void getEmployeeById_Exists_ReturnsEmployee() {
        when(employeeRepository.findByIdAndTenantIdAndDeletedFalse(employeeId, tenantId)).thenReturn(Optional.of(employee));
        when(employeeMapper.toDTO(employee)).thenReturn(employeeDTO);
        EmployeeDTO result = employeeService.getEmployeeById(employeeId, tenantId);
        assertNotNull(result);
        assertEquals(employeeDTO.getName(), result.getName());
    }

    @Test
    void getEmployeeById_NotFound_ThrowsException() {
        when(employeeRepository.findByIdAndTenantIdAndDeletedFalse(employeeId, tenantId)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> employeeService.getEmployeeById(employeeId, tenantId));
    }

    @Test
    void getEmployeeByBadgeId_Exists_ReturnsEmployee() {
        when(employeeRepository.findByBadgeIdAndTenantIdAndDeletedFalse("BADGE123", tenantId)).thenReturn(Optional.of(employee));
        when(employeeMapper.toDTO(employee)).thenReturn(employeeDTO);
        EmployeeDTO result = employeeService.getEmployeeByBadgeId("BADGE123", tenantId);
        assertNotNull(result);
        assertEquals(employeeDTO.getBadgeId(), result.getBadgeId());
    }

    @Test
    void getEmployeeByBadgeId_NotFound_ThrowsException() {
        when(employeeRepository.findByBadgeIdAndTenantIdAndDeletedFalse("BADGE123", tenantId)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> employeeService.getEmployeeByBadgeId("BADGE123", tenantId));
    }

    @Test
    void updateEmployee_Success() {
        when(employeeRepository.findByIdAndTenantIdAndDeletedFalse(employeeId, tenantId)).thenReturn(Optional.of(employee));
        when(employeeRepository.existsByBadgeIdAndTenantId(anyString(), any(UUID.class))).thenReturn(false);
        when(employeeMapper.updateEntityFromDTO(any(EmployeeDTO.class), any(Employee.class))).thenReturn(employee);
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);
        when(employeeMapper.toDTO(any(Employee.class))).thenReturn(employeeDTO);
        EmployeeDTO result = employeeService.updateEmployee(employeeId, employeeDTO, tenantId);
        assertNotNull(result);
        verify(employeeRepository).save(any(Employee.class));
    }

    @Test
    void updateEmployee_EmployeeNotFound_ThrowsException() {
        when(employeeRepository.findByIdAndTenantIdAndDeletedFalse(employeeId, tenantId)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> employeeService.updateEmployee(employeeId, employeeDTO, tenantId));
    }

    @Test
    void updateEmployee_DuplicateBadgeId_ThrowsException() {
        when(employeeRepository.findByIdAndTenantIdAndDeletedFalse(employeeId, tenantId)).thenReturn(Optional.of(employee));
        when(employeeRepository.existsByBadgeIdAndTenantId(anyString(), any(UUID.class))).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> employeeService.updateEmployee(employeeId, employeeDTO, tenantId));
    }

    @Test
    void softDeleteEmployee_Success() {
        when(employeeRepository.findByIdAndTenantIdAndDeletedFalse(employeeId, tenantId)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);
        employeeService.softDeleteEmployee(employeeId, tenantId);
        assertTrue(employee.getDeleted());
        verify(employeeRepository).save(employee);
    }

    @Test
    void softDeleteEmployee_NotFound_ThrowsException() {
        when(employeeRepository.findByIdAndTenantIdAndDeletedFalse(employeeId, tenantId)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> employeeService.softDeleteEmployee(employeeId, tenantId));
    }

    @Test
    void hardDeleteEmployee_Success() {
        when(employeeRepository.findByIdAndTenantId(employeeId, tenantId)).thenReturn(Optional.of(employee));
        doNothing().when(employeeRepository).delete(employee);
        employeeService.hardDeleteEmployee(employeeId, tenantId);
        verify(employeeRepository).delete(employee);
    }

    @Test
    void hardDeleteEmployee_NotFound_ThrowsException() {
        when(employeeRepository.findByIdAndTenantId(employeeId, tenantId)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> employeeService.hardDeleteEmployee(employeeId, tenantId));
    }

    @Test
    void restoreEmployee_Success() {
        employee.setDeleted(true);
        when(employeeRepository.findByIdAndTenantId(employeeId, tenantId)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);
        employeeService.restoreEmployee(employeeId, tenantId);
        assertFalse(employee.getDeleted());
        verify(employeeRepository).save(employee);
    }

    @Test
    void restoreEmployee_NotFound_ThrowsException() {
        when(employeeRepository.findByIdAndTenantId(employeeId, tenantId)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> employeeService.restoreEmployee(employeeId, tenantId));
    }

    @Test
    void badgeIdExists_True() {
        when(employeeRepository.existsByBadgeIdAndTenantId("BADGE123", tenantId)).thenReturn(true);
        assertTrue(employeeService.badgeIdExists("BADGE123", tenantId));
    }

    @Test
    void badgeIdExists_False() {
        when(employeeRepository.existsByBadgeIdAndTenantId("BADGE123", tenantId)).thenReturn(false);
        assertFalse(employeeService.badgeIdExists("BADGE123", tenantId));
    }

    @Test
    void countActiveEmployees_ReturnsCount() {
        when(employeeRepository.countByTenantIdAndStatusAndDeletedFalse(tenantId, "ACTIVE")).thenReturn(5L);
        long count = employeeService.countActiveEmployees(tenantId);
        assertEquals(5L, count);
    }

    // Boundary and edge cases
    @Test
    void createEmployee_EmptyName_ThrowsValidationException() {
        employeeDTO.setName("");
        // Assume validation is handled elsewhere (e.g., controller or service layer with @Valid)
        // Here, just check that repository is not called
        assertThrows(IllegalArgumentException.class, () -> employeeService.createEmployee(employeeDTO));
        verify(employeeRepository, never()).save(any());
    }

    @Test
    void createEmployee_NullBadgeId_ThrowsValidationException() {
        employeeDTO.setBadgeId(null);
        assertThrows(IllegalArgumentException.class, () -> employeeService.createEmployee(employeeDTO));
        verify(employeeRepository, never()).save(any());
    }

    @Test
    void createEmployee_InvalidEmail_ThrowsValidationException() {
        employeeDTO.setEmail("not-an-email");
        assertThrows(IllegalArgumentException.class, () -> employeeService.createEmployee(employeeDTO));
        verify(employeeRepository, never()).save(any());
    }

    @Test
    void createEmployee_PastHireDate_Success() {
        employeeDTO.setHireDate(LocalDate.now().minusYears(5));
        when(employeeRepository.existsByBadgeIdAndTenantId(anyString(), any(UUID.class))).thenReturn(false);
        when(employeeMapper.toEntity(any(EmployeeDTO.class))).thenReturn(employee);
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);
        when(employeeMapper.toDTO(any(Employee.class))).thenReturn(employeeDTO);
        EmployeeDTO result = employeeService.createEmployee(employeeDTO);
        assertNotNull(result);
    }
}
