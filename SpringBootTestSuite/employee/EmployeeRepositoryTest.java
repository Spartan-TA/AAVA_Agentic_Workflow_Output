package com.companyname.wems.employee.repository;

import com.companyname.wems.employee.model.Employee;
import com.companyname.wems.employee.model.EmployeeStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class EmployeeRepositoryTest {
    
    @Autowired
    private EmployeeRepository employeeRepository;
    
    private Employee testEmployee;
    
    @BeforeEach
    void setUp() {
        testEmployee = Employee.builder()
            .badgeId("EMP001")
            .firstName("John")
            .lastName("Doe")
            .email("john.doe@example.com")
            .department("Warehouse")
            .status(EmployeeStatus.ACTIVE)
            .deleted(false)
            .build();
        employeeRepository.save(testEmployee);
    }
    
    @Test
    void testFindByBadgeId_ValidBadgeId_Success() {
        Optional<Employee> result = employeeRepository.findByBadgeId("EMP001");
        assertTrue(result.isPresent());
        assertEquals("John", result.get().getFirstName());
    }
    
    @Test
    void testFindByStatus_ValidStatus_Success() {
        List<Employee> result = employeeRepository.findByStatus(EmployeeStatus.ACTIVE);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }
    
    @Test
    void testFindByDepartment_ValidDepartment_Success() {
        List<Employee> result = employeeRepository.findByDepartment("Warehouse");
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }
    
    @Test
    void testExistsByBadgeId_ExistingBadgeId_ReturnsTrue() {
        boolean exists = employeeRepository.existsByBadgeId("EMP001");
        assertTrue(exists);
    }
    
    @Test
    void testExistsByBadgeId_NonExistingBadgeId_ReturnsFalse() {
        boolean exists = employeeRepository.existsByBadgeId("EMP999");
        assertFalse(exists);
    }
}
