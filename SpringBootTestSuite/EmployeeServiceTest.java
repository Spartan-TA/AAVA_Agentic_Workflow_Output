package com.example.warehouse.service;

import com.example.warehouse.dto.EmployeeRequestDto;
import com.example.warehouse.dto.EmployeeResponseDto;
import com.example.warehouse.entity.Employee;
import com.example.warehouse.enums.EmployeeStatus;
import com.example.warehouse.enums.Role;
import com.example.warehouse.exception.BusinessValidationException;
import com.example.warehouse.exception.ResourceNotFoundException;
import com.example.warehouse.mapper.EmployeeMapper;
import com.example.warehouse.repository.EmployeeRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.data.domain.*;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock private EmployeeRepository employeeRepository;
    @Mock private EmployeeMapper employeeMapper;
    @InjectMocks private EmployeeService employeeService;

    private Employee employee;
    private EmployeeRequestDto requestDto;
    private EmployeeResponseDto responseDto;

    @BeforeEach
    void setUp() {
        employee = new Employee();
        employee.setId(1L);
        employee.setBadgeId("EMP001");
        employee.setFirstName("John");
        employee.setLastName("Doe");
        employee.setEmail("john.doe@example.com");
        employee.setRole(Role.WORKER);
        employee.setStatus(EmployeeStatus.ACTIVE);
        employee.setHireDate(LocalDate.now());

        requestDto = new EmployeeRequestDto();
        requestDto.setBadgeId("EMP001");
        requestDto.setFirstName("John");
        requestDto.setLastName("Doe");
        requestDto.setEmail("john.doe@example.com");
        requestDto.setRole(Role.WORKER);

        responseDto = new EmployeeResponseDto();
        responseDto.setId(1L);
        responseDto.setBadgeId("EMP001");
        responseDto.setFirstName("John");
        responseDto.setLastName("Doe");
        responseDto.setEmail("john.doe@example.com");
        responseDto.setRole(Role.WORKER);
    }

    @Test
    void createEmployee_WithValidData_ShouldReturnEmployeeResponseDto() {
        // Arrange
        when(employeeRepository.existsByBadgeId("EMP001")).thenReturn(false);
        when(employeeRepository.existsByEmail("john.doe@example.com")).thenReturn(false);
        when(employeeMapper.toEntity(requestDto)).thenReturn(employee);
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);
        when(employeeMapper.toDto(employee)).thenReturn(responseDto);

        // Act
        EmployeeResponseDto result = employeeService.createEmployee(requestDto);

        // Assert
        assertNotNull(result);
        assertEquals("EMP001", result.getBadgeId());
        verify(employeeRepository).save(any(Employee.class));
    }

    @Test
    void createEmployee_WithDuplicateBadgeId_ShouldThrowBusinessValidationException() {
        // Arrange
        when(employeeRepository.existsByBadgeId("EMP001")).thenReturn(true);

        // Act & Assert
        assertThrows(BusinessValidationException.class, () -> employeeService.createEmployee(requestDto));
    }

    @Test
    void createEmployee_WithNullBadgeId_ShouldThrowValidationException() {
        // Arrange
        requestDto.setBadgeId(null);

        // Act & Assert
        assertThrows(BusinessValidationException.class, () -> employeeService.createEmployee(requestDto));
    }

    @Test
    void updateEmployee_WithValidId_ShouldReturnUpdatedEmployeeResponseDto() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeMapper.updateEntityFromDto(requestDto, employee)).thenReturn(employee);
        when(employeeRepository.save(employee)).thenReturn(employee);
        when(employeeMapper.toDto(employee)).thenReturn(responseDto);

        // Act
        EmployeeResponseDto result = employeeService.updateEmployee(1L, requestDto);

        // Assert
        assertNotNull(result);
        assertEquals("EMP001", result.getBadgeId());
    }

    @Test
    void updateEmployee_WithNonExistingId_ShouldThrowResourceNotFoundException() {
        // Arrange
        when(employeeRepository.findById(2L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> employeeService.updateEmployee(2L, requestDto));
    }

    @Test
    void getEmployeeById_WithExistingId_ShouldReturnEmployeeResponseDto() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeMapper.toDto(employee)).thenReturn(responseDto);

        // Act
        EmployeeResponseDto result = employeeService.getEmployeeById(1L);

        // Assert
        assertNotNull(result);
        assertEquals("EMP001", result.getBadgeId());
    }

    @Test
    void getEmployeeById_WithNonExistingId_ShouldThrowResourceNotFoundException() {
        // Arrange
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> employeeService.getEmployeeById(99L));
    }

    @Test
    void getAllEmployees_ShouldReturnPagedEmployeeResponseDtos() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> employeePage = new PageImpl<>(Collections.singletonList(employee));
        when(employeeRepository.findAll(pageable)).thenReturn(employeePage);
        when(employeeMapper.toDto(employee)).thenReturn(responseDto);

        // Act
        Page<EmployeeResponseDto> result = employeeService.getAllEmployees(pageable);

        // Assert
        assertEquals(1, result.getTotalElements());
        assertEquals("EMP001", result.getContent().get(0).getBadgeId());
    }

    @Test
    void softDeleteEmployee_WithExistingId_ShouldSetDeletedTrue() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        // Act
        employeeService.softDeleteEmployee(1L);

        // Assert
        assertTrue(employee.getDeleted());
        verify(employeeRepository).save(employee);
    }

    @Test
    void softDeleteEmployee_WithNonExistingId_ShouldThrowResourceNotFoundException() {
        // Arrange
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> employeeService.softDeleteEmployee(99L));
    }
}