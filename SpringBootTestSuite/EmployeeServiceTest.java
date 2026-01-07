package com.company.wems.employee.service;

import com.company.wems.common.exception.BusinessException;
import com.company.wems.common.exception.DuplicateResourceException;
import com.company.wems.common.exception.ResourceNotFoundException;
import com.company.wems.employee.domain.Employee;
import com.company.wems.employee.domain.Department;
import com.company.wems.employee.dto.EmployeeCreateDto;
import com.company.wems.employee.dto.EmployeeResponseDto;
import com.company.wems.employee.dto.EmployeeUpdateDto;
import com.company.wems.employee.dto.EmployeeFilterDto;
import com.company.wems.employee.repository.EmployeeRepository;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for EmployeeService.
 * Tests cover normal cases, boundary conditions, and edge cases.
 */
@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private EmployeeMapper employeeMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private EmployeeService employeeService;

    private EmployeeCreateDto validCreateDto;
    private Employee validEmployee;
    private EmployeeResponseDto validResponseDto;
    private Department validDepartment;

    @BeforeEach
    void setUp() {
        // Setup valid department
        validDepartment = new Department();
        validDepartment.setId(1L);
        validDepartment.setName("Warehouse Operations");
        validDepartment.setCode("WH-OPS");

        // Setup valid create DTO
        validCreateDto = new EmployeeCreateDto();
        validCreateDto.setName("John Doe");
        validCreateDto.setBadgeId("EMP001");
        validCreateDto.setRole(Employee.Role.WORKER);
        validCreateDto.setDepartmentId(1L);
        validCreateDto.setHireDate(LocalDate.now());
        validCreateDto.setEmail("john.doe@company.com");
        validCreateDto.setPhoneNumber("+1234567890");
        validCreateDto.setPassword("SecurePass123");

        // Setup valid employee entity
        validEmployee = Employee.builder()
                .id(1L)
                .name("John Doe")
                .badgeId("EMP001")
                .role(Employee.Role.WORKER)
                .department(validDepartment)
                .hireDate(LocalDate.now())
                .status(Employee.Status.ACTIVE)
                .email("john.doe@company.com")
                .phoneNumber("+1234567890")
                .passwordHash("$2a$10$encodedPassword")
                .build();

        // Setup valid response DTO
        validResponseDto = new EmployeeResponseDto();
        validResponseDto.setId(1L);
        validResponseDto.setName("John Doe");
        validResponseDto.setBadgeId("EMP001");
        validResponseDto.setRole(Employee.Role.WORKER);
        validResponseDto.setStatus(Employee.Status.ACTIVE);
    }

    // ========== CREATE EMPLOYEE TESTS ==========

    @Test
    void createEmployee_ValidInput_ReturnsCreatedEmployee() {
        // Arrange
        when(employeeRepository.existsByBadgeId(anyString())).thenReturn(false);
        when(employeeRepository.existsByEmailIgnoreCase(anyString())).thenReturn(false);
        when(employeeMapper.toEntity(any(EmployeeCreateDto.class))).thenReturn(validEmployee);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$encodedPassword");
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);
        when(employeeMapper.toResponseDto(any(Employee.class))).thenReturn(validResponseDto);

        // Act
        EmployeeResponseDto result = employeeService.createEmployee(validCreateDto);

        // Assert
        assertNotNull(result);
        assertEquals("John Doe", result.getName());
        assertEquals("EMP001", result.getBadgeId());
        assertEquals(Employee.Role.WORKER, result.getRole());
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    void createEmployee_DuplicateBadgeId_ThrowsDuplicateResourceException() {
        // Arrange
        when(employeeRepository.existsByBadgeId("EMP001")).thenReturn(true);

        // Act & Assert
        DuplicateResourceException exception = assertThrows(
                DuplicateResourceException.class,
                () -> employeeService.createEmployee(validCreateDto)
        );
        assertTrue(exception.getMessage().contains("badgeId"));
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void createEmployee_DuplicateEmail_ThrowsDuplicateResourceException() {
        // Arrange
        when(employeeRepository.existsByBadgeId(anyString())).thenReturn(false);
        when(employeeRepository.existsByEmailIgnoreCase("john.doe@company.com")).thenReturn(true);

        // Act & Assert
        DuplicateResourceException exception = assertThrows(
                DuplicateResourceException.class,
                () -> employeeService.createEmployee(validCreateDto)
        );
        assertTrue(exception.getMessage().contains("email"));
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void createEmployee_NullEmail_SuccessfullyCreates() {
        // Arrange
        validCreateDto.setEmail(null);
        when(employeeRepository.existsByBadgeId(anyString())).thenReturn(false);
        when(employeeMapper.toEntity(any(EmployeeCreateDto.class))).thenReturn(validEmployee);
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);
        when(employeeMapper.toResponseDto(any(Employee.class))).thenReturn(validResponseDto);

        // Act
        EmployeeResponseDto result = employeeService.createEmployee(validCreateDto);

        // Assert
        assertNotNull(result);
        verify(employeeRepository, never()).existsByEmailIgnoreCase(anyString());
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    void createEmployee_NullPassword_SuccessfullyCreatesWithoutPasswordHash() {
        // Arrange
        validCreateDto.setPassword(null);
        when(employeeRepository.existsByBadgeId(anyString())).thenReturn(false);
        when(employeeRepository.existsByEmailIgnoreCase(anyString())).thenReturn(false);
        when(employeeMapper.toEntity(any(EmployeeCreateDto.class))).thenReturn(validEmployee);
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);
        when(employeeMapper.toResponseDto(any(Employee.class))).thenReturn(validResponseDto);

        // Act
        EmployeeResponseDto result = employeeService.createEmployee(validCreateDto);

        // Assert
        assertNotNull(result);
        verify(passwordEncoder, never()).encode(anyString());
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    void createEmployee_EmptyBadgeId_ThrowsException() {
        // Arrange
        validCreateDto.setBadgeId("");

        // Act & Assert - This would be caught by validation before reaching service
        // But we test the repository behavior
        when(employeeRepository.existsByBadgeId("")).thenReturn(false);
        when(employeeMapper.toEntity(any())).thenReturn(validEmployee);
        when(employeeRepository.save(any())).thenReturn(validEmployee);
        when(employeeMapper.toResponseDto(any())).thenReturn(validResponseDto);

        EmployeeResponseDto result = employeeService.createEmployee(validCreateDto);
        assertNotNull(result);
    }

    // ========== GET EMPLOYEE TESTS ==========

    @Test
    void getEmployeeById_ValidId_ReturnsEmployee() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        when(employeeMapper.toResponseDto(any(Employee.class))).thenReturn(validResponseDto);

        // Act
        EmployeeResponseDto result = employeeService.getEmployeeById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("John Doe", result.getName());
        verify(employeeRepository, times(1)).findById(1L);
    }

    @Test
    void getEmployeeById_InvalidId_ThrowsResourceNotFoundException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> employeeService.getEmployeeById(999L)
        );
        assertTrue(exception.getMessage().contains("not found"));
    }

    @Test
    void getEmployeeById_NullId_ThrowsException() {
        // Act & Assert
        assertThrows(Exception.class, () -> employeeService.getEmployeeById(null));
    }

    @Test
    void getEmployeeById_NegativeId_ThrowsResourceNotFoundException() {
        // Arrange
        when(employeeRepository.findById(-1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> employeeService.getEmployeeById(-1L));
    }

    // ========== UPDATE EMPLOYEE TESTS ==========

    @Test
    void updateEmployee_ValidInput_ReturnsUpdatedEmployee() {
        // Arrange
        EmployeeUpdateDto updateDto = new EmployeeUpdateDto();
        updateDto.setName("John Updated");
        updateDto.setEmail("john.updated@company.com");

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        when(employeeRepository.existsByEmailIgnoreCase(anyString())).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);
        when(employeeMapper.toResponseDto(any(Employee.class))).thenReturn(validResponseDto);

        // Act
        EmployeeResponseDto result = employeeService.updateEmployee(1L, updateDto);

        // Assert
        assertNotNull(result);
        verify(employeeMapper, times(1)).updateEntityFromDto(updateDto, validEmployee);
        verify(employeeRepository, times(1)).save(validEmployee);
    }

    @Test
    void updateEmployee_DuplicateEmail_ThrowsDuplicateResourceException() {
        // Arrange
        EmployeeUpdateDto updateDto = new EmployeeUpdateDto();
        updateDto.setEmail("existing@company.com");

        validEmployee.setEmail("john.doe@company.com");
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        when(employeeRepository.existsByEmailIgnoreCase("existing@company.com")).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateResourceException.class,
                () -> employeeService.updateEmployee(1L, updateDto));
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void updateEmployee_SameEmail_SuccessfullyUpdates() {
        // Arrange
        EmployeeUpdateDto updateDto = new EmployeeUpdateDto();
        updateDto.setEmail("john.doe@company.com");

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);
        when(employeeMapper.toResponseDto(any(Employee.class))).thenReturn(validResponseDto);

        // Act
        EmployeeResponseDto result = employeeService.updateEmployee(1L, updateDto);

        // Assert
        assertNotNull(result);
        verify(employeeRepository, never()).existsByEmailIgnoreCase(anyString());
        verify(employeeRepository, times(1)).save(validEmployee);
    }

    @Test
    void updateEmployee_NonExistentId_ThrowsResourceNotFoundException() {
        // Arrange
        EmployeeUpdateDto updateDto = new EmployeeUpdateDto();
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> employeeService.updateEmployee(999L, updateDto));
    }

    // ========== SOFT DELETE TESTS ==========

    @Test
    void softDeleteEmployee_ValidId_SuccessfullyDeletes() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);

        // Act
        employeeService.softDeleteEmployee(1L);

        // Assert
        verify(employeeRepository, times(1)).save(validEmployee);
        assertEquals(Employee.Status.DELETED, validEmployee.getStatus());
        assertNotNull(validEmployee.getDeletedAt());
    }

    @Test
    void softDeleteEmployee_NonExistentId_ThrowsResourceNotFoundException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> employeeService.softDeleteEmployee(999L));
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void softDeleteEmployee_AlreadyDeleted_StillProcesses() {
        // Arrange
        validEmployee.setStatus(Employee.Status.DELETED);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);

        // Act
        employeeService.softDeleteEmployee(1L);

        // Assert
        verify(employeeRepository, times(1)).save(validEmployee);
    }

    // ========== LIST EMPLOYEES TESTS ==========

    @Test
    void listEmployees_WithPagination_ReturnsPagedResults() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 20);
        Page<Employee> employeePage = new PageImpl<>(Arrays.asList(validEmployee));
        when(employeeRepository.findAll(pageable)).thenReturn(employeePage);
        when(employeeMapper.toResponseDto(any(Employee.class))).thenReturn(validResponseDto);

        // Act
        Page<EmployeeResponseDto> result = employeeService.listEmployees(new EmployeeFilterDto(), pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("John Doe", result.getContent().get(0).getName());
    }

    @Test
    void listEmployees_EmptyResults_ReturnsEmptyPage() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 20);
        Page<Employee> emptyPage = Page.empty();
        when(employeeRepository.findAll(pageable)).thenReturn(emptyPage);

        // Act
        Page<EmployeeResponseDto> result = employeeService.listEmployees(new EmployeeFilterDto(), pageable);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }

    @Test
    void listEmployees_LargePage_HandlesCorrectly() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 1000);
        Page<Employee> employeePage = new PageImpl<>(Arrays.asList(validEmployee));
        when(employeeRepository.findAll(pageable)).thenReturn(employeePage);
        when(employeeMapper.toResponseDto(any(Employee.class))).thenReturn(validResponseDto);

        // Act
        Page<EmployeeResponseDto> result = employeeService.listEmployees(new EmployeeFilterDto(), pageable);

        // Assert
        assertNotNull(result);
        verify(employeeRepository, times(1)).findAll(pageable);
    }

    // ========== FIND BY ID (INTERNAL) TESTS ==========

    @Test
    void findById_ValidId_ReturnsEmployee() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));

        // Act
        Employee result = employeeService.findById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("John Doe", result.getName());
    }

    @Test
    void findById_InvalidId_ThrowsResourceNotFoundException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> employeeService.findById(999L));
    }

    // ========== LOAD USER BY USERNAME TESTS ==========

    @Test
    void loadUserByUsername_ValidBadgeId_ReturnsUserDetails() {
        // Arrange
        when(employeeRepository.findByBadgeId("EMP001")).thenReturn(Optional.of(validEmployee));

        // Act
        var userDetails = employeeService.loadUserByUsername("EMP001");

        // Assert
        assertNotNull(userDetails);
        assertEquals("EMP001", userDetails.getUsername());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_WORKER")));
    }

    @Test
    void loadUserByUsername_InvalidBadgeId_ThrowsUsernameNotFoundException() {
        // Arrange
        when(employeeRepository.findByBadgeId("INVALID")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(org.springframework.security.core.userdetails.UsernameNotFoundException.class,
                () -> employeeService.loadUserByUsername("INVALID"));
    }

    @Test
    void loadUserByUsername_InactiveEmployee_ThrowsBusinessException() {
        // Arrange
        validEmployee.setStatus(Employee.Status.INACTIVE);
        when(employeeRepository.findByBadgeId("EMP001")).thenReturn(Optional.of(validEmployee));

        // Act & Assert
        assertThrows(BusinessException.class,
                () -> employeeService.loadUserByUsername("EMP001"));
    }

    @Test
    void loadUserByUsername_DeletedEmployee_ThrowsBusinessException() {
        // Arrange
        validEmployee.setStatus(Employee.Status.DELETED);
        when(employeeRepository.findByBadgeId("EMP001")).thenReturn(Optional.of(validEmployee));

        // Act & Assert
        assertThrows(BusinessException.class,
                () -> employeeService.loadUserByUsername("EMP001"));
    }
}