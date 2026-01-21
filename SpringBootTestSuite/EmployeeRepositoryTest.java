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

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * JUnit tests for EmployeeRepository covering repository methods and edge cases.
 */
@ExtendWith(MockitoExtension.class)
public class EmployeeRepositoryTest {

    @Mock
    private EmployeeRepository employeeRepository;

    private Employee validEmployee;
    private Employee duplicateBadgeEmployee;

    @BeforeEach
    public void setUp() {
        validEmployee = new Employee();
        validEmployee.setId(1L);
        validEmployee.setName("Bob");
        validEmployee.setBadgeId("BADGE111");
        validEmployee.setRole("WORKER");
        validEmployee.setDepartment("Shipping");
        validEmployee.setShiftGroup("A");
        validEmployee.setHireDate(LocalDate.now());
        validEmployee.setStatus("ACTIVE");

        duplicateBadgeEmployee = new Employee();
        duplicateBadgeEmployee.setId(2L);
        duplicateBadgeEmployee.setBadgeId("BADGE111");
    }

    @Test
    public void testFindByBadgeId_ExistingBadge_ReturnsEmployee() {
        when(employeeRepository.findByBadgeId("BADGE111")).thenReturn(Optional.of(validEmployee));
        Optional<Employee> result = employeeRepository.findByBadgeId("BADGE111");
        assertTrue(result.isPresent());
        assertEquals("BADGE111", result.get().getBadgeId());
    }

    @Test
    public void testFindByBadgeId_NonExistentBadge_ReturnsEmpty() {
        when(employeeRepository.findByBadgeId("BADGE999")).thenReturn(Optional.empty());
        Optional<Employee> result = employeeRepository.findByBadgeId("BADGE999");
        assertFalse(result.isPresent());
    }

    @Test
    public void testSave_ValidEmployee_SavesSuccessfully() {
        when(employeeRepository.save(validEmployee)).thenReturn(validEmployee);
        Employee saved = employeeRepository.save(validEmployee);
        assertNotNull(saved);
        assertEquals("Bob", saved.getName());
    }

    @Test
    public void testSave_DuplicateBadgeId_ThrowsException() {
        when(employeeRepository.save(duplicateBadgeEmployee)).thenThrow(new RuntimeException("Duplicate badgeId"));
        Exception ex = assertThrows(RuntimeException.class, () -> employeeRepository.save(duplicateBadgeEmployee));
        assertEquals("Duplicate badgeId", ex.getMessage());
    }

    @Test
    public void testFindAll_WithPagination_ReturnsPagedResults() {
        Pageable pageable = PageRequest.of(0, 10);
        when(employeeRepository.findAll(pageable)).thenReturn(new PageImpl<>(Arrays.asList(validEmployee)));
        Page<Employee> page = employeeRepository.findAll(pageable);
        assertEquals(1, page.getTotalElements());
    }

    @Test
    public void testFindAll_EmptyList_ReturnsEmptyPage() {
        Pageable pageable = PageRequest.of(0, 10);
        when(employeeRepository.findAll(pageable)).thenReturn(new PageImpl<>(Collections.emptyList()));
        Page<Employee> page = employeeRepository.findAll(pageable);
        assertTrue(page.isEmpty());
    }

    @Test
    public void testFindAll_WithSorting_ReturnsSortedResults() {
        Pageable pageable = PageRequest.of(0, 10);
        Employee emp2 = new Employee();
        emp2.setId(3L);
        emp2.setName("Zack");
        when(employeeRepository.findAll(pageable)).thenReturn(new PageImpl<>(Arrays.asList(validEmployee, emp2)));
        Page<Employee> page = employeeRepository.findAll(pageable);
        assertEquals(2, page.getTotalElements());
        assertEquals("Bob", page.getContent().get(0).getName());
        assertEquals("Zack", page.getContent().get(1).getName());
    }

    @Test
    public void testFindByBadgeId_NullBadge_ReturnsEmpty() {
        when(employeeRepository.findByBadgeId(null)).thenReturn(Optional.empty());
        Optional<Employee> result = employeeRepository.findByBadgeId(null);
        assertFalse(result.isPresent());
    }

    @Test
    public void testSave_EmployeeWithNullFields_SavesSuccessfully() {
        Employee emp = new Employee();
        when(employeeRepository.save(emp)).thenReturn(emp);
        Employee saved = employeeRepository.save(emp);
        assertNotNull(saved);
    }

    @Test
    public void testFindAll_MultiplePages_ReturnsCorrectPage() {
        Pageable pageable = PageRequest.of(1, 1);
        Employee emp2 = new Employee();
        emp2.setId(2L);
        emp2.setName("Sam");
        when(employeeRepository.findAll(pageable)).thenReturn(new PageImpl<>(Arrays.asList(emp2)));
        Page<Employee> page = employeeRepository.findAll(pageable);
        assertEquals(1, page.getTotalElements());
        assertEquals("Sam", page.getContent().get(0).getName());
    }

    @Test
    public void testFindAll_WithDepartmentFilter_ReturnsFilteredResults() {
        Pageable pageable = PageRequest.of(0, 10);
        when(employeeRepository.findByDepartment("Shipping", pageable)).thenReturn(new PageImpl<>(Arrays.asList(validEmployee)));
        Page<Employee> page = employeeRepository.findByDepartment("Shipping", pageable);
        assertEquals(1, page.getTotalElements());
        assertEquals("Shipping", page.getContent().get(0).getDepartment());
    }

    @Test
    public void testFindAll_WithRoleFilter_ReturnsFilteredResults() {
        Pageable pageable = PageRequest.of(0, 10);
        when(employeeRepository.findByRole("WORKER", pageable)).thenReturn(new PageImpl<>(Arrays.asList(validEmployee)));
        Page<Employee> page = employeeRepository.findByRole("WORKER", pageable);
        assertEquals(1, page.getTotalElements());
        assertEquals("WORKER", page.getContent().get(0).getRole());
    }
}
