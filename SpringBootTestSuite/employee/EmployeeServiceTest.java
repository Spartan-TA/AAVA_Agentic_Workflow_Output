package com.warehouse.ems.employee;

import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.*;
import java.time.*;
import java.util.*;

import static org.mockito.Mockito.*;

@SpringBootTest
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private CacheManager cacheManager;

    @InjectMocks
    private EmployeeService employeeService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateEmployee_UniqueBadgeId_Success() {
        EmployeeCreateDto dto = new EmployeeCreateDto("B200", "John", "john@wh.com", "Logistics", "WORKER");
        when(employeeRepository.findByBadgeId("B200")).thenReturn(Optional.empty());
        when(employeeRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        EmployeeEntity result = employeeService.createEmployee(dto);
        Assertions.assertEquals("John", result.getName());
        verify(employeeRepository, times(1)).save(any());
    }

    @Test
    void testCreateEmployee_DuplicateBadgeId_ThrowsException() {
        EmployeeCreateDto dto = new EmployeeCreateDto("B123", "Alice", "alice@wh.com", "Logistics", "WORKER");
        when(employeeRepository.findByBadgeId("B123")).thenReturn(Optional.of(new EmployeeEntity()));
        Assertions.assertThrows(DuplicateBadgeIdException.class, () -> employeeService.createEmployee(dto));
        verify(employeeRepository, never()).save(any());
    }

    @Test
    void testUpdateEmployee_ValidId_UpdatesFields() {
        EmployeeUpdateDto dto = new EmployeeUpdateDto("Alice Updated", "alice@wh.com", "IT", "SUPERVISOR");
        EmployeeEntity existing = new EmployeeEntity(1L, "B123", "Alice", "alice@wh.com", "Logistics", "WORKER", LocalDate.now(), "ACTIVE", false, LocalDateTime.now(), LocalDateTime.now());
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(employeeRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        EmployeeEntity updated = employeeService.updateEmployee(1L, dto);
        Assertions.assertEquals("Alice Updated", updated.getName());
        Assertions.assertEquals("IT", updated.getDepartment());
        verify(employeeRepository, times(1)).save(any());
    }

    @Test
    void testDeleteEmployee_SoftDelete_SetsDeletedFlag() {
        EmployeeEntity existing = new EmployeeEntity(1L, "B123", "Alice", "alice@wh.com", "Logistics", "WORKER", LocalDate.now(), "ACTIVE", false, LocalDateTime.now(), LocalDateTime.now());
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(employeeRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        employeeService.deleteEmployee(1L);
        Assertions.assertTrue(existing.isDeleted());
        verify(employeeRepository, times(1)).save(any());
    }

    @Test
    void testGetEmployee_NotFound_ThrowsException() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());
        Assertions.assertThrows(EmployeeNotFoundException.class, () -> employeeService.getEmployee(99L));
    }

    @Test
    void testGetAllEmployees_Pagination_ReturnsPage() {
        Pageable pageable = PageRequest.of(0, 2);
        List<EmployeeEntity> employees = Arrays.asList(
            new EmployeeEntity(1L, "B123", "Alice", "alice@wh.com", "Logistics", "WORKER", LocalDate.now(), "ACTIVE", false, LocalDateTime.now(), LocalDateTime.now()),
            new EmployeeEntity(2L, "B124", "Bob", "bob@wh.com", "HR", "HR", LocalDate.now(), "ACTIVE", false, LocalDateTime.now(), LocalDateTime.now())
        );
        Page<EmployeeEntity> page = new PageImpl<>(employees, pageable, 2);
        when(employeeRepository.findAll(pageable)).thenReturn(page);
        Page<EmployeeEntity> result = employeeService.getAllEmployees(pageable);
        Assertions.assertEquals(2, result.getTotalElements());
    }

    @Test
    void testGetEmployeesByDepartment_Empty_ReturnsEmptyPage() {
        Pageable pageable = PageRequest.of(0, 2);
        when(employeeRepository.findAllByDepartment("Nonexistent", pageable)).thenReturn(Page.empty());
        Page<EmployeeEntity> result = employeeService.getEmployeesByDepartment("Nonexistent", pageable);
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    void testCreateEmployee_NullInput_ThrowsException() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> employeeService.createEmployee(null));
    }

    @Test
    void testUpdateEmployee_NullFields_UpdatesOnlyNonNull() {
        EmployeeUpdateDto dto = new EmployeeUpdateDto(null, null, null, null);
        EmployeeEntity existing = new EmployeeEntity(1L, "B123", "Alice", "alice@wh.com", "Logistics", "WORKER", LocalDate.now(), "ACTIVE", false, LocalDateTime.now(), LocalDateTime.now());
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(employeeRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        EmployeeEntity updated = employeeService.updateEmployee(1L, dto);
        Assertions.assertEquals("Alice", updated.getName());
    }

    @Test
    void testCreateEmployee_WithEmptyBadgeId_ThrowsException() {
        EmployeeCreateDto dto = new EmployeeCreateDto("", "John", "john@wh.com", "Logistics", "WORKER");
        Assertions.assertThrows(IllegalArgumentException.class, () -> employeeService.createEmployee(dto));
    }

    @Test
    void testCreateEmployee_WithNullName_ThrowsException() {
        EmployeeCreateDto dto = new EmployeeCreateDto("B200", null, "john@wh.com", "Logistics", "WORKER");
        Assertions.assertThrows(IllegalArgumentException.class, () -> employeeService.createEmployee(dto));
    }

    @Test
    void testUpdateEmployee_NonExistentEmployee_ThrowsException() {
        EmployeeUpdateDto dto = new EmployeeUpdateDto("Alice Updated", "alice@wh.com", "IT", "SUPERVISOR");
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());
        Assertions.assertThrows(EmployeeNotFoundException.class, () -> employeeService.updateEmployee(999L, dto));
    }

    @Test
    void testDeleteEmployee_NonExistentEmployee_ThrowsException() {
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());
        Assertions.assertThrows(EmployeeNotFoundException.class, () -> employeeService.deleteEmployee(999L));
    }

    @Test
    void testGetEmployee_ValidId_ReturnsEmployee() {
        EmployeeEntity employee = new EmployeeEntity(1L, "B123", "Alice", "alice@wh.com", "Logistics", "WORKER", LocalDate.now(), "ACTIVE", false, LocalDateTime.now(), LocalDateTime.now());
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        EmployeeEntity result = employeeService.getEmployee(1L);
        Assertions.assertEquals("Alice", result.getName());
    }

    @Test
    void testGetEmployeesByDepartment_ValidDepartment_ReturnsEmployees() {
        Pageable pageable = PageRequest.of(0, 10);
        List<EmployeeEntity> employees = Arrays.asList(
            new EmployeeEntity(1L, "B123", "Alice", "alice@wh.com", "Logistics", "WORKER", LocalDate.now(), "ACTIVE", false, LocalDateTime.now(), LocalDateTime.now())
        );
        Page<EmployeeEntity> page = new PageImpl<>(employees, pageable, 1);
        when(employeeRepository.findAllByDepartment("Logistics", pageable)).thenReturn(page);
        Page<EmployeeEntity> result = employeeService.getEmployeesByDepartment("Logistics", pageable);
        Assertions.assertEquals(1, result.getTotalElements());
        Assertions.assertEquals("Alice", result.getContent().get(0).getName());
    }