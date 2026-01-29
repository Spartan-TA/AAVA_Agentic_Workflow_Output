package com.example.warehouseems.repository;

import com.example.warehouseems.model.Employee;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
class EmployeeRepositoryTest {
    @Autowired
    private EmployeeRepository employeeRepository;

    private Employee employee1;
    private Employee employee2;
    private Employee employeeDeleted;

    @BeforeEach
    void setUp() {
        employee1 = Employee.builder()
                .name("John Doe")
                .email("john.doe@example.com")
                .badgeId("BADGE123")
                .department("Logistics")
                .deleted(false)
                .build();
        employee2 = Employee.builder()
                .name("Jane Smith")
                .email("jane.smith@example.com")
                .badgeId("BADGE124")
                .department("Packing")
                .deleted(false)
                .build();
        employeeDeleted = Employee.builder()
                .name("Deleted Emp")
                .email("deleted@example.com")
                .badgeId("BADGE999")
                .department("Logistics")
                .deleted(true)
                .build();
        employeeRepository.saveAll(Arrays.asList(employee1, employee2, employeeDeleted));
    }

    @AfterEach
    void tearDown() {
        employeeRepository.deleteAll();
    }

    @Test
    @DisplayName("findByBadgeIdAndDeletedFalse_ExistingBadgeId_Success")
    void testFindByBadgeIdAndDeletedFalse_ExistingBadgeId_Success() {
        Optional<Employee> found = employeeRepository.findByBadgeIdAndDeletedFalse("BADGE123");
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("John Doe");
    }

    @Test
    @DisplayName("findByBadgeIdAndDeletedFalse_DeletedEmployee_Empty")
    void testFindByBadgeIdAndDeletedFalse_DeletedEmployee_Empty() {
        Optional<Employee> found = employeeRepository.findByBadgeIdAndDeletedFalse("BADGE999");
        assertThat(found).isNotPresent();
    }

    @Test
    @DisplayName("findByBadgeIdAndDeletedFalse_NonExistingBadgeId_Empty")
    void testFindByBadgeIdAndDeletedFalse_NonExistingBadgeId_Empty() {
        Optional<Employee> found = employeeRepository.findByBadgeIdAndDeletedFalse("BADGE000");
        assertThat(found).isNotPresent();
    }

    @Test
    @DisplayName("findAllByDeletedFalse_Normal_Success")
    void testFindAllByDeletedFalse_Normal_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> page = employeeRepository.findAllByDeletedFalse(pageable);
        assertThat(page.getContent()).hasSize(2);
    }

    @Test
    @DisplayName("findAllByDeletedFalse_EmptyResult_EmptyPage")
    void testFindAllByDeletedFalse_EmptyResult_EmptyPage() {
        employeeRepository.deleteAll();
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> page = employeeRepository.findAllByDeletedFalse(pageable);
        assertThat(page.getContent()).isEmpty();
    }

    @Test
    @DisplayName("findByDepartment_Normal_Success")
    void testFindByDepartment_Normal_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> page = employeeRepository.findByDepartment("Logistics", pageable);
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getName()).isEqualTo("John Doe");
    }

    @Test
    @DisplayName("findByDepartment_EmptyDepartment_EmptyPage")
    void testFindByDepartment_EmptyDepartment_EmptyPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> page = employeeRepository.findByDepartment("", pageable);
        assertThat(page.getContent()).isEmpty();
    }

    @Test
    @DisplayName("findByDepartment_SpecialCharacters_SuccessOrEmpty")
    void testFindByDepartment_SpecialCharacters_SuccessOrEmpty() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> page = employeeRepository.findByDepartment("<script>", pageable);
        assertThat(page.getContent()).isEmpty();
    }

    @Test
    @DisplayName("findByDepartment_SQLInjectionAttempt_EmptyPage")
    void testFindByDepartment_SQLInjectionAttempt_EmptyPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> page = employeeRepository.findByDepartment("Logistics'; DROP TABLE Employees;--", pageable);
        assertThat(page.getContent()).isEmpty();
    }
}
