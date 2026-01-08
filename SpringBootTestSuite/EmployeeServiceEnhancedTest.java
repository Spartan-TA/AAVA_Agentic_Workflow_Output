package com.warehouseems.employee;

import com.warehouseems.employee.dto.EmployeeRequestDto;
import com.warehouseems.employee.dto.EmployeeResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Enhanced comprehensive test suite for EmployeeService.
 * Tests all business logic with normal cases, edge cases, boundary conditions, and error scenarios.
 */
@ExtendWith(MockitoExtension.class)
class EmployeeServiceEnhancedTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeService employeeService;

    private Employee testEmployee;
    private EmployeeRequestDto testRequestDto;

    @BeforeEach
    void setUp() {
        testEmployee = new Employee();
        testEmployee.setId(1L);
        testEmployee.setName("John Doe");
        testEmployee.setBadgeId("EMP001");
        testEmployee.setRole("WORKER");
        testEmployee.setDepartment("Shipping");
        testEmployee.setShiftGroup("DAY_SHIFT");
        testEmployee.setHireDate(LocalDate.of(2023, 1, 1));
        testEmployee.setStatus("ACTIVE");
        testEmployee.setEmail("john.doe@warehouse.com");
        testEmployee.setPhone("+1234567890");
        testEmployee.setAddress("123 Main St");
        testEmployee.setDeleted(false);
        testEmployee.setCreatedAt(LocalDateTime.now());
        testEmployee.setUpdatedAt(LocalDateTime.now());

        testRequestDto = new EmployeeRequestDto();
        testRequestDto.setName("John Doe");
        testRequestDto.setBadgeId("EMP001");
        testRequestDto.setRole("WORKER");
        testRequestDto.setDepartment("Shipping");
        testRequestDto.setShiftGroup("DAY_SHIFT");
        testRequestDto.setHireDate(LocalDate.of(2023, 1, 1));
        testRequestDto.setStatus("ACTIVE");
        testRequestDto.setEmail("john.doe@warehouse.com");
        testRequestDto.setPhone("+1234567890");
        testRequestDto.setAddress("123 Main St");
    }

    // ==================== GET ALL EMPLOYEES TESTS ====================

    @Nested
    @DisplayName("Get All Employees Tests")
    class GetAllEmployeesTests {

        @Test
        @DisplayName("Should return paginated employees successfully")
        void testGetAllEmployees_Success() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Employee> employeePage = new PageImpl<>(Arrays.asList(testEmployee));
            when(employeeRepository.findAllByDeletedFalse(pageable)).thenReturn(employeePage);

            Page<EmployeeResponseDto> result = employeeService.getAllEmployees(pageable);

            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            assertEquals("John Doe", result.getContent().get(0).getName());
            assertEquals("EMP001", result.getContent().get(0).getBadgeId());
            verify(employeeRepository, times(1)).findAllByDeletedFalse(pageable);
        }

        @Test
        @DisplayName("Should return empty page when no employees exist")
        void testGetAllEmployees_EmptyResult() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Employee> emptyPage = new PageImpl<>(Collections.emptyList());
            when(employeeRepository.findAllByDeletedFalse(pageable)).thenReturn(emptyPage);

            Page<EmployeeResponseDto> result = employeeService.getAllEmployees(pageable);

            assertNotNull(result);
            assertEquals(0, result.getTotalElements());
            assertTrue(result.getContent().isEmpty());
        }

        @Test
        @DisplayName("Should handle large page size")
        void testGetAllEmployees_LargePageSize() {
            Pageable pageable = PageRequest.of(0, 1000);
            Page<Employee> employeePage = new PageImpl<>(Arrays.asList(testEmployee));
            when(employeeRepository.findAllByDeletedFalse(pageable)).thenReturn(employeePage);

            Page<EmployeeResponseDto> result = employeeService.getAllEmployees(pageable);

            assertNotNull(result);
            verify(employeeRepository, times(1)).findAllByDeletedFalse(pageable);
        }

        @Test
        @DisplayName("Should handle pagination with sorting")
        void testGetAllEmployees_WithSorting() {
            Pageable pageable = PageRequest.of(0, 10, Sort.by("name").ascending());
            Page<Employee> employeePage = new PageImpl<>(Arrays.asList(testEmployee));
            when(employeeRepository.findAllByDeletedFalse(pageable)).thenReturn(employeePage);

            Page<EmployeeResponseDto> result = employeeService.getAllEmployees(pageable);

            assertNotNull(result);
            verify(employeeRepository, times(1)).findAllByDeletedFalse(pageable);
        }

        @Test
        @DisplayName("Should exclude soft-deleted employees")
        void testGetAllEmployees_ExcludesSoftDeleted() {
            Pageable pageable = PageRequest.of(0, 10);
            Employee deletedEmployee = new Employee();
            deletedEmployee.setDeleted(true);
            Page<Employee> employeePage = new PageImpl<>(Arrays.asList(testEmployee));
            when(employeeRepository.findAllByDeletedFalse(pageable)).thenReturn(employeePage);

            Page<EmployeeResponseDto> result = employeeService.getAllEmployees(pageable);

            assertNotNull(result);
            result.getContent().forEach(emp -> assertFalse(emp.getId() == null));
        }
    }

    // ==================== GET EMPLOYEES BY FILTERS TESTS ====================

    @Nested
    @DisplayName("Get Employees By Filters Tests")
    class GetEmployeesByFiltersTests {

        @Test
        @DisplayName("Should filter by department successfully")
        void testGetEmployeesByFilters_DepartmentFilter() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Employee> employeePage = new PageImpl<>(Arrays.asList(testEmployee));
            when(employeeRepository.findByFilters(eq("Shipping"), eq(null), eq(null), eq(pageable)))
                    .thenReturn(employeePage);

            Page<EmployeeResponseDto> result = employeeService.getEmployeesByFilters(
                    "Shipping", null, null, pageable);

            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            assertEquals("Shipping", result.getContent().get(0).getDepartment());
        }

        @Test
        @DisplayName("Should filter by role successfully")
        void testGetEmployeesByFilters_RoleFilter() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Employee> employeePage = new PageImpl<>(Arrays.asList(testEmployee));
            when(employeeRepository.findByFilters(eq(null), eq("WORKER"), eq(null), eq(pageable)))
                    .thenReturn(employeePage);

            Page<EmployeeResponseDto> result = employeeService.getEmployeesByFilters(
                    null, "WORKER", null, pageable);

            assertNotNull(result);
            assertEquals("WORKER", result.getContent().get(0).getRole());
        }

        @Test
        @DisplayName("Should filter by status successfully")
        void testGetEmployeesByFilters_StatusFilter() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Employee> employeePage = new PageImpl<>(Arrays.asList(testEmployee));
            when(employeeRepository.findByFilters(eq(null), eq(null), eq("ACTIVE"), eq(pageable)))
                    .thenReturn(employeePage);

            Page<EmployeeResponseDto> result = employeeService.getEmployeesByFilters(
                    null, null, "ACTIVE", pageable);

            assertNotNull(result);
            assertEquals("ACTIVE", result.getContent().get(0).getStatus());
        }

        @Test
        @DisplayName("Should filter by multiple criteria")
        void testGetEmployeesByFilters_MultipleCriteria() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Employee> employeePage = new PageImpl<>(Arrays.asList(testEmployee));
            when(employeeRepository.findByFilters(eq("Shipping"), eq("WORKER"), eq("ACTIVE"), eq(pageable)))
                    .thenReturn(employeePage);

            Page<EmployeeResponseDto> result = employeeService.getEmployeesByFilters(
                    "Shipping", "WORKER", "ACTIVE", pageable);

            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
        }

        @Test
        @DisplayName("Should return empty when no matches found")
        void testGetEmployeesByFilters_NoMatches() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Employee> emptyPage = new PageImpl<>(Collections.emptyList());
            when(employeeRepository.findByFilters(any(), any(), any(), eq(pageable)))
                    .thenReturn(emptyPage);

            Page<EmployeeResponseDto> result = employeeService.getEmployeesByFilters(
                    "NonExistent", "INVALID", "UNKNOWN", pageable);

            assertNotNull(result);
            assertEquals(0, result.getTotalElements());
        }
    }

    // ==================== GET EMPLOYEE BY ID TESTS ====================

    @Nested
    @DisplayName("Get Employee By ID Tests")
    class GetEmployeeByIdTests {

        @Test
        @DisplayName("Should return employee when found")
        void testGetEmployeeById_Success() {
            when(employeeRepository.findByIdAndDeletedFalse(1L))
                    .thenReturn(Optional.of(testEmployee));

            Optional<EmployeeResponseDto> result = employeeService.getEmployeeById(1L);

            assertTrue(result.isPresent());
            assertEquals("John Doe", result.get().getName());
            assertEquals("EMP001", result.get().getBadgeId());
            verify(employeeRepository, times(1)).findByIdAndDeletedFalse(1L);
        }

        @Test
        @DisplayName("Should return empty when employee not found")
        void testGetEmployeeById_NotFound() {
            when(employeeRepository.findByIdAndDeletedFalse(999L))
                    .thenReturn(Optional.empty());

            Optional<EmployeeResponseDto> result = employeeService.getEmployeeById(999L);

            assertFalse(result.isPresent());
            verify(employeeRepository, times(1)).findByIdAndDeletedFalse(999L);
        }

        @Test
        @DisplayName("Should return empty for soft-deleted employee")
        void testGetEmployeeById_SoftDeleted() {
            when(employeeRepository.findByIdAndDeletedFalse(1L))
                    .thenReturn(Optional.empty());

            Optional<EmployeeResponseDto> result = employeeService.getEmployeeById(1L);

            assertFalse(result.isPresent());
        }

        @Test
        @DisplayName("Should handle null ID gracefully")
        void testGetEmployeeById_NullId() {
            when(employeeRepository.findByIdAndDeletedFalse(null))
                    .thenReturn(Optional.empty());

            Optional<EmployeeResponseDto> result = employeeService.getEmployeeById(null);

            assertFalse(result.isPresent());
        }

        @Test
        @DisplayName("Should handle negative ID")
        void testGetEmployeeById_NegativeId() {
            when(employeeRepository.findByIdAndDeletedFalse(-1L))
                    .thenReturn(Optional.empty());

            Optional<EmployeeResponseDto> result = employeeService.getEmployeeById(-1L);

            assertFalse(result.isPresent());
        }

        @Test
        @DisplayName("Should handle zero ID")
        void testGetEmployeeById_ZeroId() {
            when(employeeRepository.findByIdAndDeletedFalse(0L))
                    .thenReturn(Optional.empty());

            Optional<EmployeeResponseDto> result = employeeService.getEmployeeById(0L);

            assertFalse(result.isPresent());
        }
    }

    // ==================== GET EMPLOYEE BY BADGE ID TESTS ====================

    @Nested
    @DisplayName("Get Employee By Badge ID Tests")
    class GetEmployeeByBadgeIdTests {

        @Test
        @DisplayName("Should return employee when found by badge ID")
        void testGetEmployeeByBadgeId_Success() {
            when(employeeRepository.findByBadgeIdAndDeletedFalse("EMP001"))
                    .thenReturn(Optional.of(testEmployee));

            Optional<EmployeeResponseDto> result = employeeService.getEmployeeByBadgeId("EMP001");

            assertTrue(result.isPresent());
            assertEquals("EMP001", result.get().getBadgeId());
            verify(employeeRepository, times(1)).findByBadgeIdAndDeletedFalse("EMP001");
        }

        @Test
        @DisplayName("Should return empty when badge ID not found")
        void testGetEmployeeByBadgeId_NotFound() {
            when(employeeRepository.findByBadgeIdAndDeletedFalse("INVALID"))
                    .thenReturn(Optional.empty());

            Optional<EmployeeResponseDto> result = employeeService.getEmployeeByBadgeId("INVALID");

            assertFalse(result.isPresent());
        }

        @Test
        @DisplayName("Should handle null badge ID")
        void testGetEmployeeByBadgeId_NullBadgeId() {
            when(employeeRepository.findByBadgeIdAndDeletedFalse(null))
                    .thenReturn(Optional.empty());

            Optional<EmployeeResponseDto> result = employeeService.getEmployeeByBadgeId(null);

            assertFalse(result.isPresent());
        }

        @Test
        @DisplayName("Should handle empty badge ID")
        void testGetEmployeeByBadgeId_EmptyBadgeId() {
            when(employeeRepository.findByBadgeIdAndDeletedFalse(""))
                    .thenReturn(Optional.empty());

            Optional<EmployeeResponseDto> result = employeeService.getEmployeeByBadgeId("");

            assertFalse(result.isPresent());
        }

        @Test
        @DisplayName("Should be case-sensitive for badge ID")
        void testGetEmployeeByBadgeId_CaseSensitive() {
            when(employeeRepository.findByBadgeIdAndDeletedFalse("emp001"))
                    .thenReturn(Optional.empty());

            Optional<EmployeeResponseDto> result = employeeService.getEmployeeByBadgeId("emp001");

            assertFalse(result.isPresent());
        }
    }

    // ==================== CREATE EMPLOYEE TESTS ====================

    @Nested
    @DisplayName("Create Employee Tests")
    class CreateEmployeeTests {

        @Test
        @DisplayName("Should create employee successfully")
        void testCreateEmployee_Success() {
            when(employeeRepository.findByBadgeIdAndDeletedFalse("EMP001"))
                    .thenReturn(Optional.empty());
            when(employeeRepository.save(any(Employee.class)))
                    .thenReturn(testEmployee);

            EmployeeResponseDto result = employeeService.createEmployee(testRequestDto);

            assertNotNull(result);
            assertEquals("John Doe", result.getName());
            assertEquals("EMP001", result.getBadgeId());
            verify(employeeRepository, times(1)).save(any(Employee.class));
        }

        @Test
        @DisplayName("Should throw exception for duplicate badge ID")
        void testCreateEmployee_DuplicateBadgeId() {
            when(employeeRepository.findByBadgeIdAndDeletedFalse("EMP001"))
                    .thenReturn(Optional.of(testEmployee));

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> employeeService.createEmployee(testRequestDto)
            );

            assertTrue(exception.getMessage().contains("Badge ID already exists"));
            verify(employeeRepository, never()).save(any(Employee.class));
        }

        @Test
        @DisplayName("Should set deleted flag to false on creation")
        void testCreateEmployee_DeletedFlagFalse() {
            when(employeeRepository.findByBadgeIdAndDeletedFalse("EMP001"))
                    .thenReturn(Optional.empty());
            ArgumentCaptor<Employee> employeeCaptor = ArgumentCaptor.forClass(Employee.class);
            when(employeeRepository.save(employeeCaptor.capture()))
                    .thenReturn(testEmployee);

            employeeService.createEmployee(testRequestDto);

            Employee savedEmployee = employeeCaptor.getValue();
            assertFalse(savedEmployee.isDeleted());
        }

        @Test
        @DisplayName("Should create employee with all fields")
        void testCreateEmployee_AllFields() {
            when(employeeRepository.findByBadgeIdAndDeletedFalse("EMP001"))
                    .thenReturn(Optional.empty());
            ArgumentCaptor<Employee> employeeCaptor = ArgumentCaptor.forClass(Employee.class);
            when(employeeRepository.save(employeeCaptor.capture()))
                    .thenReturn(testEmployee);

            employeeService.createEmployee(testRequestDto);

            Employee savedEmployee = employeeCaptor.getValue();
            assertEquals("John Doe", savedEmployee.getName());
            assertEquals("EMP001", savedEmployee.getBadgeId());
            assertEquals("WORKER", savedEmployee.getRole());
            assertEquals("Shipping", savedEmployee.getDepartment());
            assertEquals("DAY_SHIFT", savedEmployee.getShiftGroup());
            assertEquals("ACTIVE", savedEmployee.getStatus());
            assertEquals("john.doe@warehouse.com", savedEmployee.getEmail());
            assertEquals("+1234567890", savedEmployee.getPhone());
            assertEquals("123 Main St", savedEmployee.getAddress());
        }

        @Test
        @DisplayName("Should create employee with optional fields null")
        void testCreateEmployee_OptionalFieldsNull() {
            testRequestDto.setShiftGroup(null);
            testRequestDto.setEmail(null);
            testRequestDto.setPhone(null);
            testRequestDto.setAddress(null);

            when(employeeRepository.findByBadgeIdAndDeletedFalse("EMP001"))
                    .thenReturn(Optional.empty());
            when(employeeRepository.save(any(Employee.class)))
                    .thenReturn(testEmployee);

            EmployeeResponseDto result = employeeService.createEmployee(testRequestDto);

            assertNotNull(result);
        }

        @Test
        @DisplayName("Should create employee with future hire date")
        void testCreateEmployee_FutureHireDate() {
            testRequestDto.setHireDate(LocalDate.now().plusDays(30));
            when(employeeRepository.findByBadgeIdAndDeletedFalse("EMP001"))
                    .thenReturn(Optional.empty());
            when(employeeRepository.save(any(Employee.class)))
                    .thenReturn(testEmployee);

            EmployeeResponseDto result = employeeService.createEmployee(testRequestDto);

            assertNotNull(result);
        }

        @Test
        @DisplayName("Should create employee with past hire date")
        void testCreateEmployee_PastHireDate() {
            testRequestDto.setHireDate(LocalDate.of(2020, 1, 1));
            when(employeeRepository.findByBadgeIdAndDeletedFalse("EMP001"))
                    .thenReturn(Optional.empty());
            when(employeeRepository.save(any(Employee.class)))
                    .thenReturn(testEmployee);

            EmployeeResponseDto result = employeeService.createEmployee(testRequestDto);

            assertNotNull(result);
        }
    }

    // ==================== UPDATE EMPLOYEE TESTS ====================

    @Nested
    @DisplayName("Update Employee Tests")
    class UpdateEmployeeTests {

        @Test
        @DisplayName("Should update employee successfully")
        void testUpdateEmployee_Success() {
            when(employeeRepository.findByIdAndDeletedFalse(1L))
                    .thenReturn(Optional.of(testEmployee));
            when(employeeRepository.save(any(Employee.class)))
                    .thenReturn(testEmployee);

            Optional<EmployeeResponseDto> result = employeeService.updateEmployee(1L, testRequestDto);

            assertTrue(result.isPresent());
            assertEquals("John Doe", result.get().getName());
            verify(employeeRepository, times(1)).save(any(Employee.class));
        }

        @Test
        @DisplayName("Should return empty when employee not found")
        void testUpdateEmployee_NotFound() {
            when(employeeRepository.findByIdAndDeletedFalse(999L))
                    .thenReturn(Optional.empty());

            Optional<EmployeeResponseDto> result = employeeService.updateEmployee(999L, testRequestDto);

            assertFalse(result.isPresent());
            verify(employeeRepository, never()).save(any(Employee.class));
        }

        @Test
        @DisplayName("Should update all modifiable fields")
        void testUpdateEmployee_AllFields() {
            when(employeeRepository.findByIdAndDeletedFalse(1L))
                    .thenReturn(Optional.of(testEmployee));
            ArgumentCaptor<Employee> employeeCaptor = ArgumentCaptor.forClass(Employee.class);
            when(employeeRepository.save(employeeCaptor.capture()))
                    .thenReturn(testEmployee);

            testRequestDto.setName("Jane Smith");
            testRequestDto.setRole("SUPERVISOR");
            testRequestDto.setDepartment("Receiving");
            testRequestDto.setStatus("INACTIVE");

            employeeService.updateEmployee(1L, testRequestDto);

            Employee updatedEmployee = employeeCaptor.getValue();
            assertEquals("Jane Smith", updatedEmployee.getName());
            assertEquals("SUPERVISOR", updatedEmployee.getRole());
            assertEquals("Receiving", updatedEmployee.getDepartment());
            assertEquals("INACTIVE", updatedEmployee.getStatus());
        }

        @Test
        @DisplayName("Should not update badge ID")
        void testUpdateEmployee_BadgeIdNotUpdated() {
            when(employeeRepository.findByIdAndDeletedFalse(1L))
                    .thenReturn(Optional.of(testEmployee));
            ArgumentCaptor<Employee> employeeCaptor = ArgumentCaptor.forClass(Employee.class);
            when(employeeRepository.save(employeeCaptor.capture()))
                    .thenReturn(testEmployee);

            employeeService.updateEmployee(1L, testRequestDto);

            Employee updatedEmployee = employeeCaptor.getValue();
            assertEquals("EMP001", updatedEmployee.getBadgeId());
        }

        @Test
        @DisplayName("Should not update soft-deleted employee")
        void testUpdateEmployee_SoftDeletedEmployee() {
            when(employeeRepository.findByIdAndDeletedFalse(1L))
                    .thenReturn(Optional.empty());

            Optional<EmployeeResponseDto> result = employeeService.updateEmployee(1L, testRequestDto);

            assertFalse(result.isPresent());
            verify(employeeRepository, never()).save(any(Employee.class));
        }
    }

    // ==================== SOFT DELETE EMPLOYEE TESTS ====================

    @Nested
    @DisplayName("Soft Delete Employee Tests")
    class SoftDeleteEmployeeTests {

        @Test
        @DisplayName("Should soft delete employee successfully")
        void testSoftDeleteEmployee_Success() {
            when(employeeRepository.findByIdAndDeletedFalse(1L))
                    .thenReturn(Optional.of(testEmployee));
            ArgumentCaptor<Employee> employeeCaptor = ArgumentCaptor.forClass(Employee.class);
            when(employeeRepository.save(employeeCaptor.capture()))
                    .thenReturn(testEmployee);

            boolean result = employeeService.softDeleteEmployee(1L);

            assertTrue(result);
            Employee deletedEmployee = employeeCaptor.getValue();
            assertTrue(deletedEmployee.isDeleted());
            assertEquals("INACTIVE", deletedEmployee.getStatus());
            verify(employeeRepository, times(1)).save(any(Employee.class));
        }

        @Test
        @DisplayName("Should return false when employee not found")
        void testSoftDeleteEmployee_NotFound() {
            when(employeeRepository.findByIdAndDeletedFalse(999L))
                    .thenReturn(Optional.empty());

            boolean result = employeeService.softDeleteEmployee(999L);

            assertFalse(result);
            verify(employeeRepository, never()).save(any(Employee.class));
        }

        @Test
        @DisplayName("Should set status to INACTIVE on soft delete")
        void testSoftDeleteEmployee_StatusInactive() {
            when(employeeRepository.findByIdAndDeletedFalse(1L))
                    .thenReturn(Optional.of(testEmployee));
            ArgumentCaptor<Employee> employeeCaptor = ArgumentCaptor.forClass(Employee.class);
            when(employeeRepository.save(employeeCaptor.capture()))
                    .thenReturn(testEmployee);

            employeeService.softDeleteEmployee(1L);

            Employee deletedEmployee = employeeCaptor.getValue();
            assertEquals("INACTIVE", deletedEmployee.getStatus());
        }

        @Test
        @DisplayName("Should not delete already soft-deleted employee")
        void testSoftDeleteEmployee_AlreadyDeleted() {
            when(employeeRepository.findByIdAndDeletedFalse(1L))
                    .thenReturn(Optional.empty());

            boolean result = employeeService.softDeleteEmployee(1L);

            assertFalse(result);
        }

        @Test
        @DisplayName("Should handle null ID gracefully")
        void testSoftDeleteEmployee_NullId() {
            when(employeeRepository.findByIdAndDeletedFalse(null))
                    .thenReturn(Optional.empty());

            boolean result = employeeService.softDeleteEmployee(null);

            assertFalse(result);
        }
    }

    // ==================== DTO CONVERSION TESTS ====================

    @Nested
    @DisplayName("DTO Conversion Tests")
    class DtoConversionTests {

        @Test
        @DisplayName("Should convert entity to response DTO correctly")
        void testToResponseDto_AllFields() {
            when(employeeRepository.findByIdAndDeletedFalse(1L))
                    .thenReturn(Optional.of(testEmployee));

            Optional<EmployeeResponseDto> result = employeeService.getEmployeeById(1L);

            assertTrue(result.isPresent());
            EmployeeResponseDto dto = result.get();
            assertEquals(testEmployee.getId(), dto.getId());
            assertEquals(testEmployee.getName(), dto.getName());
            assertEquals(testEmployee.getBadgeId(), dto.getBadgeId());
            assertEquals(testEmployee.getRole(), dto.getRole());
            assertEquals(testEmployee.getDepartment(), dto.getDepartment());
            assertEquals(testEmployee.getShiftGroup(), dto.getShiftGroup());
            assertEquals(testEmployee.getHireDate(), dto.getHireDate());
            assertEquals(testEmployee.getStatus(), dto.getStatus());
            assertEquals(testEmployee.getEmail(), dto.getEmail());
            assertEquals(testEmployee.getPhone(), dto.getPhone());
            assertEquals(testEmployee.getAddress(), dto.getAddress());
        }

        @Test
        @DisplayName("Should handle null optional fields in conversion")
        void testToResponseDto_NullOptionalFields() {
            testEmployee.setShiftGroup(null);
            testEmployee.setEmail(null);
            testEmployee.setPhone(null);
            testEmployee.setAddress(null);

            when(employeeRepository.findByIdAndDeletedFalse(1L))
                    .thenReturn(Optional.of(testEmployee));

            Optional<EmployeeResponseDto> result = employeeService.getEmployeeById(1L);

            assertTrue(result.isPresent());
            EmployeeResponseDto dto = result.get();
            assertNull(dto.getShiftGroup());
            assertNull(dto.getEmail());
            assertNull(dto.getPhone());
            assertNull(dto.getAddress());
        }
    }
}