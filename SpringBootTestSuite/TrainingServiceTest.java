package SpringBootTestSuite;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.*;

public class TrainingServiceTest {
    @Mock
    private TrainingRepository trainingRepository;
    @Mock
    private EmployeeRepository employeeRepository;

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
    void testAddCertification_Valid_Success() {
        Certification cert = new Certification(1L, 1L, "Forklift", new Date(), new Date(System.currentTimeMillis()+31536000000L), "ACTIVE");
        when(trainingRepository.save(any(Certification.class))).thenReturn(cert);
        Certification result = trainingService.addCertification(cert);
        assertNotNull(result);
        assertEquals("Forklift", result.getType());
    }

    @Test
    void testAddCertification_Duplicate_ThrowsException() {
        Certification cert = new Certification(1L, 1L, "Forklift", new Date(), new Date(System.currentTimeMillis()+31536000000L), "ACTIVE");
        when(trainingRepository.existsByEmployeeIdAndType(1L, "Forklift")).thenReturn(true);
        assertThrows(DuplicateCertificationException.class, () -> trainingService.addCertification(cert));
    }

    @Test
    void testGetCertificationsByEmployee_Valid_Success() {
        List<Certification> certs = Arrays.asList(
            new Certification(1L, 1L, "Forklift", new Date(), new Date(System.currentTimeMillis()+31536000000L), "ACTIVE"),
            new Certification(2L, 1L, "Pallet Jack", new Date(), new Date(System.currentTimeMillis()+31536000000L), "ACTIVE")
        );
        when(trainingRepository.findByEmployeeId(1L)).thenReturn(certs);
        List<Certification> result = trainingService.getCertificationsByEmployee(1L);
        assertEquals(2, result.size());
    }

    @Test
    void testAlertExpiringCertifications_Valid_Success() {
        Certification cert = new Certification(1L, 1L, "Forklift", new Date(), new Date(System.currentTimeMillis()+604800000L), "ACTIVE"); // expires in 7 days
        when(trainingRepository.findExpiringWithinDays(30)).thenReturn(Arrays.asList(cert));
        List<Certification> result = trainingService.alertExpiringCertifications(30);
        assertEquals(1, result.size());
    }

    @Test
    void testBlockAssignmentToExpiredCert_ThrowsException() {
        Certification cert = new Certification(1L, 1L, "Forklift", new Date(), new Date(System.currentTimeMillis()-86400000L), "EXPIRED");
        when(trainingRepository.findByEmployeeIdAndType(1L, "Forklift")).thenReturn(Optional.of(cert));
        assertThrows(CertificationExpiredException.class, () -> trainingService.assignToEquipment(1L, "Forklift"));
    }

    @Test
    void testAddCertification_NullType_ThrowsException() {
        Certification cert = new Certification(1L, 1L, null, new Date(), new Date(System.currentTimeMillis()+31536000000L), "ACTIVE");
        assertThrows(InvalidCertificationException.class, () -> trainingService.addCertification(cert));
    }

    // Integration scenario: Certification status visible on employee profile
    @Test
    void testCertificationStatusOnEmployeeProfile_Success() {
        Certification cert = new Certification(1L, 1L, "Forklift", new Date(), new Date(System.currentTimeMillis()+31536000000L), "ACTIVE");
        when(trainingRepository.findByEmployeeId(1L)).thenReturn(Arrays.asList(cert));
        EmployeeProfile profile = trainingService.getEmployeeProfileWithCertifications(1L);
        assertNotNull(profile);
        assertTrue(profile.getCertifications().stream().anyMatch(c -> "Forklift".equals(c.getType())));
    }
}
