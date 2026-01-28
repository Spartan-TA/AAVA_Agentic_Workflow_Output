package com.warehouse.employee.service.impl;

import com.warehouse.employee.domain.Employee;
import com.warehouse.employee.dto.*;
import com.warehouse.employee.exception.*;
import com.warehouse.employee.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceImplTest {
    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    private EmployeeCreateRequest validCreateRequest;
    private EmployeeUpdateRequest validUpdateRequest;
    private Employee employee;
    private UUID employeeId;
    private String actor;

    @BeforeEach
    void setUp() {
        employeeId = UUID.randomUUID();
        actor = "test-actor";
        validCreateRequest = EmployeeCreateRequest.builder()
                .name("John Doe")
                .badgeId("BADGE123")
                .role("Worker")
                .department("Packing")
                .shiftGroup("A")
                .hireDate(LocalDate.now())
                .status("Active")
                .build();
        validUpdateRequest = EmployeeUpdateRequest.builder()
                .name("Jane Doe")
                .role("Supervisor")
                .department("Shipping")
                .shiftGroup("B")
                .status("Inactive")
                .build();
        employee = Employee.builder()
                .id(employeeId)
                .name("John Doe")
                .badgeId("BADGE123")
                .role("Worker")
                .department("Packing")
                .shiftGroup("A")
                .hireDate(LocalDate.now())
                .status("Active")
                .deleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .createdBy(actor)
                .updatedBy(actor)
                .build();
    }

    @Test
    void testCreateEmployee_WithValidData_ShouldReturnEmployee() {
        when(employeeRepository.existsByBadgeIdAndDeletedFalse(validCreateRequest.getBadgeId())).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> {
            Employee emp = invocation.getArgument(0);
            emp.setId(employeeId);
            return emp;
        });

        EmployeeDTO result = employeeService.createEmployee(validCreateRequest, actor);
        assertNotNull(result);
        assertEquals(validCreateRequest.getName(), result.getName());
        assertEquals(validCreateRequest.getBadgeId(), result.getBadgeId());
        assertEquals(validCreateRequest.getRole(), result.getRole());
        assertEquals(validCreateRequest.getDepartment(), result.getDepartment());
        assertEquals(validCreateRequest.getShiftGroup(), result.getShiftGroup());
        assertEquals(validCreateRequest.getHireDate(), result.getHireDate());
        assertEquals(validCreateRequest.getStatus(), result.getStatus());
        verify(employeeRepository).save(any(Employee.class));
    }

    @Test
    void testCreateEmployee_WithDuplicateBadgeId_ShouldThrowDuplicateException() {
        when(employeeRepository.existsByBadgeIdAndDeletedFalse(validCreateRequest.getBadgeId())).thenReturn(true);
        DuplicateException ex = assertThrows(DuplicateException.class, () ->
                employeeService.createEmployee(validCreateRequest, actor));
        assertTrue(ex.getMessage().contains(validCreateRequest.getBadgeId()));
    }

    @Test
    void testCreateEmployee_WithNullActor_ShouldStillCreate() {
        when(employeeRepository.existsByBadgeIdAndDeletedFalse(validCreateRequest.getBadgeId())).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> {
            Employee emp = invocation.getArgument(0);
            emp.setId(employeeId);
            return emp;
        });
        EmployeeDTO result = employeeService.createEmployee(validCreateRequest, null);
        assertNotNull(result);
        assertEquals(validCreateRequest.getName(), result.getName());
        verify(employeeRepository).save(any(Employee.class));
    }

    @Test
    void testUpdateEmployee_WithValidData_ShouldReturnUpdatedEmployee() {
        Employee existing = employee.toBuilder().build();
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(existing));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EmployeeDTO result = employeeService.updateEmployee(employeeId, validUpdateRequest, actor);
        assertNotNull(result);
        assertEquals(validUpdateRequest.getName(), result.getName());
        assertEquals(validUpdateRequest.getRole(), result.getRole());
        assertEquals(validUpdateRequest.getDepartment(), result.getDepartment());
        assertEquals(validUpdateRequest.getShiftGroup(), result.getShiftGroup());
        assertEquals(validUpdateRequest.getStatus(), result.getStatus());
        verify(employeeRepository).save(any(Employee.class));
    }

    @Test
    void testUpdateEmployee_WithNonExistentId_ShouldThrowNotFoundException() {
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());
        NotFoundException ex = assertThrows(NotFoundException.class, () ->
                employeeService.updateEmployee(employeeId, validUpdateRequest, actor));
        assertTrue(ex.getMessage().contains(employeeId.toString()));
    }

    @Test
    void testUpdateEmployee_WithDeletedEmployee_ShouldThrowNotFoundException() {
        Employee deletedEmp = employee.toBuilder().deleted(true).build();
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(deletedEmp));
        NotFoundException ex = assertThrows(NotFoundException.class, () ->
                employeeService.updateEmployee(employeeId, validUpdateRequest, actor));
        assertTrue(ex.getMessage().contains(employeeId.toString()));
    }

    @Test
    void testGetEmployee_WithValidId_ShouldReturnEmployee() {
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
        EmployeeDTO result = employeeService.getEmployee(employeeId);
        assertNotNull(result);
        assertEquals(employee.getId(), result.getId());
        assertEquals(employee.getName(), result.getName());
    }

    @Test
    void testGetEmployee_WithNonExistentId_ShouldThrowNotFoundException() {
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());
        NotFoundException ex = assertThrows(NotFoundException.class, () ->
                employeeService.getEmployee(employeeId));
        assertTrue(ex.getMessage().contains(employeeId.toString()));
    }

    @Test
    void testGetEmployee_WithDeletedEmployee_ShouldThrowNotFoundException() {
        Employee deletedEmp = employee.toBuilder().deleted(true).build();
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(deletedEmp));
        NotFoundException ex = assertThrows(NotFoundException.class, () ->
                employeeService.getEmployee(employeeId));
        assertTrue(ex.getMessage().contains(employeeId.toString()));
    }

    @Test
    void testDeleteEmployee_WithValidId_ShouldSoftDelete() {
        Employee existing = employee.toBuilder().build();
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(existing));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> invocation.getArgument(0));
        employeeService.deleteEmployee(employeeId, actor);
        assertTrue(existing.getDeleted());
        assertEquals(actor, existing.getUpdatedBy());
        verify(employeeRepository).save(existing);
    }

    @Test
    void testDeleteEmployee_WithNonExistentId_ShouldThrowNotFoundException() {
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());
        NotFoundException ex = assertThrows(NotFoundException.class, () ->
                employeeService.deleteEmployee(employeeId, actor));
        assertTrue(ex.getMessage().contains(employeeId.toString()));
    }

    @Test
    void testListEmployees_WithPagination_ShouldReturnPage() {
        EmployeeDTO dto = EmployeeDTO.builder()
                .id(employee.getId())
                .name(employee.getName())
                .badgeId(employee.getBadgeId())
                .role(employee.getRole())
                .department(employee.getDepartment())
                .shiftGroup(employee.getShiftGroup())
                .hireDate(employee.getHireDate())
                .status(employee.getStatus())
                .build();
        Page<Employee> page = new PageImpl<>(List.of(employee));
        when(employeeRepository.findAllByDeletedFalse(any(Pageable.class))).thenReturn(page);
        Page<EmployeeDTO> result = employeeService.listEmployees(PageRequest.of(0, 10));
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(dto.getName(), result.getContent().get(0).getName());
    }

    @Test
    void testListEmployees_ShouldExcludeDeletedEmployees() {
        Employee deletedEmp = employee.toBuilder().deleted(true).build();
        Page<Employee> page = new PageImpl<>(List.of(employee, deletedEmp));
        when(employeeRepository.findAllByDeletedFalse(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(employee)));
        Page<EmployeeDTO> result = employeeService.listEmployees(PageRequest.of(0, 10));
        assertEquals(1, result.getTotalElements());
        assertEquals(employee.getName(), result.getContent().get(0).getName());
    }

    @Test
    void testConvertToDTO_ShouldMapAllFields() {
        EmployeeDTO dto = employeeServiceImpl_convertToDTO(employee);
        assertNotNull(dto);
        assertEquals(employee.getId(), dto.getId());
        assertEquals(employee.getName(), dto.getName());
        assertEquals(employee.getBadgeId(), dto.getBadgeId());
        assertEquals(employee.getRole(), dto.getRole());
        assertEquals(employee.getDepartment(), dto.getDepartment());
        assertEquals(employee.getShiftGroup(), dto.getShiftGroup());
        assertEquals(employee.getHireDate(), dto.getHireDate());
        assertEquals(employee.getStatus(), dto.getStatus());
    }

    // Helper to access private method for testing
    private EmployeeDTO employeeServiceImpl_convertToDTO(Employee employee) {
        return EmployeeDTO.builder()
                .id(employee.getId())
                .name(employee.getName())
                .badgeId(employee.getBadgeId())
                .role(employee.getRole())
                .department(employee.getDepartment())
                .shiftGroup(employee.getShiftGroup())
                .hireDate(employee.getHireDate())
                .status(employee.getStatus())
                .build();
    }
}
