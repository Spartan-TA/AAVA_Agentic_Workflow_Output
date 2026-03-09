package com.company.wems.employee.repository;

import com.company.wems.employee.entity.Employee;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.data.domain.*;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeRepositoryTest {
    @Mock
    private EmployeeRepository employeeRepository;

    private Employee employee;

    @BeforeEach
    void setUp() {
        employee = Employee.builder()
                .id(1L)
                .badgeId("EMP001")
                .name("John Doe")
                .role("WORKER")
                .department("Warehouse")
                .hireDate(LocalDate.now())
                .status("ACTIVE")
                .deleted(false)
                .build();
    }

    @Test
    @DisplayName("Should save and retrieve employee")
    void testSaveAndRetrieveEmployee_Success() {
        when(employeeRepository.save(employee)).thenReturn(employee);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        Employee saved = employeeRepository.save(employee);
        Optional<Employee> found = employeeRepository.findById(1L);

        assertAll(
                () -> assertNotNull(saved),
                () -> assertTrue(found.isPresent()),
                () -> assertEquals("EMP001", found.get().getBadgeId())
        );
    }

    @Test
    @DisplayName("Should find by badgeId and deleted false (valid)")
    void testFindByBadgeIdAndDeletedFalse_Valid() {
        when(employeeRepository.findByBadgeIdAndDeletedFalse("EMP001")).thenReturn(Optional.of(employee));
        Optional<Employee> found = employeeRepository.findByBadgeIdAndDeletedFalse("EMP001");
        assertTrue(found.isPresent());
        assertEquals("EMP001", found.get().getBadgeId());
    }

    @Test
    @DisplayName("Should not find by badgeId and deleted false (invalid)")
    void testFindByBadgeIdAndDeletedFalse_Invalid() {
        when(employeeRepository.findByBadgeIdAndDeletedFalse("INVALID")).thenReturn(Optional.empty());
        Optional<Employee> found = employeeRepository.findByBadgeIdAndDeletedFalse("INVALID");
        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("Should check existence by badgeId and deleted false (exists)")
    void testExistsByBadgeIdAndDeletedFalse_Exists() {
        when(employeeRepository.existsByBadgeIdAndDeletedFalse("EMP001")).thenReturn(true);
        assertTrue(employeeRepository.existsByBadgeIdAndDeletedFalse("EMP001"));
    }

    @Test
    @DisplayName("Should check existence by badgeId and deleted false (not exists)")
    void testExistsByBadgeIdAndDeletedFalse_NotExists() {
        when(employeeRepository.existsByBadgeIdAndDeletedFalse("EMP999")).thenReturn(false);
        assertFalse(employeeRepository.existsByBadgeIdAndDeletedFalse("EMP999"));
    }

    @Test
    @DisplayName("Should not find soft deleted employee")
    void testFindByBadgeIdAndDeletedFalse_SoftDeleted() {
        Employee deletedEmp = Employee.builder()
                .id(2L)
                .badgeId("EMP002")
                .name("Jane Doe")
                .role("WORKER")
                .department("Warehouse")
                .hireDate(LocalDate.now())
                .status("ACTIVE")
                .deleted(true)
                .build();
        when(employeeRepository.findByBadgeIdAndDeletedFalse("EMP002")).thenReturn(Optional.empty());
        Optional<Employee> found = employeeRepository.findByBadgeIdAndDeletedFalse("EMP002");
        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("Should paginate and sort employees")
    void testPaginationAndSorting() {
        List<Employee> employees = Arrays.asList(employee,
                Employee.builder().id(2L).badgeId("EMP002").name("Jane").role("MANAGER").department("Warehouse").hireDate(LocalDate.now()).status("ACTIVE").deleted(false).build()
        );
        Page<Employee> page = new PageImpl<>(employees);
        Pageable pageable = PageRequest.of(0, 2, Sort.by("name").ascending());
        when(employeeRepository.findAll(pageable)).thenReturn(page);
        Page<Employee> result = employeeRepository.findAll(pageable);
        assertEquals(2, result.getContent().size());
        assertEquals("EMP001", result.getContent().get(0).getBadgeId());
    }
}
