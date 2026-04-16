package com.warehouse.employee.management.service;

import com.warehouse.employee.management.dto.EmployeeCreateRequest;
import com.warehouse.employee.management.entity.Employee;
import com.warehouse.employee.management.repository.EmployeeRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.*;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class EmployeeServiceTest {

    @Test
    void createEmployee_success() {
        EmployeeRepository repo = mock(EmployeeRepository.class);
        EmployeeService service = new EmployeeService(repo);

        EmployeeCreateRequest req = EmployeeCreateRequest.builder()
                .name("John Doe")
                .badgeId("B123")
                .role("WORKER")
                .department("Shipping")
                .shiftGroup("Night")
                .hireDate(LocalDate.now())
                .status("ACTIVE")
                .build();

        when(repo.existsByBadgeIdAndDeletedFalse(anyString())).thenReturn(false);
        when(repo.save(any(Employee.class))).thenAnswer(inv -> {
            Employee e = inv.getArgument(0);
            e.setId(1L);
            return e;
        });

        var resp = service.createEmployee(req);
        assertEquals("John Doe", resp.getName());
        assertEquals("B123", resp.getBadgeId());
        assertEquals("WORKER", resp.getRole());
    }
}