import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.*;

public class CertificationServiceTest {
    @Mock
    private CertificationRepository certificationRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @InjectMocks
    private CertificationService certificationService;
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
    void testAddCertification_ValidInput() {
        Employee employee = new Employee("John Doe", "B123", "WORKER", "Shipping", "A", new Date(), "ACTIVE");
        Certification cert = new Certification(employee, "Forklift", new Date(), new Date(System.currentTimeMillis() + 2592000000L));
        when(employeeRepository.findByBadgeId("B123")).thenReturn(Optional.of(employee));
        when(certificationRepository.save(any(Certification.class))).thenReturn(cert);
        Certification result = certificationService.addCertification("B123", "Forklift", new Date(), new Date(System.currentTimeMillis() + 2592000000L));
        assertEquals("Forklift", result.getType());
    }

    @Test
    void testAddCertification_NullInput() {
        assertThrows(IllegalArgumentException.class, () -> certificationService.addCertification(null, "Forklift", new Date(), new Date()));
    }

    @Test
    void testAddCertification_InvalidBadgeId() {
        when(employeeRepository.findByBadgeId("BADGE999")).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> certificationService.addCertification("BADGE999", "Forklift", new Date(), new Date()));
    }

    @Test
    void testAddCertification_ExpiredDate() {
        Employee employee = new Employee("John Doe", "B123", "WORKER", "Shipping", "A", new Date(), "ACTIVE");
        when(employeeRepository.findByBadgeId("B123")).thenReturn(Optional.of(employee));
        Date expired = new Date(System.currentTimeMillis() - 86400000);
        assertThrows(ValidationException.class, () -> certificationService.addCertification("B123", "Forklift", new Date(), expired));
    }

    @Test
    void testCheckCertificationQualification_Valid() {
        Employee employee = new Employee("John Doe", "B123", "WORKER", "Shipping", "A", new Date(), "ACTIVE");
        Certification cert = new Certification(employee, "Forklift", new Date(), new Date(System.currentTimeMillis() + 2592000000L));
        when(certificationRepository.findByEmployeeBadgeIdAndType("B123", "Forklift")).thenReturn(Optional.of(cert));
        assertTrue(certificationService.checkCertificationQualification("B123", "Forklift"));
    }

    @Test
    void testCheckCertificationQualification_Expired() {
        Employee employee = new Employee("John Doe", "B123", "WORKER", "Shipping", "A", new Date(), "ACTIVE");
        Certification cert = new Certification(employee, "Forklift", new Date(), new Date(System.currentTimeMillis() - 86400000));
        when(certificationRepository.findByEmployeeBadgeIdAndType("B123", "Forklift")).thenReturn(Optional.of(cert));
        assertFalse(certificationService.checkCertificationQualification("B123", "Forklift"));
    }

    @Test
    void testAlertCertificationExpiration_Valid() {
        Employee employee = new Employee("John Doe", "B123", "WORKER", "Shipping", "A", new Date(), "ACTIVE");
        Certification cert = new Certification(employee, "Forklift", new Date(), new Date(System.currentTimeMillis() + 604800000L)); // 7 days
        when(certificationRepository.findExpiringCertificationsWithinDays(7)).thenReturn(Arrays.asList(cert));
        List<Certification> result = certificationService.alertCertificationExpiration(7);
        assertEquals(1, result.size());
    }

    @Test
    void testAddCertification_BoundaryValues() {
        Employee employee = new Employee("A", "B126", "WORKER", "Shipping", "A", new Date(), "ACTIVE");
        when(employeeRepository.findByBadgeId("B126")).thenReturn(Optional.of(employee));
        Certification cert = new Certification(employee, "A", new Date(), new Date(System.currentTimeMillis() + 2592000000L));
        when(certificationRepository.save(any(Certification.class))).thenReturn(cert);
        assertDoesNotThrow(() -> certificationService.addCertification("B126", "A", new Date(), new Date(System.currentTimeMillis() + 2592000000L)));
    }

    @Test
    void testAddCertification_EmptyType() {
        Employee employee = new Employee("John Doe", "B123", "WORKER", "Shipping", "A", new Date(), "ACTIVE");
        when(employeeRepository.findByBadgeId("B123")).thenReturn(Optional.of(employee));
        assertThrows(ValidationException.class, () -> certificationService.addCertification("B123", "", new Date(), new Date(System.currentTimeMillis() + 2592000000L)));
    }
}
