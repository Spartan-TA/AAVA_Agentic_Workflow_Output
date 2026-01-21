package com.wms.employee;

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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * JUnit tests for EmployeeController covering all REST endpoints and edge cases.
 */
@ExtendWith(MockitoExtension.class)
public class EmployeeControllerTest {

    @Mock
    private EmployeeService employeeService;

    @InjectMocks
    private EmployeeController employeeController;

    private EmployeeDTO validEmployeeDTO;
    private Page<EmployeeDTO> employeePage;

    @BeforeEach
    public void setUp() {
        validEmployeeDTO = new EmployeeDTO();
        validEmployeeDTO.setName("Jane Smith");
        validEmployeeDTO.setBadgeId("BADGE456");
        validEmployeeDTO.setRole("SUPERVISOR");
        validEmployeeDTO.setDepartment("Packing");
        validEmployeeDTO.setShiftGroup("B");
        validEmployeeDTO.setHireDate(LocalDate.now());
        validEmployeeDTO.setStatus("ACTIVE");
        employeePage = new PageImpl<>(Arrays.asList(validEmployeeDTO));
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "HR"})
    public void testCreateEmployee_Valid_Returns201Created() {
        when(employeeService.createEmployee(any(EmployeeDTO.class))).thenReturn(validEmployeeDTO);
        ResponseEntity<EmployeeDTO> response = employeeController.createEmployee(validEmployeeDTO);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(validEmployeeDTO, response.getBody());
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "HR"})
    public void testCreateEmployee_BadRequest_Returns400() {
        doThrow(new ValidationException("Invalid input")).when(employeeService).createEmployee(any(EmployeeDTO.class));
        ValidationException ex = assertThrows(ValidationException.class, () -> employeeController.createEmployee(validEmployeeDTO));
        assertEquals("Invalid input", ex.getMessage());
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "HR"})
    public void testCreateEmployee_Conflict_Returns409() {
        doThrow(new ValidationException("Duplicate badgeId")).when(employeeService).createEmployee(any(EmployeeDTO.class));
        ValidationException ex = assertThrows(ValidationException.class, () -> employeeController.createEmployee(validEmployeeDTO));
        assertEquals("Duplicate badgeId", ex.getMessage());
    }

    @Test
    @WithMockUser(roles = {"SUPERVISOR"})
    public void testCreateEmployee_Forbidden_Returns403() {
        // Simulate forbidden by not having ADMIN or HR role
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> employeeController.createEmployee(validEmployeeDTO));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "HR", "SUPERVISOR"})
    public void testListEmployees_200OKWithPagination() {
        Pageable pageable = PageRequest.of(0, 10);
        when(employeeService.listEmployees(pageable, null, null)).thenReturn(employeePage);
        ResponseEntity<Page<EmployeeDTO>> response = employeeController.listEmployees(null, null, pageable);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getTotalElements());
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "HR", "SUPERVISOR"})
    public void testListEmployees_200OKWithFilters() {
        Pageable pageable = PageRequest.of(0, 10);
        when(employeeService.listEmployees(pageable, "SUPERVISOR", "Packing")).thenReturn(employeePage);
        ResponseEntity<Page<EmployeeDTO>> response = employeeController.listEmployees("SUPERVISOR", "Packing", pageable);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getTotalElements());
    }

    @Test
    public void testListEmployees_Unauthorized_Returns401() {
        // No @WithMockUser annotation simulates unauthorized
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> {
            Pageable pageable = PageRequest.of(0, 10);
            employeeController.listEmployees(null, null, pageable);
        });
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatus());
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "HR", "SUPERVISOR"})
    public void testGetEmployee_200OK() {
        when(employeeService.getEmployeeById(1L)).thenReturn(validEmployeeDTO);
        ResponseEntity<EmployeeDTO> response = employeeController.getEmployee(1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(validEmployeeDTO, response.getBody());
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "HR", "SUPERVISOR"})
    public void testGetEmployee_404NotFound() {
        doThrow(new ResourceNotFoundException("Not found")).when(employeeService).getEmployeeById(99L);
        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> employeeController.getEmployee(99L));
        assertEquals("Not found", ex.getMessage());
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "HR"})
    public void testUpdateEmployee_200OK() {
        when(employeeService.updateEmployee(1L, validEmployeeDTO)).thenReturn(validEmployeeDTO);
        ResponseEntity<EmployeeDTO> response = employeeController.updateEmployee(1L, validEmployeeDTO);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(validEmployeeDTO, response.getBody());
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "HR"})
    public void testUpdateEmployee_404NotFound() {
        doThrow(new ResourceNotFoundException("Not found")).when(employeeService).updateEmployee(99L, validEmployeeDTO);
        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> employeeController.updateEmployee(99L, validEmployeeDTO));
        assertEquals("Not found", ex.getMessage());
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "HR"})
    public void testUpdateEmployee_BadRequest_ThrowsValidationException() {
        doThrow(new ValidationException("Invalid input")).when(employeeService).updateEmployee(1L, validEmployeeDTO);
        ValidationException ex = assertThrows(ValidationException.class, () -> employeeController.updateEmployee(1L, validEmployeeDTO));
        assertEquals("Invalid input", ex.getMessage());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void testDeleteEmployee_204NoContent() {
        doNothing().when(employeeService).deleteEmployee(1L);
        ResponseEntity<Void> response = employeeController.deleteEmployee(1L);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void testDeleteEmployee_404NotFound() {
        doThrow(new ResourceNotFoundException("Not found")).when(employeeService).deleteEmployee(99L);
        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> employeeController.deleteEmployee(99L));
        assertEquals("Not found", ex.getMessage());
    }

    @Test
    @WithMockUser(roles = {"HR"})
    public void testDeleteEmployee_Forbidden_Returns403() {
        // Simulate forbidden by not having ADMIN role
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> employeeController.deleteEmployee(1L));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
    }
}
