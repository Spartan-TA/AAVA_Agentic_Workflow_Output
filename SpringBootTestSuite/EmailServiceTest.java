package SpringBootTestSuite;

import com.example.customermanagement.service.EmailService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for EmailService covering verification and general email sending.
 */
@SpringBootTest
public class EmailServiceTest {

    @InjectMocks
    private EmailService emailService;

    private AutoCloseable closeable;

    @BeforeEach
    public void setup() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    public void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    public void testSendVerificationEmail_WithValidInput_ShouldNotThrow() {
        // Act & Assert
        assertDoesNotThrow(() -> emailService.sendVerificationEmail("test@example.com", "token123"));
    }

    @Test
    public void testSendVerificationEmail_WithNullEmail_ShouldThrowException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> emailService.sendVerificationEmail(null, "token123"));
    }

    @Test
    public void testSendVerificationEmail_WithEmptyToken_ShouldThrowException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> emailService.sendVerificationEmail("test@example.com", ""));
    }

    @Test
    public void testSendEmail_WithValidInput_ShouldNotThrow() {
        // Act & Assert
        assertDoesNotThrow(() -> emailService.sendEmail("test@example.com", "Subject", "Body"));
    }

    @Test
    public void testSendEmail_WithNullRecipient_ShouldThrowException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> emailService.sendEmail(null, "Subject", "Body"));
    }

    @Test
    public void testSendEmail_WithEmptySubject_ShouldNotThrow() {
        // Act & Assert
        assertDoesNotThrow(() -> emailService.sendEmail("test@example.com", "", "Body"));
    }

    @Test
    public void testSendEmail_WithEmptyBody_ShouldNotThrow() {
        // Act & Assert
        assertDoesNotThrow(() -> emailService.sendEmail("test@example.com", "Subject", ""));
    }
}
