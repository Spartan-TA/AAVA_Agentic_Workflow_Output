package com.warehouse.employee;

import com.warehouse.employee.dto.EmployeeDto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Basic unit tests for EmployeeService.
 */
public class EmployeeServiceTest {
    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeService employeeService;

    public EmployeeServiceTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCreateEmployee_UniqueBadgeId() {
        EmployeeDto dto = EmployeeDto.builder()
                .badgeId("12345")
                .name("Jane Doe")
                .role("WORKER")
                .department("Shipping")
                .shiftGroup("A")
                .hireDate(LocalDate.now())
                .status("ACTIVE")
                .build();
        when(employeeRepository.findByBadgeIdAndDeletedFalse("12345")).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenAnswer(i -> i.getArgument(0));
        Employee employee = employeeService.createEmployee(dto);
        Assertions.assertEquals("12345", employee.getBadgeId());
    }

    @Test
    public void testCreateEmployee_DuplicateBadgeIdThrows() {
        EmployeeDto dto = EmployeeDto.builder()
                .badgeId("12345")
                .name("Jane Doe")
                .role("WORKER")
                .department("Shipping")
                .shiftGroup("A")
                .hireDate(LocalDate.now())
                .status("ACTIVE")
                .build();
        when(employeeRepository.findByBadgeIdAndDeletedFalse("12345")).thenReturn(Optional.of(new Employee()));
        Assertions.assertThrows(IllegalArgumentException.class, () -> employeeService.createEmployee(dto));
    }
}
