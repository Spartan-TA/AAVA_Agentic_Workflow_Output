package com.example.warehouse.service;

import com.example.warehouse.entity.Employee;
import com.example.warehouse.entity.Role;
import com.example.warehouse.entity.Status;
import com.example.warehouse.exception.EntityNotFoundException;
import com.example.warehouse.exception.DuplicateKeyException;
import com.example.warehouse.repository.EmployeeRepository;
import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Mockito;
import org.springframework.dao.DataIntegrityViolationException;
import javax.validation.ValidationException;
import java.time.LocalDate;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class EmployeeServiceTest {
    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeService employeeService;

    private AutoCloseable closeable;

    private Employee validEmployee;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        validEmployee = new Employee();
        validEmployee.setId(1L);
        validEmployee.setName("John Doe");
        validEmployee.setBadgeId("BADGE123");
        validEmployee.setRole(Role.WORKER);
        validEmployee.setDepartment("Logistics");
        validEmployee.setShiftGroup("A");
        validEmployee.setHireDate(LocalDate.now().minusYears(1));
        validEmployee.setStatus(Status.ACTIVE);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    void createEmployee_validInput_success() {
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);
        Employee created = employeeService.createEmployee(validEmployee);
        assertThat(created).isNotNull();
        assertThat(created.getName()).isEqualTo("John Doe");
    }

    @Test
    void createEmployee_duplicateBadgeId_throwsDuplicateKeyException() {
        when(employeeRepository.save(any(Employee.class))).thenThrow(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> employeeService.createEmployee(validEmployee))
                .isInstanceOf(DuplicateKeyException.class);
    }

    @Test
    void createEmployee_nullEmployee_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> employeeService.createEmployee(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void updateEmployee_validInput_success() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);
        Employee updated = employeeService.updateEmployee(1L, validEmployee);
        assertThat(updated).isNotNull();
        assertThat(updated.getId()).isEqualTo(1L);
    }

    @Test
    void updateEmployee_nonExistentId_throwsEntityNotFoundException() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> employeeService.updateEmployee(99L, validEmployee))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void deleteEmployee_validId_success() {
        when(employeeRepository.existsById(1L)).thenReturn(true);
        doNothing().when(employeeRepository).deleteById(1L);
        assertThatCode(() -> employeeService.deleteEmployee(1L)).doesNotThrowAnyException();
    }

    @Test
    void deleteEmployee_nonExistentId_throwsEntityNotFoundException() {
        when(employeeRepository.existsById(99L)).thenReturn(false);
        assertThatThrownBy(() -> employeeService.deleteEmployee(99L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void getEmployeeById_validId_success() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        Employee found = employeeService.getEmployeeById(1L);
        assertThat(found).isNotNull();
        assertThat(found.getBadgeId()).isEqualTo("BADGE123");
    }

    @Test
    void getEmployeeById_nonExistentId_throwsEntityNotFoundException() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> employeeService.getEmployeeById(99L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void getAllEmployees_returnsList() {
        when(employeeRepository.findAll()).thenReturn(List.of(validEmployee));
        List<Employee> employees = employeeService.getAllEmployees();
        assertThat(employees).isNotEmpty();
        assertThat(employees.get(0).getName()).isEqualTo("John Doe");
    }

    @Test
    void getAllEmployees_emptyList() {
        when(employeeRepository.findAll()).thenReturn(Collections.emptyList());
        List<Employee> employees = employeeService.getAllEmployees();
        assertThat(employees).isEmpty();
    }

    @Test
    void getEmployeeByBadgeId_validBadgeId_success() {
        when(employeeRepository.findByBadgeId("BADGE123")).thenReturn(Optional.of(validEmployee));
        Employee found = employeeService.getEmployeeByBadgeId("BADGE123");
        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(1L);
    }

    @Test
    void getEmployeeByBadgeId_nonExistent_throwsEntityNotFoundException() {
        when(employeeRepository.findByBadgeId("BADGE999")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> employeeService.getEmployeeByBadgeId("BADGE999"))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void validateEmployee_validEmployee_success() {
        assertThatCode(() -> employeeService.validateEmployee(validEmployee)).doesNotThrowAnyException();
    }

    @Test
    void validateEmployee_nullName_throwsValidationException() {
        validEmployee.setName(null);
        assertThatThrownBy(() -> employeeService.validateEmployee(validEmployee))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void validateEmployee_emptyBadgeId_throwsValidationException() {
        validEmployee.setBadgeId("");
        assertThatThrownBy(() -> employeeService.validateEmployee(validEmployee))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void createEmployee_blankDepartment_throwsValidationException() {
        validEmployee.setDepartment("");
        assertThatThrownBy(() -> employeeService.createEmployee(validEmployee))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void createEmployee_futureHireDate_throwsValidationException() {
        validEmployee.setHireDate(LocalDate.now().plusDays(1));
        assertThatThrownBy(() -> employeeService.createEmployee(validEmployee))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void createEmployee_nullRole_throwsValidationException() {
        validEmployee.setRole(null);
        assertThatThrownBy(() -> employeeService.createEmployee(validEmployee))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void createEmployee_nullStatus_throwsValidationException() {
        validEmployee.setStatus(null);
        assertThatThrownBy(() -> employeeService.createEmployee(validEmployee))
                .isInstanceOf(ValidationException.class);
    }
}
