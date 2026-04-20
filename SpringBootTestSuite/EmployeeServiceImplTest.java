package com.warehouse.management.employee.service;

import com.warehouse.management.employee.domain.Employee;
import com.warehouse.management.employee.dto.EmployeeRequestDTO;
import com.warehouse.management.employee.dto.EmployeeResponseDTO;
import com.warehouse.management.employee.repository.EmployeeRepository;
import com.warehouse.management.exception.DuplicateBadgeIdException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.data.domain.*;
import javax.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class EmployeeServiceImplTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    private Employee employee;
    private EmployeeRequestDTO requestDTO;
    private EmployeeResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        employee = Employee.builder()
                .id(1L)
                .name("John Doe")
                .badgeId("BADGE123")
                .role("WORKER")
                .department("Logistics")
                .shiftGroup("A")
                .hireDate(LocalDate.of(2020, 1, 1))
                .status("ACTIVE")
                .deleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        requestDTO = EmployeeRequestDTO.builder()
                .name("John Doe")
                .badgeId("BADGE123")
                .role("WORKER")
                .department("Logistics")
                .shiftGroup("A")
                .hireDate(LocalDate.of(2020, 1, 1))
                .status("ACTIVE")
                .build();

        responseDTO = EmployeeResponseDTO.builder()
                .id(1L)
                .name("John Doe")
                .badgeId("BADGE123")
                .role("WORKER")
                .department("Logistics")
                .shiftGroup("A")
                .hireDate(LocalDate.of(2020, 1, 1))
                .status("ACTIVE")
                .deleted(false)
                .createdAt(employee.getCreatedAt())
                .updatedAt(employee.getUpdatedAt())
                .build();
    }

    @Test
    void testCreateEmployee_WithValidData_ReturnsCreatedEmployee() {
        // Arrange
        when(employeeRepository.findByBadgeId("BADGE123")).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        // Act
        EmployeeResponseDTO result = employeeService.createEmployee(requestDTO);

        // Assert
        assertNotNull(result);
        assertEquals("John Doe", result.getName());
        verify(employeeRepository).save(any(Employee.class));
    }

    @Test
    void testCreateEmployee_WithDuplicateBadgeId_ThrowsException() {
        // Arrange
        when(employeeRepository.findByBadgeId("BADGE123")).thenReturn(Optional.of(employee));

        // Act & Assert
        assertThrows(DuplicateBadgeIdException.class, () -> employeeService.createEmployee(requestDTO));
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void testCreateEmployee_WithNullFields_ThrowsException() {
        // Arrange
        EmployeeRequestDTO invalidRequest = EmployeeRequestDTO.builder()
                .name(null)
                .badgeId(null)
                .role(null)
                .build();

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> employeeService.createEmployee(invalidRequest));
    }

    @Test
    void testGetEmployeeById_WithValidId_ReturnsEmployee() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        // Act
        EmployeeResponseDTO result = employeeService.getEmployeeById(1L);

        // Assert
        assertNotNull(result);
        assertEquals("John Doe", result.getName());
    }

    @Test
    void testGetEmployeeById_WithNonExistentId_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(2L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> employeeService.getEmployeeById(2L));
    }

    @Test
    void testGetAllEmployees_WithPagination_ReturnsPage() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<Employee> employees = Collections.singletonList(employee);
        Page<Employee> page = new PageImpl<>(employees, pageable, 1);
        when(employeeRepository.findByDeletedFalse(pageable)).thenReturn(page);

        // Act
        Page<EmployeeResponseDTO> result = employeeService.getAllEmployees(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void testUpdateEmployee_WithValidData_ReturnsUpdatedEmployee() {
        // Arrange
        EmployeeRequestDTO updateRequest = EmployeeRequestDTO.builder()
                .name("Jane Doe")
                .badgeId("BADGE123")
                .role("SUPERVISOR")
                .department("Packing")
                .shiftGroup("B")
                .hireDate(LocalDate.of(2021, 2, 2))
                .status("ACTIVE")
                .build();

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.findByBadgeId("BADGE123")).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        // Act
        EmployeeResponseDTO result = employeeService.updateEmployee(1L, updateRequest);

        // Assert
        assertNotNull(result);
        assertEquals("Jane Doe", result.getName());
    }

    @Test
    void testUpdateEmployee_WithNonExistentId_ThrowsException() {
        // Arrange
        EmployeeRequestDTO updateRequest = EmployeeRequestDTO.builder()
                .name("Jane Doe")
                .badgeId("BADGE123")
                .role("SUPERVISOR")
                .build();

        when(employeeRepository.findById(2L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> employeeService.updateEmployee(2L, updateRequest));
    }

    @Test
    void testPatchEmployee_WithPartialUpdates_ReturnsPatchedEmployee() {
        // Arrange
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", "Patched Name");
        updates.put("department", "Receiving");

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        // Act
        EmployeeResponseDTO result = employeeService.patchEmployee(1L, updates);

        // Assert
        assertNotNull(result);
        assertEquals("Patched Name", result.getName());
        assertEquals("Receiving", result.getDepartment());
    }

    @Test
    void testDeleteEmployee_SetsDeletedTrue() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        // Act
        employeeService.deleteEmployee(1L);

        // Assert
        assertTrue(employee.getDeleted());
        verify(employeeRepository).save(employee);
    }

    @Test
    void testGetEmployeeByBadgeId_WithValidBadgeId_ReturnsEmployee() {
        // Arrange
        when(employeeRepository.findByBadgeId("BADGE123")).thenReturn(Optional.of(employee));

        // Act
        EmployeeResponseDTO result = employeeService.getEmployeeByBadgeId("BADGE123");

        // Assert
        assertNotNull(result);
        assertEquals("John Doe", result.getName());
    }

    @Test
    void testGetEmployeeByBadgeId_WithNonExistentBadgeId_ThrowsException() {
        // Arrange
        when(employeeRepository.findByBadgeId("BADGE999")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> employeeService.getEmployeeByBadgeId("BADGE999"));
    }
}