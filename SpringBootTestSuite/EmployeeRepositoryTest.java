package com.example.warehouse.employee;

import com.example.warehouse.employee.model.Employee;
import com.example.warehouse.employee.repository.EmployeeRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class EmployeeRepositoryTest {
 @Autowired
 private EmployeeRepository employeeRepository;

 private Employee employee1, employee2, employee3;
 private Pageable pageable;

 @BeforeEach
 void setUp() {
 employee1 = Employee.builder()
 .badgeId("EMP001")
 .name("John Doe")
 .role("WORKER")
 .department("Warehouse")
 .shiftGroup("A")
 .hireDate(LocalDate.now().minusYears(1))
 .status("ACTIVE")
 .deleted(false)
 .email("john.doe@example.com")
 .phoneNumber("1234567890")
 .createdAt(LocalDateTime.now().minusYears(1))
 .updatedAt(LocalDateTime.now())
 .createdBy("admin")
 .updatedBy("admin")
 .build();
 employee2 = Employee.builder()
 .badgeId("EMP002")
 .name("Jane Smith")
 .role("SUPERVISOR")
 .department("Packing")
 .shiftGroup("B")
 .hireDate(LocalDate.now().minusMonths(6))
 .status("INACTIVE")
 .deleted(false)
 .email("jane.smith@example.com")
 .phoneNumber("0987654321")
 .createdAt(LocalDateTime.now().minusMonths(6))
 .updatedAt(LocalDateTime.now())
 .createdBy("admin")
 .updatedBy("admin")
 .build();
 employee3 = Employee.builder()
 .badgeId("EMP003")
 .name("Bob Lee")
 .role("WORKER")
 .department("Warehouse")
 .shiftGroup("A")
 .hireDate(LocalDate.now().minusDays(10))
 .status("ACTIVE")
 .deleted(true)
 .email("bob.lee@example.com")
 .phoneNumber("1112223333")
 .createdAt(LocalDateTime.now().minusDays(10))
 .updatedAt(LocalDateTime.now())
 .createdBy("admin")
 .updatedBy("admin")
 .build();
 employeeRepository.saveAll(Arrays.asList(employee1, employee2, employee3));
 pageable = PageRequest.of(0, 10);
 }

 @AfterEach
 void tearDown() {
 employeeRepository.deleteAll();
 }

 @Test
 @DisplayName("findByBadgeIdAndDeletedFalse returns employee if exists and not deleted")
 void testFindByBadgeIdAndDeletedFalse_Found() {
 Optional<Employee> result = employeeRepository.findByBadgeIdAndDeletedFalse("EMP001");
 assertTrue(result.isPresent());
 assertEquals("EMP001", result.get().getBadgeId());
 }

 @Test
 @DisplayName("findByBadgeIdAndDeletedFalse returns empty if deleted")
 void testFindByBadgeIdAndDeletedFalse_Deleted() {
 Optional<Employee> result = employeeRepository.findByBadgeIdAndDeletedFalse("EMP003");
 assertFalse(result.isPresent());
 }

 @Test
 @DisplayName("findAllByDeletedFalse returns only non-deleted employees")
 void testFindAllByDeletedFalse() {
 Page<Employee> page = employeeRepository.findAllByDeletedFalse(pageable);
 assertEquals(2, page.getTotalElements());
 assertTrue(page.getContent().stream().noneMatch(Employee::isDeleted));
 }

 @Test
 @DisplayName("findByDepartmentAndDeletedFalse returns correct employees")
 void testFindByDepartmentAndDeletedFalse() {
 Page<Employee> page = employeeRepository.findByDepartmentAndDeletedFalse("Warehouse", pageable);
 assertEquals(1, page.getTotalElements());
 assertEquals("EMP001", page.getContent().get(0).getBadgeId());
 }

 @Test
 @DisplayName("findByRoleAndDeletedFalse returns correct employees")
 void testFindByRoleAndDeletedFalse() {
 Page<Employee> page = employeeRepository.findByRoleAndDeletedFalse("WORKER", pageable);
 assertEquals(1, page.getTotalElements());
 assertEquals("EMP001", page.getContent().get(0).getBadgeId());
 }

 @Test
 @DisplayName("findByStatusAndDeletedFalse returns correct employees")
 void testFindByStatusAndDeletedFalse() {
 Page<Employee> page = employeeRepository.findByStatusAndDeletedFalse("ACTIVE", pageable);
 assertEquals(1, page.getTotalElements());
 assertEquals("EMP001", page.getContent().get(0).getBadgeId());
 }

 @Test
 @DisplayName("findByNameContainingIgnoreCaseAndDeletedFalse returns correct employees")
 void testFindByNameContainingIgnoreCaseAndDeletedFalse() {
 Page<Employee> page = employeeRepository.findByNameContainingIgnoreCaseAndDeletedFalse("john", pageable);
 assertEquals(1, page.getTotalElements());
 assertEquals("EMP001", page.getContent().get(0).getBadgeId());
 }

 @Test
 @DisplayName("countByDeletedFalse returns correct count")
 void testCountByDeletedFalse() {
 long count = employeeRepository.countByDeletedFalse();
 assertEquals(2, count);
 }

 @Test
 @DisplayName("countByDepartmentAndDeletedFalse returns correct count")
 void testCountByDepartmentAndDeletedFalse() {
 long count = employeeRepository.countByDepartmentAndDeletedFalse("Warehouse");
 assertEquals(1, count);
 }

 @Test
 @DisplayName("existsByBadgeIdAndDeletedFalse returns true if exists and not deleted")
 void testExistsByBadgeIdAndDeletedFalse_True() {
 assertTrue(employeeRepository.existsByBadgeIdAndDeletedFalse("EMP001"));
 }

 @Test
 @DisplayName("existsByBadgeIdAndDeletedFalse returns false if deleted")
 void testExistsByBadgeIdAndDeletedFalse_Deleted() {
 assertFalse(employeeRepository.existsByBadgeIdAndDeletedFalse("EMP003"));
 }

 @Test
 @DisplayName("findByShiftGroupAndDeletedFalse returns correct employees")
 void testFindByShiftGroupAndDeletedFalse() {
 Page<Employee> page = employeeRepository.findByShiftGroupAndDeletedFalse("A", pageable);
 assertEquals(1, page.getTotalElements());
 assertEquals("EMP001", page.getContent().get(0).getBadgeId());
 }

 @Test
 @DisplayName("findByBadgeIdAndDeletedFalse returns empty for non-existent badgeId")
 void testFindByBadgeIdAndDeletedFalse_NotFound() {
 Optional<Employee> result = employeeRepository.findByBadgeIdAndDeletedFalse("EMP999");
 assertFalse(result.isPresent());
 }

 @Test
 @DisplayName("findByNameContainingIgnoreCaseAndDeletedFalse returns empty for unmatched name")
 void testFindByNameContainingIgnoreCaseAndDeletedFalse_Empty() {
 Page<Employee> page = employeeRepository.findByNameContainingIgnoreCaseAndDeletedFalse("xyz", pageable);
 assertEquals(0, page.getTotalElements());
 }

 @Test
 @DisplayName("Handles empty string and null inputs gracefully")
 void testRepositoryMethods_NullAndEmptyInputs() {
 assertThrows(Exception.class, () -> employeeRepository.findByBadgeIdAndDeletedFalse(null));
 assertThrows(Exception.class, () -> employeeRepository.findByDepartmentAndDeletedFalse(null, pageable));
 assertThrows(Exception.class, () -> employeeRepository.findByRoleAndDeletedFalse(null, pageable));
 assertThrows(Exception.class, () -> employeeRepository.findByStatusAndDeletedFalse(null, pageable));
 assertThrows(Exception.class, () -> employeeRepository.findByNameContainingIgnoreCaseAndDeletedFalse(null, pageable));
 assertThrows(Exception.class, () -> employeeRepository.findByShiftGroupAndDeletedFalse(null, pageable));
 assertThrows(Exception.class, () -> employeeRepository.existsByBadgeIdAndDeletedFalse(null));
 assertThrows(Exception.class, () -> employeeRepository.countByDepartmentAndDeletedFalse(null));
 }

 @Test
 @DisplayName("Handles boundary conditions for badgeId (very long string)")
 void testFindByBadgeIdAndDeletedFalse_Boundary() {
 String longBadgeId = "E" + "X".repeat(255);
 Employee emp = Employee.builder().badgeId(longBadgeId).name("Test").deleted(false).build();
 employeeRepository.save(emp);
 Optional<Employee> result = employeeRepository.findByBadgeIdAndDeletedFalse(longBadgeId);
 assertTrue(result.isPresent());
 assertEquals(longBadgeId, result.get().getBadgeId());
 }

 @Test
 @DisplayName("Handles special characters in badgeId")
 void testFindByBadgeIdAndDeletedFalse_SpecialChars() {
 String specialBadgeId = "EMP@#$_!";
 Employee emp = Employee.builder().badgeId(specialBadgeId).name("Test").deleted(false).build();
 employeeRepository.save(emp);
 Optional<Employee> result = employeeRepository.findByBadgeIdAndDeletedFalse(specialBadgeId);
 assertTrue(result.isPresent());
 assertEquals(specialBadgeId, result.get().getBadgeId());
 }
}
