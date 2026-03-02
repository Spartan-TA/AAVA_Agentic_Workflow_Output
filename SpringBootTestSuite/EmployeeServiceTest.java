package com.wms.employee.service;

import com.wms.employee.domain.Employee;
import com.wms.employee.domain.EmployeeStatus;
import com.wms.employee.dto.EmployeeDto;
import com.wms.employee.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class EmployeeServiceTest {
    @Mock
    private EmployeeRepository employeeRepository;
    @InjectMocks
    private EmployeeService employeeService;

    private EmployeeDto validDto;
    private Employee validEmployee;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        validDto = new EmployeeDto();
        validDto.setBadgeId("B123");
        validDto.setName("John Doe");
        validDto.setRole("Worker");
        validDto.setDepartment("Packing");
        validDto.setShiftGroup("A");
        validDto.setHireDate(LocalDate.of(2020, 1, 1));
        validDto.setStatus(EmployeeStatus.ACTIVE);

        validEmployee = new Employee();
        validEmployee.setId(1L);
        validEmployee.setBadgeId("B123");
        validEmployee.setName("John Doe");
        validEmployee.setRole("Worker");
        validEmployee.setDepartment("Packing");
        validEmployee.setShiftGroup("A");
        validEmployee.setHireDate(LocalDate.of(2020, 1, 1));
        validEmployee.setStatus(EmployeeStatus.ACTIVE);
        validEmployee.setDeleted(false);
    }

    @Test
    @DisplayName("testCreateEmployee_ValidInput_Success")
    void testCreateEmployee_ValidInput_Success() {
        when(employeeRepository.findByBadgeId("B123")).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);

        Employee result = employeeService.create(validDto);
        assertThat(result.getBadgeId()).isEqualTo("B123");
        verify(employeeRepository).save(any(Employee.class));
    }

    @Test
    @DisplayName("testCreateEmployee_DuplicateBadgeId_ThrowsException")
    void testCreateEmployee_DuplicateBadgeId_ThrowsException() {
        when(employeeRepository.findByBadgeId("B123")).thenReturn(Optional.of(validEmployee));
        assertThrows(DuplicateBadgeIdException.class, () -> employeeService.create(validDto));
    }

    @Test
    @DisplayName("testUpdateEmployee_ValidInput_Success")
    void testUpdateEmployee_ValidInput_Success() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);
        validDto.setName("Jane Doe");
        Employee result = employeeService.update(1L, validDto);
        assertThat(result.getName()).isEqualTo("Jane Doe");
    }

    @Test
    @DisplayName("testUpdateEmployee_NotFound_ThrowsException")
    void testUpdateEmployee_NotFound_ThrowsException() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(EmployeeNotFoundException.class, () -> employeeService.update(1L, validDto));
    }

    @Test
    @DisplayName("testSoftDeleteEmployee_Success")
    void testSoftDeleteEmployee_Success() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);
        employeeService.softDelete(1L);
        assertThat(validEmployee.getDeleted()).isTrue();
        verify(employeeRepository).save(validEmployee);
    }

    @Test
    @DisplayName("testSoftDeleteEmployee_NotFound_ThrowsException")
    void testSoftDeleteEmployee_NotFound_ThrowsException() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(EmployeeNotFoundException.class, () -> employeeService.softDelete(1L));
    }

    @Test
    @DisplayName("testListEmployees_WithFilters_Success")
    void testListEmployees_WithFilters_Success() {
        Page<Employee> page = new PageImpl<>(Collections.singletonList(validEmployee));
        when(employeeRepository.findByFilters(any(), any(), any(), any(Pageable.class))).thenReturn(page);
        Page<Employee> result = employeeService.list(PageRequest.of(0, 10), "Packing", "Worker", EmployeeStatus.ACTIVE);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getBadgeId()).isEqualTo("B123");
    }

    @Test
    @DisplayName("testFindById_ValidId_Success")
    void testFindById_ValidId_Success() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        Employee result = employeeService.findById(1L);
        assertThat(result).isNotNull();
        assertThat(result.getBadgeId()).isEqualTo("B123");
    }

    @Test
    @DisplayName("testFindById_NotFound_ThrowsException")
    void testFindById_NotFound_ThrowsException() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(EmployeeNotFoundException.class, () -> employeeService.findById(1L));
    }

    @Test
    @DisplayName("testPartialUpdateEmployee_ValidFields_Success")
    void testPartialUpdateEmployee_ValidFields_Success() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", "Jane Doe");
        updates.put("role", "Supervisor");
        updates.put("department", "Shipping");
        updates.put("shiftGroup", "B");
        updates.put("status", "ON_LEAVE");
        Employee result = employeeService.partialUpdate(1L, updates);
        assertThat(result.getName()).isEqualTo("Jane Doe");
        assertThat(result.getRole()).isEqualTo("Supervisor");
        assertThat(result.getDepartment()).isEqualTo("Shipping");
        assertThat(result.getShiftGroup()).isEqualTo("B");
        assertThat(result.getStatus()).isEqualTo(EmployeeStatus.ON_LEAVE);
    }

    @Test
    @DisplayName("testPartialUpdateEmployee_InvalidStatus_ThrowsException")
    void testPartialUpdateEmployee_InvalidStatus_ThrowsException() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "INVALID_STATUS");
        assertThrows(IllegalArgumentException.class, () -> employeeService.partialUpdate(1L, updates));
    }

    @Test
    @DisplayName("testPartialUpdateEmployee_NotFound_ThrowsException")
    void testPartialUpdateEmployee_NotFound_ThrowsException() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", "Jane Doe");
        assertThrows(EmployeeNotFoundException.class, () -> employeeService.partialUpdate(1L, updates));
    }

    @Nested
    @DisplayName("Boundary and Null Value Tests")
    class BoundaryTests {
        @Test
        @DisplayName("testCreateEmployee_EmptyStrings_Success")
        void testCreateEmployee_EmptyStrings_Success() {
            EmployeeDto dto = new EmployeeDto();
            dto.setBadgeId("B124");
            dto.setName("");
            dto.setRole("");
            dto.setDepartment("");
            dto.setShiftGroup("");
            dto.setHireDate(LocalDate.of(2021, 1, 1));
            dto.setStatus(EmployeeStatus.ACTIVE);
            when(employeeRepository.findByBadgeId("B124")).thenReturn(Optional.empty());
            when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);
            Employee result = employeeService.create(dto);
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("testCreateEmployee_NullOptionalFields_Success")
        void testCreateEmployee_NullOptionalFields_Success() {
            EmployeeDto dto = new EmployeeDto();
            dto.setBadgeId("B125");
            dto.setName("Null Fields");
            dto.setRole("Worker");
            dto.setDepartment("Packing");
            dto.setShiftGroup(null);
            dto.setHireDate(LocalDate.of(2021, 1, 1));
            dto.setStatus(EmployeeStatus.ACTIVE);
            when(employeeRepository.findByBadgeId("B125")).thenReturn(Optional.empty());
            when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);
            Employee result = employeeService.create(dto);
            assertThat(result).isNotNull();
        }
    }
}
