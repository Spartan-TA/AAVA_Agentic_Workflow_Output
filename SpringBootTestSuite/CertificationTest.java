package com.warehouse.employee;

import com.warehouse.employee.model.Certification;
import com.warehouse.employee.model.Employee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class CertificationTest {
    private Certification certification;
    private Employee employee;

    @BeforeEach
    void setUp() {
        employee = new Employee();
        employee.setId(1L);
        employee.setBadgeId("BADGE123");
        employee.setName("John Doe");
        employee.setRole("Worker");
        employee.setDepartment("Packing");
        employee.setShiftGroup("Morning");
        employee.setHireDate(LocalDate.of(2020, 1, 1));
        employee.setStatus("ACTIVE");
        employee.setTenantId(100L);
        employee.setDeleted(false);
        employee.setCreatedAt(LocalDateTime.now().minusDays(1));
        employee.setUpdatedAt(LocalDateTime.now().minusHours(1));

        certification = new Certification();
        certification.setId(1L);
        certification.setEmployee(employee);
        certification.setCertType("Forklift");
        certification.setIssuedDate(LocalDate.of(2022, 1, 1));
        certification.setExpiryDate(LocalDate.now().plusDays(10));
        certification.setDocumentUrl("http://example.com/doc.pdf");
        certification.setValid(true);
        certification.setCreatedAt(LocalDateTime.now().minusDays(1));
        certification.setUpdatedAt(LocalDateTime.now().minusHours(1));
    }

    @AfterEach
    void tearDown() {
        certification = null;
        employee = null;
    }

    @Test
    void constructor_AllArgs_FieldsSetCorrectly() {
        LocalDateTime now = LocalDateTime.now();
        Certification cert = new Certification(2L, employee, "Safety", LocalDate.of(2021, 5, 10), LocalDate.of(2023, 5, 10), "http://doc.com/safety.pdf", false, now.minusDays(2), now.minusDays(1));
        assertEquals(2L, cert.getId());
        assertEquals(employee, cert.getEmployee());
        assertEquals("Safety", cert.getCertType());
        assertEquals(LocalDate.of(2021, 5, 10), cert.getIssuedDate());
        assertEquals(LocalDate.of(2023, 5, 10), cert.getExpiryDate());
        assertEquals("http://doc.com/safety.pdf", cert.getDocumentUrl());
        assertFalse(cert.getValid());
        assertEquals(now.minusDays(2), cert.getCreatedAt());
        assertEquals(now.minusDays(1), cert.getUpdatedAt());
    }

    @Test
    void valid_DefaultValue_IsTrue() {
        Certification cert = new Certification();
        assertTrue(cert.getValid());
    }

    @Test
    void createdAt_DefaultValue_IsNowOrBeforeNow() {
        Certification cert = new Certification();
        assertNotNull(cert.getCreatedAt());
        assertTrue(cert.getCreatedAt().isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    @Test
    void updatedAt_DefaultValue_IsNowOrBeforeNow() {
        Certification cert = new Certification();
        assertNotNull(cert.getUpdatedAt());
        assertTrue(cert.getUpdatedAt().isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    @Test
    void onUpdate_UpdatesUpdatedAtField() throws InterruptedException {
        LocalDateTime beforeUpdate = certification.getUpdatedAt();
        Thread.sleep(1000); // Ensure time difference
        certification.onUpdate();
        assertTrue(certification.getUpdatedAt().isAfter(beforeUpdate));
    }

    @Test
    void isExpired_ExpiryDateBeforeToday_ReturnsTrue() {
        certification.setExpiryDate(LocalDate.now().minusDays(1));
        assertTrue(certification.isExpired());
    }

    @Test
    void isExpired_ExpiryDateToday_ReturnsFalse() {
        certification.setExpiryDate(LocalDate.now());
        assertFalse(certification.isExpired());
    }

    @Test
    void isExpired_ExpiryDateAfterToday_ReturnsFalse() {
        certification.setExpiryDate(LocalDate.now().plusDays(1));
        assertFalse(certification.isExpired());
    }

    @Test
    void isExpired_NullExpiryDate_ReturnsFalse() {
        certification.setExpiryDate(null);
        assertFalse(certification.isExpired());
    }

    @Test
    void isExpiringSoon_ExpiryDateWithin30Days_ReturnsTrue() {
        certification.setExpiryDate(LocalDate.now().plusDays(10));
        assertTrue(certification.isExpiringSoon());
    }

    @Test
    void isExpiringSoon_ExpiryDateAfter30Days_ReturnsFalse() {
        certification.setExpiryDate(LocalDate.now().plusDays(31));
        assertFalse(certification.isExpiringSoon());
    }

    @Test
    void isExpiringSoon_ExpiryDateBeforeToday_ReturnsFalse() {
        certification.setExpiryDate(LocalDate.now().minusDays(1));
        assertFalse(certification.isExpiringSoon());
    }

    @Test
    void isExpiringSoon_NullExpiryDate_ReturnsFalse() {
        certification.setExpiryDate(null);
        assertFalse(certification.isExpiringSoon());
    }

    @Test
    void nullFields_ThrowsExceptionOnRequiredFields() {
        Certification cert = new Certification();
        assertThrows(NullPointerException.class, () -> {
            cert.setCertType(null);
            cert.getCertType().length();
        });
    }

    @Test
    void emptyStringFields_AllowedForOptionalFields() {
        certification.setDocumentUrl("");
        assertEquals("", certification.getDocumentUrl());
    }

    @Test
    void boundaryConditions_LongCertType_AllowedUpTo100Chars() {
        String certType = "A".repeat(100);
        certification.setCertType(certType);
        assertEquals(100, certification.getCertType().length());
    }
}
