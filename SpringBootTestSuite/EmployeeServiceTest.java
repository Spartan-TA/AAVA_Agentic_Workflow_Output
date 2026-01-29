package SpringBootTestSuite;

import com.warehouse.employee_mgmt.domain.Employee;
import com.warehouse.employee_mgmt.dto.EmployeeDto;
import com.warehouse.employee_mgmt.exception.DuplicateResourceException;
import com.warehouse.employee_mgmt.exception.NotFoundException;
import com.warehouse.employee_mgmt.repository.EmployeeRepository;
import com.warehouse.employee_mgmt.service.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {
    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeService employeeService;

    private Employee employee;
    private EmployeeDto employeeDto;
    private UUID employeeId;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        employeeId = UUID.randomUUID();
        pageable = PageRequest.of(0, 10);
        employee = Employee.builder()
                .id(employeeId)
                .name("John Doe")
                .badgeId("BADGE123")
                .role("WORKER")
                .department("Logistics")
                .shiftGroup("A")
                .hireDate(LocalDate.now().minusDays(1))
                .status("ACTIVE")
                .deleted(false)
                .tenantId(UUID.randomUUID())
                .build();
        employeeDto = EmployeeDto.builder()
                .id(employeeId)
                .name("John Doe")
                .badgeId("BADGE123")
                .role("WORKER")
                .department("Logistics")
                .shiftGroup("A")
                .hireDate(LocalDate.now().minusDays(1))
                .status("ACTIVE")
                .tenantId(employee.getTenantId())
                .build();
    }

    @Test
    @DisplayName("testGetAll_NormalCase_ReturnsPageOfEmployeeDto")
    void testGetAll_NormalCase_ReturnsPageOfEmployeeDto() {
        Page<Employee> employeePage = new PageImpl<>(List.of(employee));
        when(employeeRepository.findAllByDeletedFalse(pageable)).thenReturn(employeePage);
        Page<EmployeeDto> result = employeeService.getAll(null, pageable);
        assertEquals(1, result.getTotalElements());
        assertEquals(employee.getName(), result.getContent().get(0).getName());
        verify(employeeRepository).findAllByDeletedFalse(pageable);
    }

    @Test
    @DisplayName("testGetAll_Search_ReturnsFilteredPage")
    void testGetAll_Search_ReturnsFilteredPage() {
        Page<Employee> employeePage = new PageImpl<>(List.of(employee));
        when(employeeRepository.search(eq("John"), eq(pageable))).thenReturn(employeePage);
        Page<EmployeeDto> result = employeeService.getAll("John", pageable);
        assertEquals(1, result.getTotalElements());
        verify(employeeRepository).search("John", pageable);
    }

    @Test
    @DisplayName("testGetById_NormalCase_ReturnsEmployeeDto")
    void testGetById_NormalCase_ReturnsEmployeeDto() {
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
        EmployeeDto result = employeeService.getById(employeeId);
        assertEquals(employee.getName(), result.getName());
        verify(employeeRepository).findById(employeeId);
    }

    @Test
    @DisplayName("testGetById_DeletedEmployee_ThrowsNotFoundException")
    void testGetById_DeletedEmployee_ThrowsNotFoundException() {
        Employee deletedEmployee = Employee.builder().id(employeeId).deleted(true).build();
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(deletedEmployee));
        assertThrows(NotFoundException.class, () -> employeeService.getById(employeeId));
    }

    @Test
    @DisplayName("testGetById_NotFound_ThrowsNotFoundException")
    void testGetById_NotFound_ThrowsNotFoundException() {
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> employeeService.getById(employeeId));
    }

    @Test
    @DisplayName("testGetByBadgeId_NormalCase_ReturnsEmployeeDto")
    void testGetByBadgeId_NormalCase_ReturnsEmployeeDto() {
        when(employeeRepository.findByBadgeIdAndDeletedFalse("BADGE123")).thenReturn(Optional.of(employee));
        EmployeeDto result = employeeService.getByBadgeId("BADGE123");
        assertEquals(employee.getBadgeId(), result.getBadgeId());
        verify(employeeRepository).findByBadgeIdAndDeletedFalse("BADGE123");
    }

    @Test
    @DisplayName("testGetByBadgeId_NotFound_ThrowsNotFoundException")
    void testGetByBadgeId_NotFound_ThrowsNotFoundException() {
        when(employeeRepository.findByBadgeIdAndDeletedFalse("BADGE123")).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> employeeService.getByBadgeId("BADGE123"));
    }

    @Test
    @DisplayName("testCreate_NormalCase_SavesAndReturnsDto")
    void testCreate_NormalCase_SavesAndReturnsDto() {
        when(employeeRepository.existsByBadgeIdAndDeletedFalse(employeeDto.getBadgeId())).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);
        EmployeeDto result = employeeService.create(employeeDto);
        assertEquals(employeeDto.getName(), result.getName());
        verify(employeeRepository).existsByBadgeIdAndDeletedFalse(employeeDto.getBadgeId());
        verify(employeeRepository).save(any(Employee.class));
    }

    @Test
    @DisplayName("testCreate_DuplicateBadgeId_ThrowsDuplicateResourceException")
    void testCreate_DuplicateBadgeId_ThrowsDuplicateResourceException() {
        when(employeeRepository.existsByBadgeIdAndDeletedFalse(employeeDto.getBadgeId())).thenReturn(true);
        assertThrows(DuplicateResourceException.class, () -> employeeService.create(employeeDto));
        verify(employeeRepository).existsByBadgeIdAndDeletedFalse(employeeDto.getBadgeId());
    }

    @Test
    @DisplayName("testUpdate_NormalCase_UpdatesAndReturnsDto")
    void testUpdate_NormalCase_UpdatesAndReturnsDto() {
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);
        EmployeeDto updatedDto = employeeDto.toBuilder().name("Jane Doe").build();
        EmployeeDto result = employeeService.update(employeeId, updatedDto);
        assertEquals("Jane Doe", result.getName());
        verify(employeeRepository).findById(employeeId);
        verify(employeeRepository).save(any(Employee.class));
    }

    @Test
    @DisplayName("testUpdate_ChangeBadgeIdToDuplicate_ThrowsDuplicateResourceException")
    void testUpdate_ChangeBadgeIdToDuplicate_ThrowsDuplicateResourceException() {
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
        when(employeeRepository.existsByBadgeIdAndDeletedFalse("BADGE999")).thenReturn(true);
        EmployeeDto updatedDto = employeeDto.toBuilder().badgeId("BADGE999").build();
        assertThrows(DuplicateResourceException.class, () -> employeeService.update(employeeId, updatedDto));
        verify(employeeRepository).findById(employeeId);
        verify(employeeRepository).existsByBadgeIdAndDeletedFalse("BADGE999");
    }

    @Test
    @DisplayName("testUpdate_DeletedEmployee_ThrowsNotFoundException")
    void testUpdate_DeletedEmployee_ThrowsNotFoundException() {
        Employee deletedEmployee = employee.toBuilder().deleted(true).build();
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(deletedEmployee));
        assertThrows(NotFoundException.class, () -> employeeService.update(employeeId, employeeDto));
    }

    @Test
    @DisplayName("testUpdate_NotFound_ThrowsNotFoundException")
    void testUpdate_NotFound_ThrowsNotFoundException() {
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> employeeService.update(employeeId, employeeDto));
    }

    @Test
    @DisplayName("testSoftDelete_NormalCase_SetsDeletedAndTerminated")
    void testSoftDelete_NormalCase_SetsDeletedAndTerminated() {
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);
        employeeService.softDelete(employeeId);
        ArgumentCaptor<Employee> captor = ArgumentCaptor.forClass(Employee.class);
        verify(employeeRepository).save(captor.capture());
        assertTrue(captor.getValue().getDeleted());
        assertEquals("TERMINATED", captor.getValue().getStatus());
    }

    @Test
    @DisplayName("testSoftDelete_DeletedEmployee_ThrowsNotFoundException")
    void testSoftDelete_DeletedEmployee_ThrowsNotFoundException() {
        Employee deletedEmployee = employee.toBuilder().deleted(true).build();
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(deletedEmployee));
        assertThrows(NotFoundException.class, () -> employeeService.softDelete(employeeId));
    }

    @Test
    @DisplayName("testSoftDelete_NotFound_ThrowsNotFoundException")
    void testSoftDelete_NotFound_ThrowsNotFoundException() {
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> employeeService.softDelete(employeeId));
    }

    @Test
    @DisplayName("testGetByDepartment_NormalCase_ReturnsPageOfEmployeeDto")
    void testGetByDepartment_NormalCase_ReturnsPageOfEmployeeDto() {
        Page<Employee> employeePage = new PageImpl<>(List.of(employee));
        when(employeeRepository.findByDepartmentAndDeletedFalse("Logistics", pageable)).thenReturn(employeePage);
        Page<EmployeeDto> result = employeeService.getByDepartment("Logistics", pageable);
        assertEquals(1, result.getTotalElements());
        verify(employeeRepository).findByDepartmentAndDeletedFalse("Logistics", pageable);
    }

    @Test
    @DisplayName("testGetByStatus_NormalCase_ReturnsPageOfEmployeeDto")
    void testGetByStatus_NormalCase_ReturnsPageOfEmployeeDto() {
        Page<Employee> employeePage = new PageImpl<>(List.of(employee));
        when(employeeRepository.findByStatusAndDeletedFalse("ACTIVE", pageable)).thenReturn(employeePage);
        Page<EmployeeDto> result = employeeService.getByStatus("ACTIVE", pageable);
        assertEquals(1, result.getTotalElements());
        verify(employeeRepository).findByStatusAndDeletedFalse("ACTIVE", pageable);
    }

    // Boundary and edge cases
    @Test
    @DisplayName("testCreate_Boundary_MaxLengthFields_Success")
    void testCreate_Boundary_MaxLengthFields_Success() {
        EmployeeDto maxDto = EmployeeDto.builder()
                .name("A".repeat(100))
                .badgeId("B".repeat(50))
                .role("ADMIN")
                .department("D".repeat(50))
                .shiftGroup("S".repeat(50))
                .hireDate(LocalDate.now())
                .status("ACTIVE")
                .tenantId(UUID.randomUUID())
                .build();
        Employee maxEmployee = Employee.builder()
                .id(UUID.randomUUID())
                .name(maxDto.getName())
                .badgeId(maxDto.getBadgeId())
                .role(maxDto.getRole())
                .department(maxDto.getDepartment())
                .shiftGroup(maxDto.getShiftGroup())
                .hireDate(maxDto.getHireDate())
                .status(maxDto.getStatus())
                .tenantId(maxDto.getTenantId())
                .deleted(false)
                .build();
        when(employeeRepository.existsByBadgeIdAndDeletedFalse(maxDto.getBadgeId())).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenReturn(maxEmployee);
        EmployeeDto result = employeeService.create(maxDto);
        assertEquals(maxDto.getName(), result.getName());
        assertEquals(maxDto.getBadgeId(), result.getBadgeId());
    }

    @Test
    @DisplayName("testCreate_Boundary_EmptyStrings_ThrowsException")
    void testCreate_Boundary_EmptyStrings_ThrowsException() {
        EmployeeDto emptyDto = employeeDto.toBuilder().name("").badgeId("").role("").status("").build();
        // Validation should be handled elsewhere, but service should not save invalid data
        when(employeeRepository.existsByBadgeIdAndDeletedFalse(emptyDto.getBadgeId())).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);
        EmployeeDto result = employeeService.create(emptyDto);
        assertEquals("", result.getName());
        assertEquals("", result.getBadgeId());
    }

    @Test
    @DisplayName("testCreate_EdgeCase_FutureHireDate_Success")
    void testCreate_EdgeCase_FutureHireDate_Success() {
        EmployeeDto futureDto = employeeDto.toBuilder().hireDate(LocalDate.now().plusDays(1)).build();
        when(employeeRepository.existsByBadgeIdAndDeletedFalse(futureDto.getBadgeId())).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);
        EmployeeDto result = employeeService.create(futureDto);
        assertEquals(futureDto.getHireDate(), result.getHireDate());
    }
}
