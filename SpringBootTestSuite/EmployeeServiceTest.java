package com.warehouse.employeemgmt.employee;

import com.warehouse.employeemgmt.common.exception.ApiException;
import com.warehouse.employeemgmt.common.exception.ResourceNotFoundException;
import com.warehouse.employeemgmt.employee.dto.*;
import com.warehouse.employeemgmt.employee.enums.EmployeeRole;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for EmployeeService using Mockito.
 */
@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {
    @Mock
    private EmployeeRepository employeeRepository;
    @InjectMocks
    private EmployeeService employeeService;

    private Employee employee;
    private EmployeeRequestDTO requestDTO;
    private EmployeeResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        employee = Employee.builder()
                .id(1L)
                .badgeId("BADGE1")
                .name("Alice")
                .role(EmployeeRole.ADMIN)
                .department("HR")
                .shiftGroup("Morning")
                .hireDate(LocalDate.now().minusYears(2))
                .status("ACTIVE")
                .email("alice@warehouse.com")
                .phoneNumber("1234567890")
                .emergencyContactName("Bob")
                .emergencyContactPhone("0987654321")
                .createdAt(LocalDateTime.now().minusDays(10))
                .updatedAt(LocalDateTime.now().minusDays(1))
                .deleted(false)
                .build();
        requestDTO = new EmployeeRequestDTO("BADGE1", "Alice", EmployeeRole.ADMIN, "HR", "Morning", LocalDate.now().minusYears(2), "ACTIVE", "alice@warehouse.com", "1234567890", "Bob", "0987654321");
        responseDTO = new EmployeeResponseDTO(1L, "BADGE1", "Alice", EmployeeRole.ADMIN, "HR", "Morning", LocalDate.now().minusYears(2), "ACTIVE", "alice@warehouse.com", "1234567890", "Bob", "0987654321", employee.getCreatedAt(), employee.getUpdatedAt());
    }

    @Test
    @DisplayName("getAll returns page of EmployeeResponseDTOs")
    void getAll_returnsPageOfEmployeeResponseDTOs() {
        Page<Employee> employeePage = new PageImpl<>(List.of(employee));
        when(employeeRepository.findAllByDeletedFalse(any(Pageable.class))).thenReturn(employeePage);
        Page<EmployeeResponseDTO> result = employeeService.getAll(PageRequest.of(0, 10));
        assertEquals(1, result.getTotalElements());
        assertEquals("Alice", result.getContent().get(0).name());
    }

    @Test
    @DisplayName("getById returns EmployeeResponseDTO for valid id")
    void getById_validId_returnsEmployeeResponseDTO() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        EmployeeResponseDTO result = employeeService.getById(1L);
        assertEquals("Alice", result.name());
    }

    @Test
    @DisplayName("getById throws ResourceNotFoundException for deleted employee")
    void getById_deletedEmployee_throwsException() {
        Employee deleted = Employee.builder().id(2L).deleted(true).build();
        when(employeeRepository.findById(2L)).thenReturn(Optional.of(deleted));
        assertThrows(ResourceNotFoundException.class, () -> employeeService.getById(2L));
    }

    @Test
    @DisplayName("getById throws ResourceNotFoundException for non-existent id")
    void getById_nonExistentId_throwsException() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> employeeService.getById(99L));
    }

    @Test
    @DisplayName("create saves and returns EmployeeResponseDTO for valid input")
    void create_validInput_savesAndReturnsEmployeeResponseDTO() {
        when(employeeRepository.findByBadgeIdAndDeletedFalse("BADGE1")).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> {
            Employee e = invocation.getArgument(0);
            e.setId(1L);
            return e;
        });
        EmployeeResponseDTO result = employeeService.create(requestDTO, "admin");
        assertEquals("BADGE1", result.badgeId());
        assertEquals("Alice", result.name());
    }

    @Test
    @DisplayName("create throws ApiException for duplicate badgeId")
    void create_duplicateBadgeId_throwsApiException() {
        when(employeeRepository.findByBadgeIdAndDeletedFalse("BADGE1")).thenReturn(Optional.of(employee));
        assertThrows(ApiException.class, () -> employeeService.create(requestDTO, "admin"));
    }

    @Test
    @DisplayName("softDelete sets deleted and status to INACTIVE")
    void softDelete_setsDeletedAndStatusToInactive() {
        Employee notDeleted = Employee.builder().id(1L).deleted(false).status("ACTIVE").build();
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(notDeleted));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> invocation.getArgument(0));
        employeeService.softDelete(1L, "admin");
        assertTrue(notDeleted.isDeleted());
        assertEquals("INACTIVE", notDeleted.getStatus());
    }

    @Test
    @DisplayName("softDelete throws ResourceNotFoundException for deleted employee")
    void softDelete_deletedEmployee_throwsException() {
        Employee deleted = Employee.builder().id(2L).deleted(true).build();
        when(employeeRepository.findById(2L)).thenReturn(Optional.of(deleted));
        assertThrows(ResourceNotFoundException.class, () -> employeeService.softDelete(2L, "admin"));
    }

    @Test
    @DisplayName("softDelete throws ResourceNotFoundException for non-existent id")
    void softDelete_nonExistentId_throwsException() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> employeeService.softDelete(99L, "admin"));
    }
}
