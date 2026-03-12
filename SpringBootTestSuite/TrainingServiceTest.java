package SpringBootTestSuite;

import com.example.warehouse.training.Certification;
import com.example.warehouse.training.EmployeeCertification;
import com.example.warehouse.training.TrainingService;
import com.example.warehouse.training.TrainingRepository;
import com.example.warehouse.exception.ResourceNotFoundException;
import com.example.warehouse.exception.ValidationException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
public class TrainingServiceTest {
    @Mock
    private TrainingRepository trainingRepository;

    @InjectMocks
    private TrainingService trainingService;

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    public void createCertification_ValidInput_ReturnsCertification() {
        Certification cert = new Certification();
        cert.setName("Forklift");
        when(trainingRepository.saveCertification(any())).thenReturn(cert);
        Certification result = trainingService.createCertification(cert);
        assertNotNull(result);
        assertEquals("Forklift", result.getName());
    }

    @Test
    public void createCertification_NullInput_ThrowsValidationException() {
        assertThrows(ValidationException.class, () -> trainingService.createCertification(null));
    }

    @Test
    public void assignCertification_ValidInput_ReturnsEmployeeCertification() {
        EmployeeCertification empCert = new EmployeeCertification();
        empCert.setEmployeeId(1L);
        empCert.setCertificationId(1L);
        empCert.setExpiryDate(LocalDate.now().plusYears(1));
        when(trainingRepository.saveEmployeeCertification(any())).thenReturn(empCert);
        EmployeeCertification result = trainingService.assignCertification(1L, 1L, LocalDate.now().plusYears(1));
        assertNotNull(result);
        assertEquals(1L, result.getEmployeeId());
    }

    @Test
    public void assignCertification_NullEmployeeId_ThrowsValidationException() {
        assertThrows(ValidationException.class, () -> trainingService.assignCertification(null, 1L, LocalDate.now().plusYears(1)));
    }

    @Test
    public void assignCertification_NullCertificationId_ThrowsValidationException() {
        assertThrows(ValidationException.class, () -> trainingService.assignCertification(1L, null, LocalDate.now().plusYears(1)));
    }

    @Test
    public void assignCertification_NullExpiryDate_ThrowsValidationException() {
        assertThrows(ValidationException.class, () -> trainingService.assignCertification(1L, 1L, null));
    }

    @Test
    public void getCertifications_ReturnsList() {
        Certification cert = new Certification();
        cert.setId(1L);
        when(trainingRepository.findAllCertifications()).thenReturn(Collections.singletonList(cert));
        List<Certification> result = trainingService.getCertifications();
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    public void getCertifications_Empty_ReturnsEmptyList() {
        when(trainingRepository.findAllCertifications()).thenReturn(Collections.emptyList());
        List<Certification> result = trainingService.getCertifications();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
