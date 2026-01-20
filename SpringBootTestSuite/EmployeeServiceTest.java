package com.warehouse.ems.domain.employee;

import com.warehouse.ems.exception.BusinessException;
import com.warehouse.ems.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("Employee Service Test Suite")
public class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private EmployeeMapper employeeMapper;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    private Employee testEmployee;
    private EmployeeDto testEmployeeDto;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        
        testEmployee = new Employee();
        testEmployee.setId(1L);
        testEmployee.setBadgeId("EMP001");
        testEmployee.setName("John Doe");
        testEmployee.setRole(Role.WORKER);
        testEmployee.setDepartment("Warehouse");
        testEmployee.setShiftGroup("A");
        testEmployee.setHireDate(LocalDate.of(2023, 1, 15));
        testEmployee.setStatus(EmployeeStatus.ACTIVE);
        testEmployee.setDeleted(false);

        testEmployeeDto = new EmployeeDto();
        testEmployeeDto.setId(1L);
        testEmployeeDto.setBadgeId("EMP001");
        testEmployeeDto.setName("John Doe");
        testEmployeeDto.setRole(Role.WORKER);
        testEmployeeDto.setDepartment("Warehouse");
        testEmployeeDto.setShiftGroup("A");
        testEmployeeDto.setHireDate(LocalDate.of(2023, 1, 15));
        testEmployeeDto.setStatus(EmployeeStatus.ACTIVE);
    }

    @Test
    @DisplayName("Test create employee with valid data")
    public void testCreateEmployeeWithValidData() {
        // Arrange
        when(employeeRepository.existsByBadgeIdAndDeletedFalse(testEmployeeDto.getBadgeId())).thenReturn(false);
        when(employeeMapper.toEntity(testEmployeeDto)).thenReturn(testEmployee);
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);
        when(employeeMapper.toDto(testEmployee)).thenReturn(testEmployeeDto);

        // Act
        EmployeeDto result = employeeService.create(testEmployeeDto);

        // Assert
        assertNotNull(result);
        assertEquals("EMP001", result.getBadgeId());
        assertEquals("John Doe", result.getName());
        verify(employeeRepository, times(1)).save(any(Employee.class));
        verify(eventPublisher, times(1)).publishEvent(any());
    }

    @Test
    @DisplayName("Test create employee with duplicate badge ID")
    public void testCreateEmployeeWithDuplicateBadgeId() {
        // Arrange
        when(employeeRepository.existsByBadgeIdAndDeletedFalse(testEmployeeDto.getBadgeId())).thenReturn(true);

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            employeeService.create(testEmployeeDto);
        });
        
        assertTrue(exception.getMessage().contains("Badge ID already exists"));
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    @DisplayName("Test create employee with null badge ID")
    public void testCreateEmployeeWithNullBadgeId() {
        // Arrange
        testEmployeeDto.setBadgeId(null);

        // Act & Assert
        assertThrows(Exception.class, () -> {
            employeeService.create(testEmployeeDto);
        });
    }

    @Test
    @DisplayName("Test create employee with empty name")
    public void testCreateEmployeeWithEmptyName() {
        // Arrange
        testEmployeeDto.setName("");
        when(employeeRepository.existsByBadgeIdAndDeletedFalse(testEmployeeDto.getBadgeId())).thenReturn(false);

        // Act & Assert
        assertThrows(Exception.class, () -> {
            employeeService.create(testEmployeeDto);
        });
    }

    @Test
    @DisplayName("Test create employee with null hire date")
    public void testCreateEmployeeWithNullHireDate() {
        // Arrange
        testEmployeeDto.setHireDate(null);
        when(employeeRepository.existsByBadgeIdAndDeletedFalse(testEmployeeDto.getBadgeId())).thenReturn(false);

        // Act & Assert
        assertThrows(Exception.class, () -> {
            employeeService.create(testEmployeeDto);
        });
    }

    @Test
    @DisplayName("Test create employee with future hire date")
    public void testCreateEmployeeWithFutureHireDate() {
        // Arrange
        testEmployeeDto.setHireDate(LocalDate.now().plusDays(30));
        when(employeeRepository.existsByBadgeIdAndDeletedFalse(testEmployeeDto.getBadgeId())).thenReturn(false);
        when(employeeMapper.toEntity(testEmployeeDto)).thenReturn(testEmployee);
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);
        when(employeeMapper.toDto(testEmployee)).thenReturn(testEmployeeDto);

        // Act
        EmployeeDto result = employeeService.create(testEmployeeDto);

        // Assert
        assertNotNull(result);
        assertTrue(result.getHireDate().isAfter(LocalDate.now()));
    }

    @Test
    @DisplayName("Test find employee by ID - success")
    public void testFindEmployeeByIdSuccess() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeMapper.toDto(testEmployee)).thenReturn(testEmployeeDto);

        // Act
        EmployeeDto result = employeeService.findById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("EMP001", result.getBadgeId());
    }

    @Test
    @DisplayName("Test find employee by ID - not found")
    public void testFindEmployeeByIdNotFound() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            employeeService.findById(999L);
        });
    }

    @Test
    @DisplayName("Test find employee by null ID")
    public void testFindEmployeeByNullId() {
        // Act & Assert
        assertThrows(Exception.class, () -> {
            employeeService.findById(null);
        });
    }

    @Test
    @DisplayName("Test find all employees with pagination")
    public void testFindAllEmployeesWithPagination() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 20);
        Page<Employee> employeePage = new PageImpl<>(Arrays.asList(testEmployee));
        when(employeeRepository.findAllByDeletedFalse(pageable)).thenReturn(employeePage);
        when(employeeMapper.toDto(testEmployee)).thenReturn(testEmployeeDto);

        // Act
        Page<EmployeeDto> result = employeeService.findAll(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("EMP001", result.getContent().get(0).getBadgeId());
    }

    @Test
    @DisplayName("Test update employee - success")
    public void testUpdateEmployeeSuccess() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);
        when(employeeMapper.toDto(testEmployee)).thenReturn(testEmployeeDto);

        testEmployeeDto.setName("Jane Doe");

        // Act
        EmployeeDto result = employeeService.update(1L, testEmployeeDto);

        // Assert
        assertNotNull(result);
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    @DisplayName("Test update non-existent employee")
    public void testUpdateNonExistentEmployee() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            employeeService.update(999L, testEmployeeDto);
        });
    }

    @Test
    @DisplayName("Test soft delete employee - success")
    public void testSoftDeleteEmployeeSuccess() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        employeeService.delete(1L);

        // Assert
        verify(employeeRepository, times(1)).save(any(Employee.class));
        verify(eventPublisher, times(1)).publishEvent(any());
    }

    @Test
    @DisplayName("Test soft delete non-existent employee")
    public void testSoftDeleteNonExistentEmployee() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            employeeService.delete(999L);
        });
    }

    @Test
    @DisplayName("Test soft delete with null ID")
    public void testSoftDeleteWithNullId() {
        // Act & Assert
        assertThrows(Exception.class, () -> {
            employeeService.delete(null);
        });
    }

    @Test
    @DisplayName("Test find by badge ID - success")
    public void testFindByBadgeIdSuccess() {
        // Arrange
        when(employeeRepository.findByBadgeIdAndDeletedFalse("EMP001")).thenReturn(Optional.of(testEmployee));
        when(employeeMapper.toDto(testEmployee)).thenReturn(testEmployeeDto);

        // Act
        EmployeeDto result = employeeService.findByBadgeId("EMP001");

        // Assert
        assertNotNull(result);
        assertEquals("EMP001", result.getBadgeId());
    }

    @Test
    @DisplayName("Test find by badge ID - not found")
    public void testFindByBadgeIdNotFound() {
        // Arrange
        when(employeeRepository.findByBadgeIdAndDeletedFalse("INVALID")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            employeeService.findByBadgeId("INVALID");
        });
    }

    @Test
    @DisplayName("Test find by null badge ID")
    public void testFindByNullBadgeId() {
        // Act & Assert
        assertThrows(Exception.class, () -> {
            employeeService.findByBadgeId(null);
        });
    }

    @Test
    @DisplayName("Test find by empty badge ID")
    public void testFindByEmptyBadgeId() {
        // Act & Assert
        assertThrows(Exception.class, () -> {
            employeeService.findByBadgeId("");
        });
    }

    @Test
    @DisplayName("Test create employee with maximum length badge ID")
    public void testCreateEmployeeWithMaxLengthBadgeId() {
        // Arrange
        String maxLengthBadgeId = "A".repeat(50);
        testEmployeeDto.setBadgeId(maxLengthBadgeId);
        when(employeeRepository.existsByBadgeIdAndDeletedFalse(maxLengthBadgeId)).thenReturn(false);
        when(employeeMapper.toEntity(testEmployeeDto)).thenReturn(testEmployee);
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);
        when(employeeMapper.toDto(testEmployee)).thenReturn(testEmployeeDto);

        // Act
        EmployeeDto result = employeeService.create(testEmployeeDto);

        // Assert
        assertNotNull(result);
        assertEquals(50, result.getBadgeId().length());
    }

    @Test
    @DisplayName("Test create employee with special characters in name")
    public void testCreateEmployeeWithSpecialCharactersInName() {
        // Arrange
        testEmployeeDto.setName("O'Brien-Smith");
        when(employeeRepository.existsByBadgeIdAndDeletedFalse(testEmployeeDto.getBadgeId())).thenReturn(false);
        when(employeeMapper.toEntity(testEmployeeDto)).thenReturn(testEmployee);
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);
        when(employeeMapper.toDto(testEmployee)).thenReturn(testEmployeeDto);

        // Act
        EmployeeDto result = employeeService.create(testEmployeeDto);

        // Assert
        assertNotNull(result);
        assertEquals("O'Brien-Smith", result.getName());
    }
}