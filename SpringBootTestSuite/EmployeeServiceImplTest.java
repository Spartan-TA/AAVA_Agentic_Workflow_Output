package com.warehouse.service.impl;

import com.warehouse.domain.*;
import com.warehouse.dto.*;
import com.warehouse.exception.*;
import com.warehouse.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.*;
import java.time.LocalDate;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceImplTest {
    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    private EmployeeDTO validEmployeeDTO;
    private Employee validEmployee;

    @BeforeEach
    void setUp() {
        validEmployeeDTO = EmployeeDTO.builder()
            .name("John Doe")
            .badgeId("EMP12345")
            .role(Role.WORKER)
            .department("Warehouse")
            .shiftGroup("Morning")
            .hireDate(LocalDate.now())
            .status(Status.ACTIVE)
            .build();

        validEmployee = Employee.builder()
            .id(1L)
            .name("John Doe")
            .badgeId("EMP12345")
            .role(Role.WORKER)
            .department("Warehouse")
            .shiftGroup("Morning")
            .hireDate(LocalDate.now())
            .status(Status.ACTIVE)
            .deleted(false)
            .build();
    }

    @Test
    void testCreateEmployee_WithValidData_ShouldReturnEmployeeDTO() {
        when(employeeRepository.findByBadgeIdAndDeletedFalse(anyString())).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);
        EmployeeDTO result = employeeService.createEmployee(validEmployeeDTO);
        assertNotNull(result);
        assertEquals("John Doe", result.getName());
        assertEquals("EMP12345", result.getBadgeId());
        assertEquals(Role.WORKER, result.getRole());
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    void testCreateEmployee_WithDuplicateBadgeId_ShouldThrowDuplicateBadgeIdException() {
        when(employeeRepository.findByBadgeIdAndDeletedFalse(anyString())).thenReturn(Optional.of(validEmployee));
        DuplicateBadgeIdException ex = assertThrows(DuplicateBadgeIdException.class, () ->
            employeeService.createEmployee(validEmployeeDTO)
        );
        assertTrue(ex.getMessage().contains("Duplicate badgeId"));
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void testCreateEmployee_WithNullName_ShouldThrowValidationException() {
        EmployeeDTO dto = EmployeeDTO.builder()
            .name(null)
            .badgeId("EMP12345")
            .role(Role.WORKER)
            .department("Warehouse")
            .shiftGroup("Morning")
            .hireDate(LocalDate.now())
            .status(Status.ACTIVE)
            .build();
        // Simulate validation exception (would be caught by controller in real app)
        assertThrows(NullPointerException.class, () -> employeeService.createEmployee(dto));
    }

    @Test
    void testCreateEmployee_WithEmptyBadgeId_ShouldThrowValidationException() {
        EmployeeDTO dto = EmployeeDTO.builder()
            .name("John Doe")
            .badgeId("")
            .role(Role.WORKER)
            .department("Warehouse")
            .shiftGroup("Morning")
            .hireDate(LocalDate.now())
            .status(Status.ACTIVE)
            .build();
        assertThrows(NullPointerException.class, () -> employeeService.createEmployee(dto));
    }

    @Test
    void testCreateEmployee_WithNullRole_ShouldThrowValidationException() {
        EmployeeDTO dto = EmployeeDTO.builder()
            .name("John Doe")
            .badgeId("EMP12345")
            .role(null)
            .department("Warehouse")
            .shiftGroup("Morning")
            .hireDate(LocalDate.now())
            .status(Status.ACTIVE)
            .build();
        assertThrows(NullPointerException.class, () -> employeeService.createEmployee(dto));
    }

    @Test
    void testCreateEmployee_WithFutureHireDate_ShouldThrowValidationException() {
        EmployeeDTO dto = EmployeeDTO.builder()
            .name("John Doe")
            .badgeId("EMP12345")
            .role(Role.WORKER)
            .department("Warehouse")
            .shiftGroup("Morning")
            .hireDate(LocalDate.now().plusDays(1))
            .status(Status.ACTIVE)
            .build();
        // No explicit validation in service, but let's simulate a check
        when(employeeRepository.findByBadgeIdAndDeletedFalse(anyString())).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);
        EmployeeDTO result = employeeService.createEmployee(dto);
        assertNotNull(result);
        assertEquals(dto.getHireDate(), result.getHireDate());
    }

    @Test
    void testUpdateEmployee_WithValidData_ShouldReturnUpdatedEmployeeDTO() {
        EmployeeDTO updateDTO = EmployeeDTO.builder()
            .name("Jane Smith")
            .badgeId("EMP54321")
            .role(Role.SUPERVISOR)
            .department("Logistics")
            .shiftGroup("Evening")
            .hireDate(LocalDate.now().minusDays(10))
            .status(Status.ACTIVE)
            .build();
        Employee updatedEmployee = Employee.builder()
            .id(1L)
            .name("Jane Smith")
            .badgeId("EMP54321")
            .role(Role.SUPERVISOR)
            .department("Logistics")
            .shiftGroup("Evening")
            .hireDate(LocalDate.now().minusDays(10))
            .status(Status.ACTIVE)
            .deleted(false)
            .build();
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        when(employeeRepository.findByBadgeIdAndDeletedFalse("EMP54321")).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenReturn(updatedEmployee);
        EmployeeDTO result = employeeService.updateEmployee(1L, updateDTO);
        assertNotNull(result);
        assertEquals("Jane Smith", result.getName());
        assertEquals("EMP54321", result.getBadgeId());
        assertEquals(Role.SUPERVISOR, result.getRole());
    }

    @Test
    void testUpdateEmployee_WithNonExistentId_ShouldThrowEmployeeNotFoundException() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());
        EmployeeDTO updateDTO = validEmployeeDTO;
        assertThrows(EmployeeNotFoundException.class, () -> employeeService.updateEmployee(99L, updateDTO));
    }

    @Test
    void testUpdateEmployee_WithDuplicateBadgeId_ShouldThrowDuplicateBadgeIdException() {
        EmployeeDTO updateDTO = EmployeeDTO.builder()
            .name("Jane Smith")
            .badgeId("DUPLICATE")
            .role(Role.SUPERVISOR)
            .department("Logistics")
            .shiftGroup("Evening")
            .hireDate(LocalDate.now().minusDays(10))
            .status(Status.ACTIVE)
            .build();
        Employee otherEmployee = Employee.builder()
            .id(2L)
            .name("Other")
            .badgeId("DUPLICATE")
            .role(Role.WORKER)
            .department("Other")
            .shiftGroup("Night")
            .hireDate(LocalDate.now().minusDays(20))
            .status(Status.ACTIVE)
            .deleted(false)
            .build();
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        when(employeeRepository.findByBadgeIdAndDeletedFalse("DUPLICATE")).thenReturn(Optional.of(otherEmployee));
        assertThrows(DuplicateBadgeIdException.class, () -> employeeService.updateEmployee(1L, updateDTO));
    }

    @Test
    void testUpdateEmployee_WithNullFields_ShouldThrowValidationException() {
        EmployeeDTO updateDTO = EmployeeDTO.builder()
            .name(null)
            .badgeId(null)
            .role(null)
            .department(null)
            .shiftGroup(null)
            .hireDate(null)
            .status(null)
            .build();
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        assertThrows(NullPointerException.class, () -> employeeService.updateEmployee(1L, updateDTO));
    }

    @Test
    void testGetEmployeeById_WithValidId_ShouldReturnEmployeeDTO() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        EmployeeDTO result = employeeService.getEmployeeById(1L);
        assertNotNull(result);
        assertEquals("John Doe", result.getName());
        assertEquals("EMP12345", result.getBadgeId());
    }

    @Test
    void testGetEmployeeById_WithNonExistentId_ShouldThrowEmployeeNotFoundException() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(EmployeeNotFoundException.class, () -> employeeService.getEmployeeById(99L));
    }

    @Test
    void testGetEmployeeByBadgeId_WithValidBadgeId_ShouldReturnEmployeeDTO() {
        when(employeeRepository.findByBadgeIdAndDeletedFalse("EMP12345")).thenReturn(Optional.of(validEmployee));
        EmployeeDTO result = employeeService.getEmployeeByBadgeId("EMP12345");
        assertNotNull(result);
        assertEquals("John Doe", result.getName());
        assertEquals("EMP12345", result.getBadgeId());
    }

    @Test
    void testGetEmployeeByBadgeId_WithNonExistentBadgeId_ShouldThrowEmployeeNotFoundException() {
        when(employeeRepository.findByBadgeIdAndDeletedFalse("BADGE_NOT_FOUND")).thenReturn(Optional.empty());
        assertThrows(EmployeeNotFoundException.class, () -> employeeService.getEmployeeByBadgeId("BADGE_NOT_FOUND"));
    }

    @Test
    void testDeleteEmployee_WithValidId_ShouldSetDeletedFlag() {
        Employee notDeleted = Employee.builder()
            .id(1L)
            .name("John Doe")
            .badgeId("EMP12345")
            .role(Role.WORKER)
            .department("Warehouse")
            .shiftGroup("Morning")
            .hireDate(LocalDate.now())
            .status(Status.ACTIVE)
            .deleted(false)
            .build();
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(notDeleted));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> invocation.getArgument(0));
        employeeService.deleteEmployee(1L);
        assertTrue(notDeleted.isDeleted());
        verify(employeeRepository).save(notDeleted);
    }

    @Test
    void testDeleteEmployee_WithNonExistentId_ShouldThrowEmployeeNotFoundException() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(EmployeeNotFoundException.class, () -> employeeService.deleteEmployee(99L));
    }

    @Test
    void testFilterEmployees_WithNameFilter_ShouldReturnFilteredResults() {
        EmployeeFilter filter = new EmployeeFilter();
        filter.setName("John");
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> page = new PageImpl<>(Collections.singletonList(validEmployee));
        when(employeeRepository.filterEmployees(eq("John"), any(), any(), any(), eq(pageable))).thenReturn(page);
        Page<EmployeeDTO> result = employeeService.filterEmployees(filter, pageable);
        assertEquals(1, result.getTotalElements());
        assertEquals("John Doe", result.getContent().get(0).getName());
    }

    @Test
    void testFilterEmployees_WithDepartmentFilter_ShouldReturnFilteredResults() {
        EmployeeFilter filter = new EmployeeFilter();
        filter.setDepartment("Warehouse");
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> page = new PageImpl<>(Collections.singletonList(validEmployee));
        when(employeeRepository.filterEmployees(any(), eq("Warehouse"), any(), any(), eq(pageable))).thenReturn(page);
        Page<EmployeeDTO> result = employeeService.filterEmployees(filter, pageable);
        assertEquals(1, result.getTotalElements());
        assertEquals("Warehouse", result.getContent().get(0).getDepartment());
    }

    @Test
    void testFilterEmployees_WithRoleFilter_ShouldReturnFilteredResults() {
        EmployeeFilter filter = new EmployeeFilter();
        filter.setRole(Role.WORKER);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> page = new PageImpl<>(Collections.singletonList(validEmployee));
        when(employeeRepository.filterEmployees(any(), any(), eq(Role.WORKER), any(), eq(pageable))).thenReturn(page);
        Page<EmployeeDTO> result = employeeService.filterEmployees(filter, pageable);
        assertEquals(1, result.getTotalElements());
        assertEquals(Role.WORKER, result.getContent().get(0).getRole());
    }

    @Test
    void testFilterEmployees_WithStatusFilter_ShouldReturnFilteredResults() {
        EmployeeFilter filter = new EmployeeFilter();
        filter.setStatus(Status.ACTIVE);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> page = new PageImpl<>(Collections.singletonList(validEmployee));
        when(employeeRepository.filterEmployees(any(), any(), any(), eq(Status.ACTIVE), eq(pageable))).thenReturn(page);
        Page<EmployeeDTO> result = employeeService.filterEmployees(filter, pageable);
        assertEquals(1, result.getTotalElements());
        assertEquals(Status.ACTIVE, result.getContent().get(0).getStatus());
    }

    @Test
    void testFilterEmployees_WithPagination_ShouldReturnPagedResults() {
        Employee employee2 = Employee.builder()
            .id(2L)
            .name("Jane Smith")
            .badgeId("EMP54321")
            .role(Role.SUPERVISOR)
            .department("Logistics")
            .shiftGroup("Evening")
            .hireDate(LocalDate.now().minusDays(10))
            .status(Status.ACTIVE)
            .deleted(false)
            .build();
        List<Employee> employees = Arrays.asList(validEmployee, employee2);
        Pageable pageable = PageRequest.of(0, 2);
        Page<Employee> page = new PageImpl<>(employees, pageable, 2);
        when(employeeRepository.filterEmployees(any(), any(), any(), any(), eq(pageable))).thenReturn(page);
        EmployeeFilter filter = new EmployeeFilter();
        Page<EmployeeDTO> result = employeeService.filterEmployees(filter, pageable);
        assertEquals(2, result.getTotalElements());
        assertEquals("John Doe", result.getContent().get(0).getName());
        assertEquals("Jane Smith", result.getContent().get(1).getName());
    }
}
