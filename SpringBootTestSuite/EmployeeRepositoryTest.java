package com.warehouse.employee.management.repository;

import com.warehouse.employee.management.entity.Employee;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class EmployeeRepositoryTest {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15.2")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    EmployeeRepository employeeRepository;

    Employee employee1, employee2, employee3;

    @BeforeEach
    void setUp() {
        employee1 = Employee.builder()
                .badgeId("BID1").firstName("John").lastName("Doe").email("john@ex.com")
                .role("WORKER").department("Logistics").shiftGroup("A")
                .hireDate(LocalDate.of(2020, 1, 1)).status("ACTIVE").deleted(false)
                .build();
        employee2 = Employee.builder()
                .badgeId("BID2").firstName("Jane").lastName("Smith").email("jane@ex.com")
                .role("HR").department("HR").shiftGroup("B")
                .hireDate(LocalDate.of(2021, 2, 2)).status("INACTIVE").deleted(false)
                .build();
        employee3 = Employee.builder()
                .badgeId("BID3").firstName("Bob").lastName("Brown").email("bob@ex.com")
                .role("SUPERVISOR").department("Logistics").shiftGroup("A")
                .hireDate(LocalDate.of(2022, 3, 3)).status("ACTIVE").deleted(true)
                .build();
        employeeRepository.save(employee1);
        employeeRepository.save(employee2);
        employeeRepository.save(employee3);
    }

    @AfterEach
    void tearDown() {
        employeeRepository.deleteAll();
    }

    @Test
    void testFindByBadgeId_Found() {
        Optional<Employee> found = employeeRepository.findByBadgeId("BID1");
        assertTrue(found.isPresent());
        assertEquals("John", found.get().getFirstName());
    }

    @Test
    void testFindByBadgeId_NotFound() {
        Optional<Employee> found = employeeRepository.findByBadgeId("NONEXIST");
        assertFalse(found.isPresent());
    }

    @Test
    void testFindAllActive_ExcludesDeleted() {
        Page<Employee> page = employeeRepository.findAllActive(PageRequest.of(0, 10));
        assertEquals(2, page.getTotalElements());
        assertTrue(page.getContent().stream().noneMatch(e -> e.getDeleted()));
    }

    @Test
    void testFindByStatus() {
        Page<Employee> page = employeeRepository.findByStatus("ACTIVE", PageRequest.of(0, 10));
        assertEquals(1, page.getTotalElements());
        assertEquals("John", page.getContent().get(0).getFirstName());
    }

    @Test
    void testFindByStatus_NoResults() {
        Page<Employee> page = employeeRepository.findByStatus("ON_LEAVE", PageRequest.of(0, 10));
        assertEquals(0, page.getTotalElements());
    }

    @Test
    void testFindByDepartment() {
        Page<Employee> page = employeeRepository.findByDepartment("Logistics", PageRequest.of(0, 10));
        assertEquals(1, page.getTotalElements());
        assertEquals("John", page.getContent().get(0).getFirstName());
    }

    @Test
    void testFindByDepartment_ExcludesDeleted() {
        Page<Employee> page = employeeRepository.findByDepartment("Logistics", PageRequest.of(0, 10));
        assertTrue(page.getContent().stream().noneMatch(e -> e.getDeleted()));
    }

    @Test
    void testSearchEmployees_ByFirstName() {
        Page<Employee> page = employeeRepository.searchEmployees("john", PageRequest.of(0, 10));
        assertEquals(1, page.getTotalElements());
        assertEquals("John", page.getContent().get(0).getFirstName());
    }

    @Test
    void testSearchEmployees_ByLastName() {
        Page<Employee> page = employeeRepository.searchEmployees("smith", PageRequest.of(0, 10));
        assertEquals(1, page.getTotalElements());
        assertEquals("Jane", page.getContent().get(0).getFirstName());
    }

    @Test
    void testSearchEmployees_ByDepartment() {
        Page<Employee> page = employeeRepository.searchEmployees("logistics", PageRequest.of(0, 10));
        assertEquals(1, page.getTotalElements());
    }

    @Test
    void testSearchEmployees_ByRole() {
        Page<Employee> page = employeeRepository.searchEmployees("hr", PageRequest.of(0, 10));
        assertEquals(1, page.getTotalElements());
        assertEquals("Jane", page.getContent().get(0).getFirstName());
    }

    @Test
    void testSearchEmployees_NoResults() {
        Page<Employee> page = employeeRepository.searchEmployees("nonexistent", PageRequest.of(0, 10));
        assertEquals(0, page.getTotalElements());
    }

    @Test
    void testPagination() {
        Page<Employee> page = employeeRepository.findAllActive(PageRequest.of(0, 1));
        assertEquals(2, page.getTotalElements());
        assertEquals(1, page.getContent().size());
    }

    @Test
    void testSaveAndDelete() {
        Employee emp = Employee.builder()
                .badgeId("BID4").firstName("Alice").lastName("Wonder").role("ADMIN").status("ACTIVE").deleted(false)
                .build();
        Employee saved = employeeRepository.save(emp);
        assertNotNull(saved.getId());
        employeeRepository.delete(saved);
        assertFalse(employeeRepository.findById(saved.getId()).isPresent());
    }
}
