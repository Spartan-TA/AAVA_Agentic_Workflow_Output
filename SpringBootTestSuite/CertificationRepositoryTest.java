package com.warehouse.employee;

import com.warehouse.employee.model.Certification;
import com.warehouse.employee.model.Employee;
import com.warehouse.employee.repository.CertificationRepository;
import org.junit.jupiter.api.AfterEach;
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

    private Employee employee1;
    private Employee employee2;
    private Certification cert1;
    private Certification cert2;
    private Certification cert3;

    @BeforeEach
    void setUp() {
        employee1 = new Employee();
        employee1.setBadgeId("BADGE001");
        employee1.setName("Alice");
        employee1.setRole("Worker");
        employee1.setDepartment("Packing");
        employee1.setShiftGroup("Morning");
        employee1.setHireDate(LocalDate.of(2020, 1, 1));
        employee1.setStatus("ACTIVE");
        employee1.setDeleted(false);

        employee2 = new Employee();
        employee2.setBadgeId("BADGE002");
        employee2.setName("Bob");
        employee2.setRole("Supervisor");
        employee2.setDepartment("Shipping");
        employee2.setShiftGroup("Night");
        employee2.setHireDate(LocalDate.of(2021, 5, 10));
        employee2.setStatus("ACTIVE");
        employee2.setDeleted(false);

        cert1 = new Certification();
        cert1.setEmployee(employee1);
        cert1.setCertType("Forklift");
        cert1.setIssuedDate(LocalDate.of(2022, 1, 1));
        cert1.setExpiryDate(LocalDate.now().plusDays(10));
        cert1.setValid(true);

        cert2 = new Certification();
        cert2.setEmployee(employee1);
        cert2.setCertType("Safety");
        cert2.setIssuedDate(LocalDate.of(2021, 5, 10));
        cert2.setExpiryDate(LocalDate.now().minusDays(1));
        cert2.setValid(true);

        cert3 = new Certification();
        cert3.setEmployee(employee2);
        cert3.setCertType("Forklift");
        cert3.setIssuedDate(LocalDate.of(2023, 1, 1));
        cert3.setExpiryDate(LocalDate.now().plusDays(40));
        cert3.setValid(false);

        certificationRepository.save(cert1);
        certificationRepository.save(cert2);
        certificationRepository.save(cert3);
    }

    @AfterEach
    void tearDown() {
        certificationRepository.deleteAll();
    }

    @Test
    void findByEmployeeId_ValidEmployeeId_ReturnsCertifications() {
        List<Certification> certs = certificationRepository.findByEmployeeId(employee1.getId());
        assertEquals(2, certs.size());
    }

    @Test
    void findByEmployeeId_NonExistentEmployeeId_ReturnsEmptyList() {
        List<Certification> certs = certificationRepository.findByEmployeeId(999L);
        assertTrue(certs.isEmpty());
    }

    @Test
    void findByEmployeeIdAndValidTrue_ValidEmployeeId_ReturnsValidCertifications() {
        List<Certification> certs = certificationRepository.findByEmployeeIdAndValidTrue(employee1.getId());
        assertEquals(2, certs.size());
    }

    @Test
    void findByCertType_ValidCertType_ReturnsCertifications() {
        List<Certification> certs = certificationRepository.findByCertType("Forklift");
        assertEquals(2, certs.size());
    }

    @Test
    void findByCertType_NonExistentCertType_ReturnsEmptyList() {
        List<Certification> certs = certificationRepository.findByCertType("NonExistentType");
        assertTrue(certs.isEmpty());
    }

    @Test
    void findExpiringSoon_ValidDateRange_ReturnsCertifications() {
        LocalDate start = LocalDate.now();
        LocalDate end = LocalDate.now().plusDays(30);
        List<Certification> certs = certificationRepository.findExpiringSoon(start, end);
        assertEquals(1, certs.size());
        assertEquals("Forklift", certs.get(0).getCertType());
    }

    @Test
    void findExpiringSoon_InvalidDateRange_ReturnsEmptyList() {
        LocalDate start = LocalDate.now().plusDays(100);
        LocalDate end = LocalDate.now().plusDays(200);
        List<Certification> certs = certificationRepository.findExpiringSoon(start, end);
        assertTrue(certs.isEmpty());
    }

    @Test
    void findExpired_CurrentDate_ReturnsExpiredCertifications() {
        List<Certification> certs = certificationRepository.findExpired(LocalDate.now());
        assertEquals(1, certs.size());
        assertEquals("Safety", certs.get(0).getCertType());
    }

    @Test
    void findExpired_FutureDate_ReturnsAllExpiredCertifications() {
        List<Certification> certs = certificationRepository.findExpired(LocalDate.now().plusDays(1));
        assertEquals(1, certs.size());
    }

    @Test
    void findByEmployeeId_NullEmployeeId_ThrowsException() {
        assertThrows(Exception.class, () -> certificationRepository.findByEmployeeId(null));
    }

    @Test
    void findByCertType_EmptyString_ReturnsEmptyList() {
        List<Certification> certs = certificationRepository.findByCertType("");
        assertTrue(certs.isEmpty());
    }
}
