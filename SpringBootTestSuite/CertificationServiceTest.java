import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CertificationServiceTest {
    @Mock
    private CertificationRepository repository;
    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private CertificationService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Should add certification")
    void testAddCertification_NormalCase() {
        Employee employee = new Employee(1L, "B123", "John Doe", "WORKER", "Shipping", "A", LocalDate.now(), EmployeeStatus.ACTIVE, false);
        Certification cert = new Certification(null, employee, "Forklift", LocalDate.now(), LocalDate.now().plusYears(1), "url");

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(repository.save(any(Certification.class))).thenReturn(cert);

        Certification result = service.addCertification(1L, "Forklift", LocalDate.now(), LocalDate.now().plusYears(1), "url");

        assertNotNull(result);
        assertEquals("Forklift", result.getType());
    }

    @Test
    @DisplayName("Should check expiry")
    void testCheckExpiry_NormalCase() {
        Certification cert = new Certification(1L, null, "Forklift", LocalDate.now(), LocalDate.now().minusDays(1), "url");

        when(repository.findById(1L)).thenReturn(Optional.of(cert));

        boolean expired = service.checkExpiry(1L);

        assertTrue(expired);
    }

    @Test
    @DisplayName("Should validate for assignment")
    void testValidateForAssignment_NormalCase() {
        Certification cert = new Certification(1L, null, "Forklift", LocalDate.now(), LocalDate.now().plusDays(30), "url");

        when(repository.findById(1L)).thenReturn(Optional.of(cert));

        boolean valid = service.validateForAssignment(1L);

        assertTrue(valid);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException for unknown certification")
    void testCheckExpiry_ResourceNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.checkExpiry(99L));
    }

    @Test
    @DisplayName("Should handle null input for addCertification")
    void testAddCertification_NullInput() {
        assertThrows(ValidationException.class, () -> service.addCertification(null, null, null, null, null));
    }

    @Test
    @DisplayName("Should handle expired certifications in validateForAssignment")
    void testValidateForAssignment_Expired() {
        Certification cert = new Certification(1L, null, "Forklift", LocalDate.now(), LocalDate.now().minusDays(1), "url");

        when(repository.findById(1L)).thenReturn(Optional.of(cert));

        boolean valid = service.validateForAssignment(1L);

        assertFalse(valid);
    }
}