package com.example.warehouseems.service;

import com.example.warehouseems.dto.EmployeeRequest;
import com.example.warehouseems.exception.EntityNotFoundException;
import com.example.warehouseems.model.Employee;
import com.example.warehouseems.repository.EmployeeRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {
    @Mock
    private EmployeeRepository employeeRepository;
    @InjectMocks
    private EmployeeService employeeService;

    private Employee employee;
    private EmployeeRequest employeeRequest;

    @BeforeEach
    void setUp() {
        employee = Employee.builder()
                .id(1L)
                .name("John Doe")
                .email("john.doe@example.com")
                .badgeId("BADGE123")
                .department("Logistics")
                .deleted(false)
                .build();
        employeeRequest = EmployeeRequest.builder()
                .name("John Doe")
                .email("john.doe@example.com")
                .badgeId("BADGE123")
                .department("Logistics")
                .build();
    }

    @AfterEach
    void tearDown() {
        // Clean up if necessary
    }

    @Test
    @DisplayName("createEmployee_Normal_Success")
    void testCreateEmployee_Normal_Success() {
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);
        Employee result = employeeService.createEmployee(employeeRequest);
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(employeeRequest.getName());
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    @DisplayName("createEmployee_DuplicateBadgeId_DataIntegrityViolationException")
    void testCreateEmployee_DuplicateBadgeId_DataIntegrityViolationException() {
        when(employeeRepository.save(any(Employee.class))).thenThrow(new DataIntegrityViolationException("Duplicate badgeId"));
        assertThrows(DataIntegrityViolationException.class, () -> employeeService.createEmployee(employeeRequest));
    }

    @Test
    @DisplayName("createEmployee_InvalidEmail_IllegalArgumentException")
    void testCreateEmployee_InvalidEmail_IllegalArgumentException() {
        employeeRequest.setEmail("invalid-email");
        assertThrows(IllegalArgumentException.class, () -> employeeService.createEmployee(employeeRequest));
    }

    @Test
    @DisplayName("getEmployeeById_ExistingId_Success")
    void testGetEmployeeById_ExistingId_Success() {
        when(employeeRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(employee));
        Employee result = employeeService.getEmployeeById(1L);
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getEmployeeById_NonExistingId_EntityNotFoundException")
    void testGetEmployeeById_NonExistingId_EntityNotFoundException() {
        when(employeeRepository.findByIdAndDeletedFalse(2L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> employeeService.getEmployeeById(2L));
    }

    @Test
    @DisplayName("getAllEmployees_Normal_Success")
    void testGetAllEmployees_Normal_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Employee> employees = Arrays.asList(employee);
        Page<Employee> page = new PageImpl<>(employees);
        when(employeeRepository.findAllByDeletedFalse(pageable)).thenReturn(page);
        Page<Employee> result = employeeService.getAllEmployees(pageable, Collections.emptyMap());
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("getAllEmployees_EmptyResult_EmptyPage")
    void testGetAllEmployees_EmptyResult_EmptyPage() {
        Pageable pageable = PageRequest.of(0, 10);
        when(employeeRepository.findAllByDeletedFalse(pageable)).thenReturn(Page.empty());
        Page<Employee> result = employeeService.getAllEmployees(pageable, Collections.emptyMap());
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("updateEmployee_ExistingId_Success")
    void testUpdateEmployee_ExistingId_Success() {
        when(employeeRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);
        EmployeeRequest updateRequest = EmployeeRequest.builder().name("Jane Doe").email("jane.doe@example.com").badgeId("BADGE124").department("Packing").build();
        Employee result = employeeService.updateEmployee(1L, updateRequest);
        assertThat(result.getName()).isEqualTo("Jane Doe");
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    @DisplayName("updateEmployee_NonExistingId_EntityNotFoundException")
    void testUpdateEmployee_NonExistingId_EntityNotFoundException() {
        when(employeeRepository.findByIdAndDeletedFalse(2L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> employeeService.updateEmployee(2L, employeeRequest));
    }

    @Test
    @DisplayName("deleteEmployee_ExistingId_Success")
    void testDeleteEmployee_ExistingId_Success() {
        when(employeeRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(employee));
        employeeService.deleteEmployee(1L);
        assertThat(employee.isDeleted()).isTrue();
        verify(employeeRepository, times(1)).save(employee);
    }

    @Test
    @DisplayName("deleteEmployee_NonExistingId_EntityNotFoundException")
    void testDeleteEmployee_NonExistingId_EntityNotFoundException() {
        when(employeeRepository.findByIdAndDeletedFalse(2L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> employeeService.deleteEmployee(2L));
    }

    @Test
    @DisplayName("findByBadgeId_ExistingBadgeId_Success")
    void testFindByBadgeId_ExistingBadgeId_Success() {
        when(employeeRepository.findByBadgeIdAndDeletedFalse("BADGE123")).thenReturn(Optional.of(employee));
        Employee result = employeeService.findByBadgeId("BADGE123");
        assertThat(result).isNotNull();
        assertThat(result.getBadgeId()).isEqualTo("BADGE123");
    }

    @Test
    @DisplayName("findByBadgeId_NonExistingBadgeId_EntityNotFoundException")
    void testFindByBadgeId_NonExistingBadgeId_EntityNotFoundException() {
        when(employeeRepository.findByBadgeIdAndDeletedFalse("BADGE999")).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> employeeService.findByBadgeId("BADGE999"));
    }

    @Test
    @DisplayName("createEmployee_SQLInjectionAttempt_IllegalArgumentException")
    void testCreateEmployee_SQLInjectionAttempt_IllegalArgumentException() {
        employeeRequest.setName("Robert'); DROP TABLE Employees;--");
        assertThrows(IllegalArgumentException.class, () -> employeeService.createEmployee(employeeRequest));
    }

    @Test
    @DisplayName("createEmployee_XSSAttempt_IllegalArgumentException")
    void testCreateEmployee_XSSAttempt_IllegalArgumentException() {
        employeeRequest.setName("<script>alert('xss')</script>");
        assertThrows(IllegalArgumentException.class, () -> employeeService.createEmployee(employeeRequest));
    }

    @Test
    @DisplayName("createEmployee_NullRequest_IllegalArgumentException")
    void testCreateEmployee_NullRequest_IllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> employeeService.createEmployee(null));
    }

    @Test
    @DisplayName("createEmployee_EmptyFields_IllegalArgumentException")
    void testCreateEmployee_EmptyFields_IllegalArgumentException() {
        employeeRequest.setName("");
        employeeRequest.setEmail("");
        employeeRequest.setBadgeId("");
        assertThrows(IllegalArgumentException.class, () -> employeeService.createEmployee(employeeRequest));
    }
}
