package SpringBootTestSuite;

import com.example.warehouse.model.Certification;
import org.junit.jupiter.api.*;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Certification entity logic.
 * Tests expiry and expiring soon logic.
 */
class CertificationTest {
    private Certification cert;

    @BeforeEach
    void setUp() {
        cert = new Certification();
        cert.setIssueDate(LocalDate.now().minusYears(1));
        cert.setExpiryDate(LocalDate.now().plusDays(10));
    }

    @Test
    void testIsExpiringWithin_ExpiringCert_ReturnsTrue() {
        assertTrue(cert.isExpiringWithin(15));
    }

    @Test
    void testIsExpiringWithin_NotExpiringCert_ReturnsFalse() {
        assertFalse(cert.isExpiringWithin(5));
    }

    @Test
    void testIsExpiringWithin_NullExpiryDate_ReturnsFalse() {
        cert.setExpiryDate(null);
        assertFalse(cert.isExpiringWithin(10));
    }

    @Test
    void testIsExpired_ExpiredCert_ReturnsTrue() {
        cert.setExpiryDate(LocalDate.now().minusDays(1));
        assertTrue(cert.isExpired());
    }

    @Test
    void testIsExpired_ValidCert_ReturnsFalse() {
        cert.setExpiryDate(LocalDate.now().plusDays(5));
        assertFalse(cert.isExpired());
    }

    @Test
    void testIsExpired_NullExpiryDate_ReturnsFalse() {
        cert.setExpiryDate(null);
        assertFalse(cert.isExpired());
    }
}
