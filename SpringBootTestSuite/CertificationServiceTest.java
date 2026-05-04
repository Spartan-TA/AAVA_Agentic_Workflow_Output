package com.warehouse.management.certification;

import com.warehouse.management.certification.CertificationService;
import com.warehouse.management.certification.Certification;
import com.warehouse.management.employee.Employee;
import org.junit.jupiter.api.*;
import org.mockito.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CertificationServiceTest {

    @Mock
    private CertificationRepository certificationRepository;

    @InjectMocks
    private CertificationService certificationService;

    private Employee employee;
    private Certification certification;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        employee = new Employee(1L, "John Doe", "BADGE123", "WORKER", "Logistics", "A", new Date(), "ACTIVE");
        certification = new Certification(1L, employee, "Forklift", new Date(), new Date(), "ACTIVE");
    }

    @Test
    void testCreateCertification_Valid() {
        when(certificationRepository.save(any(Certification.class))).thenReturn(certification);
        Certification result = certificationService.create(employee, "Forklift", new Date(), new Date());
        assertNotNull(result);
        assertEquals("Forklift", result.getType());
    }

    @Test
    void testCheckExpiry_Expired() {
        certification.setExpiryDate(new Date(System.currentTimeMillis() - 86400000));
        boolean expired = certificationService.checkExpiry(certification);
        assertTrue(expired);
    }

    @Test
    void testCheckExpiry_NotExpired() {
        certification.setExpiryDate(new Date(System.currentTimeMillis() + 86400000));
        boolean expired = certificationService.checkExpiry(certification);
        assertFalse(expired);
    }

    @Test
    void testBlockAssignment_ExpiredCert() {
        certification.setExpiryDate(new Date(System.currentTimeMillis() - 86400000));
        assertThrows(IllegalStateException.class, () -> certificationService.blockAssignment(employee, "Forklift"));
    }
}