package com.example.service;

import com.example.dto.EmployeeDto;
import com.example.entity.Employee;
import com.example.entity.EmployeeStatus;
import com.example.exception.ResourceNotFoundException;
import com.example.repository.EmployeeRepository;
import com.example.repository.AuditLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private EmployeeService employeeService;

    @Captor
    private ArgumentCaptor<Employee> employeeCaptor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createEmployee_validInput_success() {
        EmployeeDto dto = new EmployeeDto("B123", "John Doe", "ENGINEER", "IT", "A", LocalDate.now(), EmployeeStatus.ACTIVE, List.of(), List.of());
        when(employeeRepository.findByBadgeId("B123")).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenAnswer(i -> i.getArgument(0));

        Employee result = employeeService.createEmployee(dto);

        assertEquals("B123", result.getBadgeId());
        verify(auditLogRepository).save(any());
    }

    @Test
    void createEmployee_duplicateBadgeId_throwsException() {
        EmployeeDto dto = new EmployeeDto("B123", "John Doe", "ENGINEER", "IT", "A", LocalDate.now(), EmployeeStatus.ACTIVE, List.of(), List.of());
        when(employeeRepository.findByBadgeId("B123")).thenReturn(Optional.of(new Employee()));

        assertThrows(ValidationException.class, () -> employeeService.createEmployee(dto));
        verify(employeeRepository, never()).save(any());
    }

    @Test
    void getEmployee_found_returnsEmployee() {
        Employee emp = new Employee();
        emp.setId(1L);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(emp));

        Employee result = employeeService.getEmployee(1L);

        assertEquals(1L, result.getId());
    }

    @Test
    void getEmployee_notFound_throwsException() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> employeeService.getEmployee(1L));
    }

    @Test
    void getAllEmployees_withFilters_returnsPage() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Employee> employees = List.of(new Employee(), new Employee());
        Page<Employee> page = new PageImpl<>(employees, pageable, 2);
        when(employeeRepository.findAll(any(), eq(pageable))).thenReturn(page);

        Page<Employee> result = employeeService.getAllEmployees(pageable, "IT", EmployeeStatus.ACTIVE);

        assertEquals(2, result.getTotalElements());
    }

    @Test
    void updateEmployee_validInput_success() {
        EmployeeDto dto = new EmployeeDto("B123", "Jane Doe", "MANAGER", "HR", "B", LocalDate.now(), EmployeeStatus.ACTIVE, List.of(), List.of());
        Employee existing = new Employee();
        existing.setId(1L);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(i -> i.getArgument(0));

        Employee result = employeeService.updateEmployee(1L, dto);

        assertEquals("Jane Doe", result.getName());
        verify(auditLogRepository).save(any());
    }

    @Test
    void updateEmployee_notFound_throwsException() {
        EmployeeDto dto = new EmployeeDto("B123", "Jane Doe", "MANAGER", "HR", "B", LocalDate.now(), EmployeeStatus.ACTIVE, List.of(), List.of());
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> employeeService.updateEmployee(1L, dto));
    }

    @Test
    void deleteEmployee_found_softDeletesAndLogs() {
        Employee emp = new Employee();
        emp.setId(1L);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(emp));

        employeeService.deleteEmployee(1L);

        assertEquals(EmployeeStatus.DELETED, emp.getStatus());
        verify(employeeRepository).save(emp);
        verify(auditLogRepository).save(any());
    }

    @Test
    void deleteEmployee_notFound_throwsException() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> employeeService.deleteEmployee(1L));
    }
}