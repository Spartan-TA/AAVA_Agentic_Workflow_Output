package com.warehouse.training.service;

import com.warehouse.training.entity.Certification;
import com.warehouse.training.repository.CertificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CertificationServiceTest {
    @Mock
    private CertificationRepository certificationRepository;

    @InjectMocks
    private CertificationService certificationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateCertification() {
        Certification cert = Certification.builder()
                .type("Forklift")
                .issueDate(LocalDate.now())
                .expiryDate(LocalDate.now().plusYears(1))
                .status(Certification.Status.ACTIVE)
                .employeeId(1L)
                .build();
        when(certificationRepository.save(any(Certification.class))).thenReturn(cert);
        Certification result = certificationService.createCertification(cert);
        assertEquals(Certification.Status.ACTIVE, result.getStatus());
        assertEquals("Forklift", result.getType());
    }
}
