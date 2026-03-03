package com.company.wms.employee.service;

import com.company.wms.employee.dto.EmployeeDTO;
import com.company.wms.employee.entity.Employee;
import com.company.wms.employee.mapper.EmployeeMapper;
import com.company.wms.employee.repository.EmployeeRepository;
import com.company.wms.exception.NotFoundException;
import com.company.wms.exception.DuplicateResourceException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDate;
import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for EmployeeService.
 * Tests business logic, validation, and error handling.
 * 
 * @author WMS Development Team
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private EmployeeMapper employeeMapper;

    @InjectMocks
    private EmployeeService employeeService;

    private Employee testEmployee;
    private EmployeeDTO testEmployeeDTO;

    @BeforeEach
    void setUp() {
        testEmployee = Employee.builder()
            .id(1L)
            .badgeId("EMP001")
            .name("John Doe")
            .role("WORKER")
            .department("Shipping")
            .shiftGroup("DAY_SHIFT")
            .hireDate(LocalDate.of(2024, 1, 15))
            .status("ACTIVE")
            .deleted(false)
            .email("john.doe@company.com")
            .phone("+1234567890")
            .build();

        testEmployeeDTO = EmployeeDTO.builder()
            .id(1L)
            .badgeId("EMP001")
            .name("John Doe")
            .role("WORKER")
            .department("Shipping")
            .shiftGroup("DAY_SHIFT")
            .hireDate(LocalDate.of(2024, 1, 15))
            .status("ACTIVE")
            .email("john.doe@company.com")
            .phone("+1234567890")
            .build();
    }

    @Test
    @DisplayName("Should get employee by ID successfully")
    void testGetById_Success() {
        // Arrange
        when(employeeRepository.findActiveById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeMapper.toDto(testEmployee)).thenReturn(testEmployeeDTO);

        // Act
        EmployeeDTO result = employeeService.getById(1L);

        // Assert
        assertNotNull(result);
        assertEquals("John Doe", result.getName());
        assertEquals("EMP001", result.getBadgeId());
        verify(employeeRepository, times(1)).findActiveById(1L);
        verify(employeeMapper, times(1)).toDto(testEmployee);
    }

    @Test
    @DisplayName("Should throw NotFoundException when employee not found")
    void testGetById_NotFound() {
        // Arrange
        when(employeeRepository.findActiveById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> employeeService.getById(999L));
        verify(employeeRepository, times(1)).findActiveById(999L);
        verify(employeeMapper, never()).toDto(any());
    }

    @Test
    @DisplayName("Should create employee successfully")
    void testCreate_Success() {
        // Arrange
        when(employeeRepository.findByBadgeIdAndDeletedFalse("EMP001")).thenReturn(Optional.empty());
        when(employeeMapper.toEntity(testEmployeeDTO)).thenReturn(testEmployee);
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);
        when(employeeMapper.toDto(testEmployee)).thenReturn(testEmployeeDTO);

        // Act
        EmployeeDTO result = employeeService.create(testEmployeeDTO);

        // Assert
        assertNotNull(result);
        assertEquals("John Doe", result.getName());
        verify(employeeRepository, times(1)).findByBadgeIdAndDeletedFalse("EMP001");
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when badge ID exists")
    void testCreate_DuplicateBadgeId() {
        // Arrange
        when(employeeRepository.findByBadgeIdAndDeletedFalse("EMP001"))
            .thenReturn(Optional.of(testEmployee));

        // Act & Assert
        assertThrows(DuplicateResourceException.class, () -> employeeService.create(testEmployeeDTO));
        verify(employeeRepository, times(1)).findByBadgeIdAndDeletedFalse("EMP001");
        verify(employeeRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should update employee successfully")
    void testUpdate_Success() {
        // Arrange
        EmployeeDTO updateDTO = EmployeeDTO.builder()
            .name("John Updated")
            .department("Receiving")
            .build();

        when(employeeRepository.findActiveById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);
        when(employeeMapper.toDto(testEmployee)).thenReturn(testEmployeeDTO);

        // Act
        EmployeeDTO result = employeeService.update(1L, updateDTO);

        // Assert
        assertNotNull(result);
        verify(employeeRepository, times(1)).findActiveById(1L);
        verify(employeeMapper, times(1)).updateEntityFromDto(updateDTO, testEmployee);
        verify(employeeRepository, times(1)).save(testEmployee);
    }

    @Test
    @DisplayName("Should soft delete employee successfully")
    void testDelete_Success() {
        // Arrange
        when(employeeRepository.findActiveById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        employeeService.delete(1L);

        // Assert
        verify(employeeRepository, times(1)).findActiveById(1L);
        verify(employeeRepository, times(1)).save(testEmployee);
        assertTrue(testEmployee.getDeleted());
        assertEquals("TERMINATED", testEmployee.getStatus());
    }

    @Test
    @DisplayName("Should get employees by department")
    void testGetByDepartment_Success() {
        // Arrange
        Page<Employee> employeePage = new PageImpl<>(List.of(testEmployee));
        when(employeeRepository.findByDepartmentAndDeletedFalse("Shipping", Pageable.unpaged()))
            .thenReturn(employeePage);
        when(employeeMapper.toDtoList(anyList())).thenReturn(List.of(testEmployeeDTO));

        // Act
        List<EmployeeDTO> result = employeeService.getByDepartment("Shipping");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(employeeRepository, times(1)).findByDepartmentAndDeletedFalse("Shipping", Pageable.unpaged());
    }

    @Test
    @DisplayName("Should count employees by department")
    void testCountByDepartment_Success() {
        // Arrange
        when(employeeRepository.countByDepartmentAndDeletedFalse("Shipping")).thenReturn(5L);

        // Act
        long count = employeeService.countByDepartment("Shipping");

        // Assert
        assertEquals(5L, count);
        verify(employeeRepository, times(1)).countByDepartmentAndDeletedFalse("Shipping");
    }

    @Test
    @DisplayName("Should get employee by badge ID successfully")
    void testGetByBadgeId_Success() {
        // Arrange
        when(employeeRepository.findByBadgeIdAndDeletedFalse("EMP001"))
            .thenReturn(Optional.of(testEmployee));
        when(employeeMapper.toDto(testEmployee)).thenReturn(testEmployeeDTO);

        // Act
        EmployeeDTO result = employeeService.getByBadgeId("EMP001");

        // Assert
        assertNotNull(result);
        assertEquals("EMP001", result.getBadgeId());
        verify(employeeRepository, times(1)).findByBadgeIdAndDeletedFalse("EMP001");
    }
}