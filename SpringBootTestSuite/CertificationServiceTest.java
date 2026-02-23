package com.example.service;

import com.example.entity.Certification;
import com.example.repository.CertificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.Optional;

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
    void getCertification_found_returnsCertification() {
        Certification cert = new Certification();
        cert.setId(1L);
        when(certificationRepository.findById(1L)).thenReturn(Optional.of(cert));

        Certification result = certificationService.getCertification(1L);

        assertEquals(1L, result.getId());
    }

    @Test
    void getCertification_notFound_throwsException() {
        when(certificationRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> certificationService.getCertification(1L));
    }
}