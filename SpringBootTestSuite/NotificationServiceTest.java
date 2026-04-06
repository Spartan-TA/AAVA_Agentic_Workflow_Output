package SpringBootTestSuite;

import com.example.app.entity.NotificationPreference;
import com.example.app.entity.User;
import com.example.app.exception.UserNotFoundException;
import com.example.app.repository.NotificationPreferenceRepository;
import com.example.app.repository.UserRepository;
import com.example.app.service.NotificationService;
import com.example.app.util.EmailService;
import org.junit.jupiter.api.*;
import org.mockito.*;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {
    @Mock
    private NotificationPreferenceRepository notificationPreferenceRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private NotificationService notificationService;

    private User validUser;
    private NotificationPreference preference;

    @BeforeEach
    void setUp() {
        validUser = new User();
        validUser.setId(1L);
        validUser.setEmail("test@example.com");
        preference = new NotificationPreference();
        preference.setUser(validUser);
        preference.setEmailNotifications(true);
    }

    @Test
    void testUpdateNotificationPreference() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(validUser));
        when(notificationPreferenceRepository.save(any(NotificationPreference.class))).thenReturn(preference);
        NotificationPreference result = notificationService.updatePreference(1L, true);
        assertTrue(result.isEmailNotifications());
        verify(notificationPreferenceRepository).save(any(NotificationPreference.class));
    }

    @Test
    void testUpdateNotificationPreferenceWithInvalidUser() {
        when(userRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> notificationService.updatePreference(2L, true));
    }

    @Test
    void testSendNotificationEmail() {
        doNothing().when(emailService).sendEmail(anyString(), anyString(), anyString());
        notificationService.sendNotificationEmail("test@example.com", "Subject", "Body");
        verify(emailService).sendEmail("test@example.com", "Subject", "Body");
    }

    @Test
    void testSendNotificationEmailWithNullEmail() {
        assertThrows(NullPointerException.class, () -> notificationService.sendNotificationEmail(null, "Subject", "Body"));
    }

    @Test
    void testSendNotificationEmailWithEmptySubject() {
        doNothing().when(emailService).sendEmail(anyString(), anyString(), anyString());
        notificationService.sendNotificationEmail("test@example.com", "", "Body");
        verify(emailService).sendEmail("test@example.com", "", "Body");
    }

    @AfterEach
    void tearDown() {
        validUser = null;
        preference = null;
    }
}
