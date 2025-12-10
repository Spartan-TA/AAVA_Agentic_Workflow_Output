package com.company.warehouse.employee;

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

import jakarta.persistence.EntityNotFoundException;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private EmployeeMapper employeeMapper;

    @InjectMocks
    private EmployeeService employeeService;

    private Employee employee;
    private EmployeeDTO employeeDTO;

    @BeforeEach
    public void setUp() {
        employee = new Employee();
        employee.setId(1L);
        employee.setName("John Doe");
        employee.setBadgeId("ABC123");
        employee.setRole("Worker");
        employee.setDepartment("Logistics");
        employee.setShiftGroup("A");
        employee.setHireDate(LocalDate.now().minusDays(10));
        employee.setStatus("ACTIVE");

        employeeDTO = new EmployeeDTO();
        employeeDTO.setId(1L);
        employeeDTO.setName("John Doe");
        employeeDTO.setBadgeId("ABC123");
        employeeDTO.setRole("Worker");
        employeeDTO.setDepartment("Logistics");
        employeeDTO.setShiftGroup("A");
        employeeDTO.setHireDate(LocalDate.now().minusDays(10));
        employeeDTO.setStatus("ACTIVE");
    }

    @Test
    public void testCreateEmployee_WithValidData_ShouldReturnCreatedEmployee() {
        when(employeeRepository.existsByBadgeId(employeeDTO.getBadgeId())).thenReturn(false);
        when(employeeMapper.toEntity(employeeDTO)).thenReturn(employee);
        when(employeeRepository.save(employee)).thenReturn(employee);
        when(employeeMapper.toDTO(employee)).thenReturn(employeeDTO);

        EmployeeDTO result = employeeService.createEmployee(employeeDTO);
        assertNotNull(result);
        assertEquals(employeeDTO.getBadgeId(), result.getBadgeId());
        verify(employeeRepository).save(employee);
    }

    @Test
    public void testCreateEmployee_WithDuplicateBadgeId_ShouldThrowException() {
        when(employeeRepository.existsByBadgeId(employeeDTO.getBadgeId())).thenReturn(true);
        DuplicateBadgeIdException ex = assertThrows(DuplicateBadgeIdException.class, () -> {
            employeeService.createEmployee(employeeDTO);
        });
        assertTrue(ex.getMessage().contains("Badge ID already exists"));
        verify(employeeRepository, never()).save(any());
    }

    @Test
    public void testCreateEmployee_WithNullDTO_ShouldThrowException() {
        assertThrows(NullPointerException.class, () -> {
            employeeService.createEmployee(null);
        });
    }

    @Test
    public void testGetEmployeeById_WithValidId_ShouldReturnEmployee() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeMapper.toDTO(employee)).thenReturn(employeeDTO);
        EmployeeDTO result = employeeService.getEmployeeById(1L);
        assertNotNull(result);
        assertEquals(employeeDTO.getId(), result.getId());
    }

    @Test
    public void testGetEmployeeById_WithInvalidId_ShouldThrowEntityNotFoundException() {
        when(employeeRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> {
            employeeService.getEmployeeById(2L);
        });
    }

    @Test
    public void testGetAllEmployees_WithPagination_ShouldReturnPagedResults() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> employeePage = new PageImpl<>(Arrays.asList(employee));
        when(employeeRepository.findAll(pageable)).thenReturn(employeePage);
        when(employeeMapper.toDTO(employee)).thenReturn(employeeDTO);
        Page<EmployeeDTO> result = employeeService.getAllEmployees(pageable);
        assertEquals(1, result.getTotalElements());
        assertEquals(employeeDTO.getBadgeId(), result.getContent().get(0).getBadgeId());
    }

    @Test
    public void testGetAllEmployees_WithEmptyDatabase_ShouldReturnEmptyPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> employeePage = new PageImpl<>(Collections.emptyList());
        when(employeeRepository.findAll(pageable)).thenReturn(employeePage);
        Page<EmployeeDTO> result = employeeService.getAllEmployees(pageable);
        assertEquals(0, result.getTotalElements());
    }

    @Test
    public void testUpdateEmployee_WithValidData_ShouldReturnUpdatedEmployee() {
        EmployeeDTO updatedDTO = new EmployeeDTO();
        updatedDTO.setId(1L);
        updatedDTO.setName("Jane Doe");
        updatedDTO.setBadgeId("XYZ789");
        updatedDTO.setRole("Supervisor");
        updatedDTO.setDepartment("Shipping");
        updatedDTO.setShiftGroup("B");
        updatedDTO.setHireDate(LocalDate.now().minusDays(20));
        updatedDTO.setStatus("ACTIVE");

        Employee updatedEmployee = new Employee();
        updatedEmployee.setId(1L);
        updatedEmployee.setName("Jane Doe");
        updatedEmployee.setBadgeId("XYZ789");
        updatedEmployee.setRole("Supervisor");
        updatedEmployee.setDepartment("Shipping");
        updatedEmployee.setShiftGroup("B");
        updatedEmployee.setHireDate(LocalDate.now().minusDays(20));
        updatedEmployee.setStatus("ACTIVE");

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.existsByBadgeId(updatedDTO.getBadgeId())).thenReturn(false);
        doNothing().when(employeeMapper).updateEntityFromDTO(updatedDTO, employee);
        when(employeeRepository.save(employee)).thenReturn(updatedEmployee);
        when(employeeMapper.toDTO(updatedEmployee)).thenReturn(updatedDTO);

        EmployeeDTO result = employeeService.updateEmployee(1L, updatedDTO);
        assertNotNull(result);
        assertEquals("Jane Doe", result.getName());
        assertEquals("XYZ789", result.getBadgeId());
    }

    @Test
    public void testUpdateEmployee_WithDuplicateBadgeId_ShouldThrowException() {
        EmployeeDTO updatedDTO = new EmployeeDTO();
        updatedDTO.setId(1L);
        updatedDTO.setName("Jane Doe");
        updatedDTO.setBadgeId("DUPLICATE");
        updatedDTO.setRole("Supervisor");
        updatedDTO.setDepartment("Shipping");
        updatedDTO.setShiftGroup("B");
        updatedDTO.setHireDate(LocalDate.now().minusDays(20));
        updatedDTO.setStatus("ACTIVE");

        Employee existingEmployee = new Employee();
        existingEmployee.setId(1L);
        existingEmployee.setBadgeId("ORIGINAL");

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(existingEmployee));
        when(employeeRepository.existsByBadgeId(updatedDTO.getBadgeId())).thenReturn(true);

        assertThrows(DuplicateBadgeIdException.class, () -> {
            employeeService.updateEmployee(1L, updatedDTO);
        });
    }

    @Test
    public void testUpdateEmployee_WithInvalidId_ShouldThrowEntityNotFoundException() {
        EmployeeDTO updatedDTO = new EmployeeDTO();
        updatedDTO.setId(99L);
        updatedDTO.setBadgeId("NEWBADGE");
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> {
            employeeService.updateEmployee(99L, updatedDTO);
        });
    }

    @Test
    public void testDeleteEmployee_WithValidId_ShouldDeleteSuccessfully() {
        when(employeeRepository.existsById(1L)).thenReturn(true);
        doNothing().when(employeeRepository).deleteById(1L);
        assertDoesNotThrow(() -> employeeService.deleteEmployee(1L));
        verify(employeeRepository).deleteById(1L);
    }

    @Test
    public void testDeleteEmployee_WithInvalidId_ShouldThrowEntityNotFoundException() {
        when(employeeRepository.existsById(2L)).thenReturn(false);
        assertThrows(EntityNotFoundException.class, () -> {
            employeeService.deleteEmployee(2L);
        });
        verify(employeeRepository, never()).deleteById(anyLong());
    }
}
