package com.companyname.wems.employee.service;

import com.companyname.wems.employee.model.Employee;
import com.companyname.wems.employee.model.EmployeeStatus;
import com.companyname.wems.employee.repository.EmployeeRepository;
import com.companyname.wems.employee.dto.CreateEmployeeRequest;
import com.companyname.wems.employee.dto.UpdateEmployeeRequest;
import com.companyname.wems.employee.dto.EmployeeDTO;
import com.companyname.wems.exception.ResourceNotFoundException;
import com.companyname.wems.exception.ValidationException;
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

import java.time.LocalDateTime;
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

    private Employee testEmployee;
    private CreateEmployeeRequest validRequest;
    private UpdateEmployeeRequest updateRequest;

    @BeforeEach
    void setUp() {
        testEmployee = Employee.builder()
            .id(1L)
            .badgeId("EMP001")
            .firstName("John")
            .lastName("Doe")
            .email("john.doe@example.com")
            .phone("1234567890")
            .department("Warehouse")
            .status(EmployeeStatus.ACTIVE)
            .deleted(false)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        validRequest = new CreateEmployeeRequest();
        validRequest.setBadgeId("EMP001");
        validRequest.setFirstName("John");
        validRequest.setLastName("Doe");
        validRequest.setEmail("john.doe@example.com");
        validRequest.setPhone("1234567890");
        validRequest.setDepartment("Warehouse");

        updateRequest = new UpdateEmployeeRequest();
        updateRequest.setFirstName("Jane");
        updateRequest.setLastName("Smith");
        updateRequest.setEmail("jane.smith@example.com");
        updateRequest.setPhone("0987654321");
        updateRequest.setDepartment("Logistics");
        updateRequest.setStatus(EmployeeStatus.INACTIVE);
    }

    @Test
    void testCreateEmployee_ValidInput_Success() {
        when(employeeRepository.existsByBadgeId("EMP001")).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        EmployeeDTO result = employeeService.createEmployee(validRequest);

        assertNotNull(result);
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        assertEquals("EMP001", result.getBadgeId());
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    void testCreateEmployee_NullRequest_ThrowsValidationException() {
        assertThrows(ValidationException.class, () -> employeeService.createEmployee(null));
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void testCreateEmployee_EmptyFirstName_ThrowsValidationException() {
        validRequest.setFirstName("");
        assertThrows(ValidationException.class, () -> employeeService.createEmployee(validRequest));
    }

    @Test
    void testCreateEmployee_NullFirstName_ThrowsValidationException() {
        validRequest.setFirstName(null);
        assertThrows(ValidationException.class, () -> employeeService.createEmployee(validRequest));
    }

    @Test
    void testCreateEmployee_DuplicateBadgeId_ThrowsValidationException() {
        when(employeeRepository.existsByBadgeId("EMP001")).thenReturn(true);
        assertThrows(ValidationException.class, () -> employeeService.createEmployee(validRequest));
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void testCreateEmployee_InvalidEmail_ThrowsValidationException() {
        validRequest.setEmail("invalid-email");
        assertThrows(ValidationException.class, () -> employeeService.createEmployee(validRequest));
    }

    @Test
    void testGetEmployeeById_ValidId_Success() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        EmployeeDTO result = employeeService.getEmployeeById(1L);
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("John", result.getFirstName());
    }

    @Test
    void testGetEmployeeById_InvalidId_ThrowsResourceNotFoundException() {
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> employeeService.getEmployeeById(999L));
    }

    @Test
    void testGetEmployeeById_NullId_ThrowsValidationException() {
        assertThrows(ValidationException.class, () -> employeeService.getEmployeeById(null));
    }

    @Test
    void testGetAllEmployees_Pagination_Success() {
        List<Employee> employees = Arrays.asList(testEmployee);
        Page<Employee> page = new PageImpl<>(employees);
        Pageable pageable = PageRequest.of(0, 10);
        when(employeeRepository.findAll(pageable)).thenReturn(page);
        Page<EmployeeDTO> result = employeeService.getAllEmployees(pageable);
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void testUpdateEmployee_ValidInput_Success() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);
        EmployeeDTO result = employeeService.updateEmployee(1L, updateRequest);
        assertNotNull(result);
        assertEquals("Jane", result.getFirstName());
        assertEquals("Smith", result.getLastName());
    }

    @Test
    void testUpdateEmployee_InvalidId_ThrowsResourceNotFoundException() {
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> employeeService.updateEmployee(999L, updateRequest));
    }

    @Test
    void testUpdateEmployee_NullRequest_ThrowsValidationException() {
        assertThrows(ValidationException.class, () -> employeeService.updateEmployee(1L, null));
    }

    @Test
    void testDeleteEmployee_ValidId_Success() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);
        employeeService.deleteEmployee(1L);
        verify(employeeRepository, times(1)).save(any(Employee.class));
        assertTrue(testEmployee.isDeleted());
    }

    @Test
    void testDeleteEmployee_InvalidId_ThrowsResourceNotFoundException() {
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> employeeService.deleteEmployee(999L));
    }

    @Test
    void testGetEmployeesByStatus_ValidStatus_Success() {
        List<Employee> employees = Arrays.asList(testEmployee);
        when(employeeRepository.findByStatus(EmployeeStatus.ACTIVE)).thenReturn(employees);
        List<EmployeeDTO> result = employeeService.getEmployeesByStatus(EmployeeStatus.ACTIVE);
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testGetEmployeesByStatus_NullStatus_ThrowsValidationException() {
        assertThrows(ValidationException.class, () -> employeeService.getEmployeesByStatus(null));
    }

    @Test
    void testGetEmployeesByDepartment_ValidDepartment_Success() {
        List<Employee> employees = Arrays.asList(testEmployee);
        when(employeeRepository.findByDepartment("Warehouse")).thenReturn(employees);
        List<EmployeeDTO> result = employeeService.getEmployeesByDepartment("Warehouse");
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testGetEmployeesByDepartment_EmptyDepartment_ThrowsValidationException() {
        assertThrows(ValidationException.class, () -> employeeService.getEmployeesByDepartment(""));
    }

    @Test
    void testGetEmployeesByDepartment_NullDepartment_ThrowsValidationException() {
        assertThrows(ValidationException.class, () -> employeeService.getEmployeesByDepartment(null));
    }
}
