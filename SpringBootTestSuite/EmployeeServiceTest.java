package com.example.warehouse.employee;

import com.example.warehouse.audit.AuditService;
import com.example.warehouse.employee.model.Employee;
import com.example.warehouse.employee.repository.EmployeeRepository;
import com.example.warehouse.employee.service.EmployeeService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {
 @Mock
 private EmployeeRepository employeeRepository;
 @Mock
 private AuditService auditService;
 @InjectMocks
 private EmployeeService employeeService;

 private Employee validEmployee;
 private Pageable pageable;

 @BeforeEach
 void setUp() {
 validEmployee = Employee.builder()
 .id(1L)
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
 pageable = PageRequest.of(0, 10);
 }

 @AfterEach
 void tearDown() {
 Mockito.reset(employeeRepository, auditService);
 }

 @Test
 @DisplayName("Should create employee with valid data")
 void testCreateEmployee_ValidData() {
 when(employeeRepository.existsByBadgeIdAndDeletedFalse("EMP001")).thenReturn(false);
 when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);
 Employee result = employeeService.create(validEmployee);
 assertNotNull(result);
 assertEquals("EMP001", result.getBadgeId());
 verify(employeeRepository).save(any(Employee.class));
 verify(auditService).logCreate(any(), any());
 }

 @Test
 @DisplayName("Should throw exception when badge ID already exists")
 void testCreateEmployee_DuplicateBadgeId() {
 when(employeeRepository.existsByBadgeIdAndDeletedFalse("EMP001")).thenReturn(true);
 assertThrows(IllegalArgumentException.class, () -> employeeService.create(validEmployee));
 }

 @Test
 @DisplayName("Should throw exception when employee is null")
 void testCreateEmployee_NullEmployee() {
 assertThrows(IllegalArgumentException.class, () -> employeeService.create(null));
 }

 @Test
 @DisplayName("Should update employee with valid data")
 void testUpdateEmployee_ValidData() {
 Employee updated = Employee.builder().id(1L).badgeId("EMP001").name("Jane Doe").build();
 when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
 when(employeeRepository.save(any(Employee.class))).thenReturn(updated);
 Employee result = employeeService.update(1L, updated);
 assertNotNull(result);
 assertEquals("Jane Doe", result.getName());
 verify(auditService).logUpdate(any(), any(), any());
 }

 @Test
 @DisplayName("Should throw exception when updating non-existent employee")
 void testUpdateEmployee_NotFound() {
 Employee updated = Employee.builder().id(1L).badgeId("EMP001").name("Jane Doe").build();
 when(employeeRepository.findById(1L)).thenReturn(Optional.empty());
 assertThrows(NoSuchElementException.class, () -> employeeService.update(1L, updated));
 }

 @Test
 @DisplayName("Should soft delete employee")
 void testSoftDeleteEmployee() {
 when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
 employeeService.softDelete(1L);
 verify(employeeRepository).save(argThat(emp -> emp.isDeleted()));
 verify(auditService).logDelete(any(), any());
 }

 @Test
 @DisplayName("Should throw exception when soft deleting non-existent employee")
 void testSoftDeleteEmployee_NotFound() {
 when(employeeRepository.findById(1L)).thenReturn(Optional.empty());
 assertThrows(NoSuchElementException.class, () -> employeeService.softDelete(1L));
 }

 @Test
 @DisplayName("Should return paginated list of active employees")
 void testListEmployees() {
 Page<Employee> page = new PageImpl<>(Collections.singletonList(validEmployee));
 when(employeeRepository.findAllByDeletedFalse(pageable)).thenReturn(page);
 Page<Employee> result = employeeService.list(pageable);
 assertNotNull(result);
 assertEquals(1, result.getTotalElements());
 }

 @Test
 @DisplayName("Should search employees by name, department, role, status")
 void testSearchEmployees() {
 Page<Employee> page = new PageImpl<>(Collections.singletonList(validEmployee));
 when(employeeRepository.findByNameContainingIgnoreCaseAndDeletedFalse(eq("John"), eq(pageable))).thenReturn(page);
 Page<Employee> result = employeeService.search("John", null, null, null, pageable);
 assertNotNull(result);
 assertEquals(1, result.getTotalElements());
 }

 @Test
 @DisplayName("Should find employee by badgeId")
 void testFindByBadgeId() {
 when(employeeRepository.findByBadgeIdAndDeletedFalse("EMP001")).thenReturn(Optional.of(validEmployee));
 Optional<Employee> result = employeeService.findByBadgeId("EMP001");
 assertTrue(result.isPresent());
 assertEquals("EMP001", result.get().getBadgeId());
 }

 @Test
 @DisplayName("Should return empty when badgeId not found")
 void testFindByBadgeId_NotFound() {
 when(employeeRepository.findByBadgeIdAndDeletedFalse("EMP999")).thenReturn(Optional.empty());
 Optional<Employee> result = employeeService.findByBadgeId("EMP999");
 assertFalse(result.isPresent());
 }

 @Test
 @DisplayName("Should find employee by id")
 void testFindById() {
 when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
 Optional<Employee> result = employeeService.findById(1L);
 assertTrue(result.isPresent());
 assertEquals(1L, result.get().getId());
 }

 @Test
 @DisplayName("Should return empty when id not found")
 void testFindById_NotFound() {
 when(employeeRepository.findById(2L)).thenReturn(Optional.empty());
 Optional<Employee> result = employeeService.findById(2L);
 assertFalse(result.isPresent());
 }

 @Test
 @DisplayName("Should count active employees")
 void testCountActiveEmployees() {
 when(employeeRepository.countByDeletedFalse()).thenReturn(5L);
 long count = employeeService.countActiveEmployees();
 assertEquals(5L, count);
 }

 @Test
 @DisplayName("Should count employees by department")
 void testCountByDepartment() {
 when(employeeRepository.countByDepartmentAndDeletedFalse("Warehouse")).thenReturn(3L);
 long count = employeeService.countByDepartment("Warehouse");
 assertEquals(3L, count);
 }

 @Test
 @DisplayName("Should check badgeId availability - available")
 void testIsBadgeIdAvailable_True() {
 when(employeeRepository.existsByBadgeIdAndDeletedFalse("EMP002")).thenReturn(false);
 assertTrue(employeeService.isBadgeIdAvailable("EMP002"));
 }

 @Test
 @DisplayName("Should check badgeId availability - not available")
 void testIsBadgeIdAvailable_False() {
 when(employeeRepository.existsByBadgeIdAndDeletedFalse("EMP001")).thenReturn(true);
 assertFalse(employeeService.isBadgeIdAvailable("EMP001"));
 }

 @Test
 @DisplayName("Should handle null and empty badgeId in isBadgeIdAvailable")
 void testIsBadgeIdAvailable_NullOrEmpty() {
 assertThrows(IllegalArgumentException.class, () -> employeeService.isBadgeIdAvailable(null));
 assertThrows(IllegalArgumentException.class, () -> employeeService.isBadgeIdAvailable(""));
 }

 @Test
 @DisplayName("Should handle boundary conditions for badgeId (very long string)")
 void testCreateEmployee_BadgeIdBoundary() {
 String longBadgeId = "E" + "X".repeat(255);
 Employee emp = Employee.builder().badgeId(longBadgeId).name("Test").build();
 when(employeeRepository.existsByBadgeIdAndDeletedFalse(longBadgeId)).thenReturn(false);
 when(employeeRepository.save(any(Employee.class))).thenReturn(emp);
 Employee result = employeeService.create(emp);
 assertEquals(longBadgeId, result.getBadgeId());
 }

 @Test
 @DisplayName("Should handle special characters in badgeId")
 void testCreateEmployee_BadgeIdSpecialChars() {
 String specialBadgeId = "EMP@#$_!";
 Employee emp = Employee.builder().badgeId(specialBadgeId).name("Test").build();
 when(employeeRepository.existsByBadgeIdAndDeletedFalse(specialBadgeId)).thenReturn(false);
 when(employeeRepository.save(any(Employee.class))).thenReturn(emp);
 Employee result = employeeService.create(emp);
 assertEquals(specialBadgeId, result.getBadgeId());
 }
}
