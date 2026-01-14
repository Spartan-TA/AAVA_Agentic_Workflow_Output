package com.example.warehouse.test;

import com.example.warehouse.certification.Certification;
import com.example.warehouse.certification.CertificationRepository;
import com.example.warehouse.certification.CertificationService;
import com.example.warehouse.certification.CertificationController;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.http.ResponseEntity;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CertificationServiceTest {
    @Mock
    private CertificationRepository certificationRepository;
    @InjectMocks
    private CertificationService certificationService;
    private CertificationController certificationController;
    private Certification testCert;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        certificationController = new CertificationController(certificationService);
        testCert = new Certification(1L, 1L, "Forklift", LocalDate.now().plusDays(30), "ACTIVE");
    }

    @AfterEach
    void tearDown() {
        // Cleanup if needed
    }

    @Test
    void testCreateCertification_ValidInput_Success() {
        when(certificationRepository.save(any(Certification.class))).thenReturn(testCert);
        Certification created = certificationService.createCertification(testCert);
        assertNotNull(created);
        assertEquals("Forklift", created.getName());
    }

    @Test
    void testCreateCertification_Duplicate_ThrowsException() {
        when(certificationRepository.findByEmployeeIdAndName(1L, "Forklift")).thenReturn(Optional.of(testCert));
        assertThrows(IllegalArgumentException.class, () -> certificationService.createCertification(testCert));
    }

    @Test
    void testGetCertificationById_ValidId_ReturnsCertification() {
        when(certificationRepository.findById(1L)).thenReturn(Optional.of(testCert));
        Certification found = certificationService.getCertificationById(1L);
        assertNotNull(found);
        assertEquals(1L, found.getId());
    }

    @Test
    void testGetCertificationById_InvalidId_ThrowsException() {
        when(certificationRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> certificationService.getCertificationById(2L));
    }

    @Test
    void testAlertBeforeExpiry_Within30Days_True() {
        assertTrue(certificationService.isExpiringSoon(testCert));
    }

    @Test
    void testAlertBeforeExpiry_Expired_False() {
        Certification expired = new Certification(2L, 1L, "Forklift", LocalDate.now().minusDays(1), "EXPIRED");
        assertFalse(certificationService.isExpiringSoon(expired));
    }

    @Test
    void testController_CreateCertification_Success() {
        when(certificationService.createCertification(any(Certification.class))).thenReturn(testCert);
        ResponseEntity<Certification> response = certificationController.createCertification(testCert);
        assertEquals(201, response.getStatusCodeValue());
        assertEquals("Forklift", response.getBody().getName());
    }

    @Test
    void testController_CreateCertification_Duplicate() {
        when(certificationService.createCertification(any(Certification.class))).thenThrow(new IllegalArgumentException("Duplicate"));
        assertThrows(IllegalArgumentException.class, () -> certificationController.createCertification(testCert));
    }
}
