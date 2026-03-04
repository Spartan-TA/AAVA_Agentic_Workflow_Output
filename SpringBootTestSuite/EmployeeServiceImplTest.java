package com.warehouse.ems.service.impl;

import com.warehouse.ems.domain.Employee;
import com.warehouse.ems.dto.EmployeeDTO;
import com.warehouse.ems.mapper.EmployeeMapper;
import com.warehouse.ems.repository.EmployeeRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.data.domain.*;
import java.time.LocalDate;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmployeeServiceImplTest {
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private EmployeeMapper employeeMapper;
    @InjectMocks
    private EmployeeServiceImpl employeeService;

    private Employee employee;
    private EmployeeDTO employeeDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        employee = Employee.builder()
                .id(1L)
                .badgeId("BADGE1")
                .name("Alice")
                .role("WORKER")
                .department("Logistics")
                .shiftGroup("A")
                .hireDate(LocalDate.of(2020, 1, 1))
                .status("ACTIVE")
                .deleted(false)
                .build();
        employeeDTO = EmployeeDTO.builder()
                .id(1L)
                .badgeId("BADGE1")
                .name("Alice")
                .role("WORKER")
                .department("Logistics")
                .shiftGroup("A")
                .hireDate(LocalDate.of(2020, 1, 1))
                .status("ACTIVE")
                .build();
    }

    @Test
    void testCreateEmployee_WithValidInput_ReturnsCreatedEmployee() {
        when(employeeRepository.existsByBadgeId("BADGE1")).thenReturn(false);
        when(employeeMapper.toEntity(employeeDTO)).thenReturn(employee);
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);
        when(employeeMapper.toDto(employee)).thenReturn(employeeDTO);
        EmployeeDTO result = employeeService.createEmployee(employeeDTO);
        assertEquals(employeeDTO, result);
        verify(employeeRepository).existsByBadgeId("BADGE1");
        verify(employeeRepository).save(any(Employee.class));
    }

    @Test
    void testCreateEmployee_WithDuplicateBadgeId_ThrowsException() {
        when(employeeRepository.existsByBadgeId("BADGE1")).thenReturn(true);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> employeeService.createEmployee(employeeDTO));
        assertTrue(ex.getMessage().contains("Badge ID already exists"));
        verify(employeeRepository).existsByBadgeId("BADGE1");
        verify(employeeRepository, never()).save(any());
    }

    @Test
    void testUpdateEmployee_WithValidInput_ReturnsUpdatedEmployee() {
        EmployeeDTO updatedDTO = EmployeeDTO.builder().id(1L).badgeId("BADGE1").name("Bob").role("HR").department("HR").shiftGroup("B").hireDate(LocalDate.of(2021, 2, 2)).status("INACTIVE").build();
        Employee updatedEmployee = Employee.builder().id(1L).badgeId("BADGE1").name("Bob").role("HR").department("HR").shiftGroup("B").hireDate(LocalDate.of(2021, 2, 2)).status("INACTIVE").deleted(false).build();
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(updatedEmployee);
        when(employeeMapper.toDto(updatedEmployee)).thenReturn(updatedDTO);
        EmployeeDTO result = employeeService.updateEmployee(1L, updatedDTO);
        assertEquals(updatedDTO, result);
        verify(employeeRepository).findById(1L);
        verify(employeeRepository).save(any(Employee.class));
    }

    @Test
    void testUpdateEmployee_NonExistentId_ThrowsException() {
        when(employeeRepository.findById(2L)).thenReturn(Optional.empty());
        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class, () -> employeeService.updateEmployee(2L, employeeDTO));
        assertTrue(ex.getMessage().contains("Employee not found with ID: 2"));
        verify(employeeRepository).findById(2L);
    }

    @Test
    void testGetEmployee_ActiveEmployee_ReturnsDTO() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeMapper.toDto(employee)).thenReturn(employeeDTO);
        Optional<EmployeeDTO> result = employeeService.getEmployee(1L);
        assertTrue(result.isPresent());
        assertEquals(employeeDTO, result.get());
    }

    @Test
    void testGetEmployee_DeletedEmployee_ReturnsEmpty() {
        Employee deleted = Employee.builder().id(2L).badgeId("BADGE2").deleted(true).build();
        when(employeeRepository.findById(2L)).thenReturn(Optional.of(deleted));
        Optional<EmployeeDTO> result = employeeService.getEmployee(2L);
        assertFalse(result.isPresent());
    }

    @Test
    void testGetEmployee_NonExistent_ReturnsEmpty() {
        when(employeeRepository.findById(3L)).thenReturn(Optional.empty());
        Optional<EmployeeDTO> result = employeeService.getEmployee(3L);
        assertFalse(result.isPresent());
    }

    @Test
    void testGetAllEmployees_ReturnsPageOfDTOs() {
        Pageable pageable = PageRequest.of(0, 2);
        List<Employee> employees = List.of(employee);
        Page<Employee> page = new PageImpl<>(employees, pageable, 1);
        when(employeeRepository.findAll(pageable)).thenReturn(page);
        when(employeeMapper.toDto(employee)).thenReturn(employeeDTO);
        Page<EmployeeDTO> result = employeeService.getAllEmployees(pageable);
        assertEquals(1, result.getTotalElements());
        assertEquals(employeeDTO, result.getContent().get(0));
    }

    @Test
    void testGetAllEmployees_EmptyPage() {
        Pageable pageable = PageRequest.of(0, 2);
        Page<Employee> page = new PageImpl<>(List.of(), pageable, 0);
        when(employeeRepository.findAll(pageable)).thenReturn(page);
        Page<EmployeeDTO> result = employeeService.getAllEmployees(pageable);
        assertEquals(0, result.getTotalElements());
    }

    @Test
    void testDeleteEmployee_SoftDeletesAndTerminates() {
        Employee emp = Employee.builder().id(1L).badgeId("BADGE1").deleted(false).status("ACTIVE").build();
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(emp));
        when(employeeRepository.save(any(Employee.class))).thenReturn(emp);
        employeeService.deleteEmployee(1L);
        assertTrue(emp.getDeleted());
        assertEquals("TERMINATED", emp.getStatus());
        verify(employeeRepository).save(emp);
    }

    @Test
    void testDeleteEmployee_NonExistent_ThrowsException() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());
        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class, () -> employeeService.deleteEmployee(99L));
        assertTrue(ex.getMessage().contains("Employee not found with ID: 99"));
    }

    @Test
    void testCreateEmployee_BoundaryBadgeIdLength() {
        String badgeId = "B".repeat(32);
        EmployeeDTO dto = EmployeeDTO.builder().badgeId(badgeId).name("Boundary").role("WORKER").status("ACTIVE").build();
        Employee emp = Employee.builder().badgeId(badgeId).name("Boundary").role("WORKER").status("ACTIVE").deleted(false).build();
        when(employeeRepository.existsByBadgeId(badgeId)).thenReturn(false);
        when(employeeMapper.toEntity(dto)).thenReturn(emp);
        when(employeeRepository.save(any(Employee.class))).thenReturn(emp);
        when(employeeMapper.toDto(emp)).thenReturn(dto);
        EmployeeDTO result = employeeService.createEmployee(dto);
        assertEquals(badgeId, result.getBadgeId());
    }

    @Test
    void testUpdateEmployee_ConcurrentModification() {
        Employee emp = Employee.builder().id(1L).badgeId("BADGE1").name("Alice").build();
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(emp));
        when(employeeRepository.save(any(Employee.class))).thenThrow(new RuntimeException("Concurrent update"));
        EmployeeDTO dto = EmployeeDTO.builder().id(1L).badgeId("BADGE1").name("Alice").build();
        RuntimeException ex = assertThrows(RuntimeException.class, () -> employeeService.updateEmployee(1L, dto));
        assertEquals("Concurrent update", ex.getMessage());
    }

    @Test
    void testCreateEmployee_XssAndSqlInjection() {
        EmployeeDTO dto = EmployeeDTO.builder().badgeId("<script>alert('xss')</script>").name("'; DROP TABLE employee; --").role("WORKER").status("ACTIVE").build();
        Employee emp = Employee.builder().badgeId("<script>alert('xss')</script>").name("'; DROP TABLE employee; --").role("WORKER").status("ACTIVE").deleted(false).build();
        when(employeeRepository.existsByBadgeId(dto.getBadgeId())).thenReturn(false);
        when(employeeMapper.toEntity(dto)).thenReturn(emp);
        when(employeeRepository.save(any(Employee.class))).thenReturn(emp);
        when(employeeMapper.toDto(emp)).thenReturn(dto);
        EmployeeDTO result = employeeService.createEmployee(dto);
        assertEquals("<script>alert('xss')</script>", result.getBadgeId());
        assertEquals("'; DROP TABLE employee; --", result.getName());
    }
}
