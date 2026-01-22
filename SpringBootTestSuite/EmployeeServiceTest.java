package com.warehouse.ems.service;

import com.warehouse.ems.dto.employee.*;
import com.warehouse.ems.entity.Employee;
import com.warehouse.ems.exception.ResourceNotFoundException;
import com.warehouse.ems.repository.EmployeeRepository;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.data.domain.*;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class EmployeeServiceTest {
    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeService employeeService;

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    private EmployeeCreateRequest validCreateRequest() {
        return EmployeeCreateRequest.builder()
                .badgeId("B123")
                .name("John Doe")
                .role("Worker")
                .department("Packing")
                .shiftGroup("A")
                .hireDate(LocalDate.of(2020, 1, 1))
                .status("Active")
                .build();
    }

    private EmployeeUpdateRequest validUpdateRequest() {
        return EmployeeUpdateRequest.builder()
                .name("Jane Smith")
                .role("Supervisor")
                .department("Shipping")
                .shiftGroup("B")
                .hireDate(LocalDate.of(2021, 2, 2))
                .status("Inactive")
                .build();
    }

    private Employee validEmployee(Long id, boolean deleted) {
        return Employee.builder()
                .id(id)
                .badgeId("B123")
                .name("John Doe")
                .role("Worker")
                .department("Packing")
                .shiftGroup("A")
                .hireDate(LocalDate.of(2020, 1, 1))
                .status("Active")
                .deleted(deleted)
                .build();
    }

    @Test
    void testCreateEmployee_ValidInput_Success() {
        EmployeeCreateRequest request = validCreateRequest();
        Employee saved = validEmployee(1L, false);
        when(employeeRepository.save(any(Employee.class))).thenReturn(saved);

        EmployeeResponse response = employeeService.createEmployee(request);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getBadgeId()).isEqualTo("B123");
        verify(employeeRepository).save(any(Employee.class));
    }

    @Test
    void testCreateEmployee_NullBadgeId_ThrowsException() {
        EmployeeCreateRequest request = validCreateRequest();
        request.setBadgeId(null);
        assertThrows(NullPointerException.class, () -> employeeService.createEmployee(request));
    }

    @Test
    void testCreateEmployee_EmptyName_ThrowsException() {
        EmployeeCreateRequest request = validCreateRequest();
        request.setName("");
        assertThrows(IllegalArgumentException.class, () -> employeeService.createEmployee(request));
    }

    @Test
    void testGetEmployees_WithDepartmentFilter_ReturnsFiltered() {
        Employee emp = validEmployee(1L, false);
        Page<Employee> page = new PageImpl<>(Collections.singletonList(emp));
        when(employeeRepository.findAll(any(), any(Pageable.class))).thenReturn(page);

        Page<EmployeeResponse> result = employeeService.getEmployees("Packing", PageRequest.of(0, 10));
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getDepartment()).isEqualTo("Packing");
    }

    @Test
    void testGetEmployees_NoDepartmentFilter_ReturnsAll() {
        Employee emp1 = validEmployee(1L, false);
        Employee emp2 = validEmployee(2L, false);
        emp2.setDepartment("Shipping");
        Page<Employee> page = new PageImpl<>(Arrays.asList(emp1, emp2));
        when(employeeRepository.findAll(any(), any(Pageable.class))).thenReturn(page);

        Page<EmployeeResponse> result = employeeService.getEmployees(null, PageRequest.of(0, 10));
        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    @Test
    void testGetEmployee_ValidId_ReturnsEmployee() {
        Employee emp = validEmployee(1L, false);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(emp));

        EmployeeResponse response = employeeService.getEmployee(1L);
        assertThat(response.getId()).isEqualTo(1L);
    }

    @Test
    void testGetEmployee_InvalidId_ThrowsResourceNotFoundException() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> employeeService.getEmployee(99L));
    }

    @Test
    void testGetEmployee_SoftDeletedEmployee_ThrowsResourceNotFoundException() {
        Employee emp = validEmployee(1L, true);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(emp));
        assertThrows(ResourceNotFoundException.class, () -> employeeService.getEmployee(1L));
    }

    @Test
    void testUpdateEmployee_ValidInput_Success() {
        Employee emp = validEmployee(1L, false);
        Employee updated = validEmployee(1L, false);
        updated.setName("Jane Smith");
        updated.setRole("Supervisor");
        updated.setDepartment("Shipping");
        updated.setShiftGroup("B");
        updated.setHireDate(LocalDate.of(2021, 2, 2));
        updated.setStatus("Inactive");

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(emp));
        when(employeeRepository.save(any(Employee.class))).thenReturn(updated);

        EmployeeUpdateRequest request = validUpdateRequest();
        EmployeeResponse response = employeeService.updateEmployee(1L, request);
        assertThat(response.getName()).isEqualTo("Jane Smith");
        assertThat(response.getRole()).isEqualTo("Supervisor");
    }

    @Test
    void testUpdateEmployee_InvalidId_ThrowsResourceNotFoundException() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> employeeService.updateEmployee(99L, validUpdateRequest()));
    }

    @Test
    void testSoftDeleteEmployee_ValidId_Success() {
        Employee emp = validEmployee(1L, false);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(emp));
        when(employeeRepository.save(any(Employee.class))).thenReturn(emp);

        assertDoesNotThrow(() -> employeeService.softDeleteEmployee(1L));
        verify(employeeRepository).save(argThat(e -> e.getDeleted()));
    }

    @Test
    void testSoftDeleteEmployee_InvalidId_ThrowsResourceNotFoundException() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> employeeService.softDeleteEmployee(99L));
    }
}