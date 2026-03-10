package com.company.warehouse.employee.service;

import com.company.warehouse.common.exception.BadRequestException;
import com.company.warehouse.common.exception.ResourceNotFoundException;
import com.company.warehouse.employee.dto.EmployeeDto;
import com.company.warehouse.employee.entity.Employee;
import com.company.warehouse.employee.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeService employeeService;

    private Employee employee;
    private EmployeeDto employeeDto;

    @BeforeEach
    void setUp() {
        employee = Employee.builder()
                .id(1L)
                .badgeId("B123456")
                .name("John Doe")
                .role("Worker")
                .department("Logistics")
                .shiftGroup("A")
                .hireDate(LocalDate.of(2020, 1, 1))
                .status("ACTIVE")
                .deleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        employeeDto = EmployeeDto.builder()
                .id(1L)
                .badgeId("B123456")
                .name("John Doe")
                .role("Worker")
                .department("Logistics")
                .shiftGroup("A")
                .hireDate(LocalDate.of(2020, 1, 1))
                .status("ACTIVE")
                .build();
    }

    @Test
    @DisplayName("getAllEmployees returns page of not deleted employees")
    void getAllEmployees_returnsPageOfEmployees() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Employee> employees = Arrays.asList(employee);
        Page<Employee> page = new PageImpl<>(employees);
        given(employeeRepository.findAllByDeletedFalse(pageable)).willReturn(page);
        Page<Employee> result = employeeService.getAllEmployees(pageable);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getBadgeId()).isEqualTo("B123456");
    }

    @Test
    @DisplayName("getEmployeeById returns employee when found and not deleted")
    void getEmployeeById_employeeExistsAndNotDeleted_returnsEmployee() {
        given(employeeRepository.findById(1L)).willReturn(Optional.of(employee));
        Employee found = employeeService.getEmployeeById(1L);
        assertThat(found).isNotNull();
        assertThat(found.getBadgeId()).isEqualTo("B123456");
    }

    @Test
    @DisplayName("getEmployeeById throws ResourceNotFoundException when not found")
    void getEmployeeById_employeeNotFound_throwsException() {
        given(employeeRepository.findById(2L)).willReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> employeeService.getEmployeeById(2L));
    }

    @Test
    @DisplayName("getEmployeeById throws ResourceNotFoundException when deleted")
    void getEmployeeById_employeeDeleted_throwsException() {
        Employee deleted = Employee.builder().id(3L).deleted(true).build();
        given(employeeRepository.findById(3L)).willReturn(Optional.of(deleted));
        assertThrows(ResourceNotFoundException.class, () -> employeeService.getEmployeeById(3L));
    }

    @Test
    @DisplayName("createEmployee saves and returns new employee when badgeId unique")
    void createEmployee_badgeIdUnique_savesAndReturnsEmployee() {
        given(employeeRepository.existsByBadgeIdAndDeletedFalse("B123456")).willReturn(false);
        given(employeeRepository.save(any(Employee.class))).willAnswer(invocation -> {
            Employee e = invocation.getArgument(0);
            e.setId(10L);
            return e;
        });
        Employee created = employeeService.createEmployee(employeeDto);
        assertThat(created.getId()).isEqualTo(10L);
        assertThat(created.getBadgeId()).isEqualTo("B123456");
        assertThat(created.getDeleted()).isFalse();
        assertThat(created.getCreatedAt()).isNotNull();
        assertThat(created.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("createEmployee throws BadRequestException when badgeId exists and not deleted")
    void createEmployee_badgeIdExists_throwsException() {
        given(employeeRepository.existsByBadgeIdAndDeletedFalse("B123456")).willReturn(true);
        assertThrows(BadRequestException.class, () -> employeeService.createEmployee(employeeDto));
    }

    @Test
    @DisplayName("updateEmployee updates and returns employee when badgeId unchanged")
    void updateEmployee_badgeIdUnchanged_updatesEmployee() {
        given(employeeRepository.findById(1L)).willReturn(Optional.of(employee));
        given(employeeRepository.save(any(Employee.class))).willAnswer(invocation -> invocation.getArgument(0));
        EmployeeDto updateDto = employeeDto.toBuilder().name("Jane Updated").build();
        Employee updated = employeeService.updateEmployee(1L, updateDto);
        assertThat(updated.getName()).isEqualTo("Jane Updated");
        assertThat(updated.getBadgeId()).isEqualTo("B123456");
    }

    @Test
    @DisplayName("updateEmployee updates and returns employee when badgeId changed and unique")
    void updateEmployee_badgeIdChangedAndUnique_updatesEmployee() {
        given(employeeRepository.findById(1L)).willReturn(Optional.of(employee));
        given(employeeRepository.existsByBadgeIdAndDeletedFalse("B654321")).willReturn(false);
        given(employeeRepository.save(any(Employee.class))).willAnswer(invocation -> invocation.getArgument(0));
        EmployeeDto updateDto = employeeDto.toBuilder().badgeId("B654321").build();
        Employee updated = employeeService.updateEmployee(1L, updateDto);
        assertThat(updated.getBadgeId()).isEqualTo("B654321");
    }

    @Test
    @DisplayName("updateEmployee throws BadRequestException when badgeId changed and exists")
    void updateEmployee_badgeIdChangedAndExists_throwsException() {
        given(employeeRepository.findById(1L)).willReturn(Optional.of(employee));
        given(employeeRepository.existsByBadgeIdAndDeletedFalse("B654321")).willReturn(true);
        EmployeeDto updateDto = employeeDto.toBuilder().badgeId("B654321").build();
        assertThrows(BadRequestException.class, () -> employeeService.updateEmployee(1L, updateDto));
    }

    @Test
    @DisplayName("updateEmployee throws ResourceNotFoundException when employee not found")
    void updateEmployee_employeeNotFound_throwsException() {
        given(employeeRepository.findById(2L)).willReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> employeeService.updateEmployee(2L, employeeDto));
    }

    @Test
    @DisplayName("deleteEmployee sets deleted to true and saves employee")
    void deleteEmployee_setsDeletedTrueAndSaves() {
        given(employeeRepository.findById(1L)).willReturn(Optional.of(employee));
        given(employeeRepository.save(any(Employee.class))).willAnswer(invocation -> invocation.getArgument(0));
        employeeService.deleteEmployee(1L);
        ArgumentCaptor<Employee> captor = ArgumentCaptor.forClass(Employee.class);
        verify(employeeRepository).save(captor.capture());
        assertThat(captor.getValue().getDeleted()).isTrue();
    }

    @Test
    @DisplayName("deleteEmployee throws ResourceNotFoundException when employee not found")
    void deleteEmployee_employeeNotFound_throwsException() {
        given(employeeRepository.findById(2L)).willReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> employeeService.deleteEmployee(2L));
    }
}
