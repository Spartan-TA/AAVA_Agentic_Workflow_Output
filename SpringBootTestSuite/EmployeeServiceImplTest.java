package com.example.warehouse.service;

import com.example.warehouse.entity.Employee;
import com.example.warehouse.exception.DuplicateBadgeIdException;
import com.example.warehouse.exception.EmployeeNotFoundException;
import com.example.warehouse.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmployeeServiceImplTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    private Employee employee;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        employee = new Employee();
        employee.setId(1L);
        employee.setName("John Doe");
        employee.setBadgeId("BADGE123");
        employee.setRole("WORKER");
        employee.setDepartment("Logistics");
        employee.setShiftGroup("A");
        employee.setHireDate(LocalDate.now().minusYears(1));
        employee.setStatus("ACTIVE");
        employee.setSoftDeleted(false);
    }

    @Test
    void testCreateEmployee_ValidInput_Success() {
        when(employeeRepository.findByBadgeId("BADGE123")).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        Employee created = employeeService.createEmployee(employee);

        assertThat(created).isNotNull();
        assertThat(created.getBadgeId()).isEqualTo("BADGE123");
        verify(employeeRepository).save(employee);
    }

    @Test
    void testCreateEmployee_NullEmployee_ThrowsException() {
        assertThatThrownBy(() -> employeeService.createEmployee(null))
                .isInstanceOf(IllegalArgumentException.class);
        verify(employeeRepository, never()).save(any());
    }

    @Test
    void testCreateEmployee_DuplicateBadgeId_ThrowsException() {
        when(employeeRepository.findByBadgeId("BADGE123")).thenReturn(Optional.of(employee));
        Employee duplicate = new Employee();
        duplicate.setBadgeId("BADGE123");

        assertThatThrownBy(() -> employeeService.createEmployee(duplicate))
                .isInstanceOf(DuplicateBadgeIdException.class);
        verify(employeeRepository, never()).save(any());
    }

    @Test
    void testGetEmployeeById_ExistingId_ReturnsEmployee() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        Employee found = employeeService.getEmployeeById(1L);

        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(1L);
    }

    @Test
    void testGetEmployeeById_NonExistingId_ThrowsNotFoundException() {
        when(employeeRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.getEmployeeById(2L))
                .isInstanceOf(EmployeeNotFoundException.class);
    }

    @Test
    void testGetEmployeeById_NullId_ThrowsException() {
        assertThatThrownBy(() -> employeeService.getEmployeeById(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testUpdateEmployee_ValidInput_Success() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        Employee updated = new Employee();
        updated.setId(1L);
        updated.setName("Jane Doe");
        updated.setBadgeId("BADGE123");
        updated.setRole("SUPERVISOR");
        updated.setDepartment("Logistics");
        updated.setShiftGroup("B");
        updated.setHireDate(LocalDate.now().minusYears(2));
        updated.setStatus("ACTIVE");
        updated.setSoftDeleted(false);

        Employee result = employeeService.updateEmployee(1L, updated);

        assertThat(result.getName()).isEqualTo("Jane Doe");
        verify(employeeRepository).save(any(Employee.class));
    }

    @Test
    void testUpdateEmployee_NonExistingId_ThrowsException() {
        when(employeeRepository.findById(2L)).thenReturn(Optional.empty());
        Employee updated = new Employee();
        updated.setId(2L);

        assertThatThrownBy(() -> employeeService.updateEmployee(2L, updated))
                .isInstanceOf(EmployeeNotFoundException.class);
    }

    @Test
    void testDeleteEmployee_ExistingId_SoftDeletes() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        employeeService.deleteEmployee(1L);

        assertThat(employee.isSoftDeleted()).isTrue();
        verify(employeeRepository).save(employee);
    }

    @Test
    void testGetAllEmployees_WithPagination_ReturnsPage() {
        Page<Employee> page = new PageImpl<>(Collections.singletonList(employee));
        Pageable pageable = PageRequest.of(0, 10);
        when(employeeRepository.findBySoftDeletedFalse(pageable)).thenReturn(page);

        Page<Employee> result = employeeService.getAllEmployees(pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getBadgeId()).isEqualTo("BADGE123");
    }

    @Test
    void testGetAllEmployees_EmptyDatabase_ReturnsEmptyPage() {
        Pageable pageable = PageRequest.of(0, 10);
        when(employeeRepository.findBySoftDeletedFalse(pageable)).thenReturn(Page.empty());

        Page<Employee> result = employeeService.getAllEmployees(pageable);

        assertThat(result.getContent()).isEmpty();
    }
}