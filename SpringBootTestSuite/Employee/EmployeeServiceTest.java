package com.example.warehouse.employee;

import com.example.warehouse.employee.dto.EmployeeDTO;
import com.example.warehouse.employee.entity.Employee;
import com.example.warehouse.employee.exception.ResourceNotFoundException;
import com.example.warehouse.employee.repository.EmployeeRepository;
import com.example.warehouse.employee.service.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class EmployeeServiceTest {
    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeService employeeService;

    private EmployeeDTO validDto;
    private Employee validEmployee;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        validDto = new EmployeeDTO();
        validDto.setName("John Doe");
        validDto.setBadgeId("BADGE123");
        validDto.setRole("WORKER");
        validDto.setDepartment("Shipping");
        validDto.setShiftGroup("A");
        validDto.setHireDate(LocalDate.now());
        validDto.setStatus("ACTIVE");

        validEmployee = new Employee();
        validEmployee.setId(1L);
        validEmployee.setName(validDto.getName());
        validEmployee.setBadgeId(validDto.getBadgeId());
        validEmployee.setRole(validDto.getRole());
        validEmployee.setDepartment(validDto.getDepartment());
        validEmployee.setShiftGroup(validDto.getShiftGroup());
        validEmployee.setHireDate(validDto.getHireDate());
        validEmployee.setStatus(validDto.getStatus());
    }

    @Test
    public void testCreateEmployee_ValidInput_ReturnsEmployee() {
        when(employeeRepository.existsByBadgeId(validDto.getBadgeId())).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);

        Employee result = employeeService.createEmployee(validDto);
        assertNotNull(result);
        assertEquals(validDto.getBadgeId(), result.getBadgeId());
    }

    @Test
    public void testCreateEmployee_DuplicateBadgeId_ThrowsException() {
        when(employeeRepository.existsByBadgeId(validDto.getBadgeId())).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> employeeService.createEmployee(validDto));
    }

    @Test
    public void testCreateEmployee_NullFields_ThrowsException() {
        EmployeeDTO dto = new EmployeeDTO();
        assertThrows(IllegalArgumentException.class, () -> employeeService.createEmployee(dto));
    }

    @Test
    public void testGetEmployeeById_ValidId_ReturnsEmployee() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        Employee result = employeeService.getEmployeeById(1L);
        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    public void testGetEmployeeById_InvalidId_ThrowsResourceNotFoundException() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> employeeService.getEmployeeById(99L));
    }

    @Test
    public void testGetAllEmployees_ReturnsPageOfEmployees() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> page = new PageImpl<>(Arrays.asList(validEmployee));
        when(employeeRepository.findByStatus(eq("ACTIVE"), eq(pageable))).thenReturn(page);
        Page<Employee> result = employeeService.getAllEmployees(pageable);
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    public void testUpdateEmployee_ValidInput_ReturnsUpdatedEmployee() {
        EmployeeDTO updateDto = new EmployeeDTO();
        updateDto.setName("Jane Doe");
        updateDto.setBadgeId("BADGE123");
        updateDto.setRole("SUPERVISOR");
        updateDto.setDepartment("Receiving");
        updateDto.setShiftGroup("B");
        updateDto.setHireDate(LocalDate.now());
        updateDto.setStatus("ACTIVE");

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        when(employeeRepository.existsByBadgeId(updateDto.getBadgeId())).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);

        Employee result = employeeService.updateEmployee(1L, updateDto);
        assertNotNull(result);
        assertEquals("Jane Doe", result.getName());
    }

    @Test
    public void testUpdateEmployee_ChangeToDuplicateBadgeId_ThrowsException() {
        EmployeeDTO updateDto = new EmployeeDTO();
        updateDto.setBadgeId("DUPLICATE_BADGE");
        updateDto.setName("Jane Doe");
        updateDto.setRole("SUPERVISOR");
        updateDto.setDepartment("Receiving");
        updateDto.setShiftGroup("B");
        updateDto.setHireDate(LocalDate.now());
        updateDto.setStatus("ACTIVE");

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        when(employeeRepository.existsByBadgeId(updateDto.getBadgeId())).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> employeeService.updateEmployee(1L, updateDto));
    }

    @Test
    public void testUpdateEmployee_InvalidId_ThrowsResourceNotFoundException() {
        EmployeeDTO updateDto = new EmployeeDTO();
        updateDto.setBadgeId("BADGE123");
        updateDto.setName("Jane Doe");
        updateDto.setRole("SUPERVISOR");
        updateDto.setDepartment("Receiving");
        updateDto.setShiftGroup("B");
        updateDto.setHireDate(LocalDate.now());
        updateDto.setStatus("ACTIVE");

        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> employeeService.updateEmployee(99L, updateDto));
    }

    @Test
    public void testSoftDeleteEmployee_ValidId_SetsStatusInactive() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        validEmployee.setStatus("ACTIVE");
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);
        employeeService.softDeleteEmployee(1L);
        verify(employeeRepository, times(1)).save(argThat(emp -> "INACTIVE".equals(emp.getStatus())));
    }

    @Test
    public void testSoftDeleteEmployee_InvalidId_ThrowsResourceNotFoundException() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> employeeService.softDeleteEmployee(99L));
    }
}