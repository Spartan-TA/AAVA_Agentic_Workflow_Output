package com.wms.employee.repository;

import com.wms.employee.entity.Certification;
import com.wms.employee.entity.Employee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class CertificationRepositoryTest {

    @Autowired
    private CertificationRepository certificationRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    private Employee employee;
    private Certification certification;

    @BeforeEach
    void setUp() {
        employee = new Employee(null, "B123", "John Doe", "Worker", "Logistics", "A", null, "ACTIVE", false);
        employeeRepository.save(employee);

        certification = new Certification(null, employee, "Forklift", LocalDate.now().minusYears(1), LocalDate.now().plusMonths(1), "doc.pdf");
        certificationRepository.save(certification);
    }

    @Test
    void testFindByExpiryDateBefore_ReturnsExpiringCertifications() {
        LocalDate futureDate = LocalDate.now().plusMonths(2);
        List<Certification> result = certificationRepository.findByExpiryDateBefore(futureDate);
        assertEquals(1, result.size());
    }

    @Test
    void testFindByEmployeeAndType_ReturnsEmployeeCertifications() {
        List<Certification> result = certificationRepository.findByEmployeeAndType(employee, "Forklift");
        assertEquals(1, result.size());
        assertEquals("Forklift", result.get(0).getType());
    }

    @Test
    void testFindByEmployeeAndType_NoMatch_ReturnsEmpty() {
        List<Certification> result = certificationRepository.findByEmployeeAndType(employee, "Crane");
        assertEquals(0, result.size());
    }
}