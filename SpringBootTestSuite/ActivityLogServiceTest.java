package SpringBootTestSuite;

import com.example.app.entity.ActivityLog;
import com.example.app.entity.User;
import com.example.app.exception.UserNotFoundException;
import com.example.app.repository.ActivityLogRepository;
import com.example.app.repository.UserRepository;
import com.example.app.service.ActivityLogService;
import org.junit.jupiter.api.*;
import org.mockito.*;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActivityLogServiceTest {
    @Mock
    private ActivityLogRepository activityLogRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ActivityLogService activityLogService;

    private User validUser;
    private List<ActivityLog> logs;

    @BeforeEach
    void setUp() {
        validUser = new User();
        validUser.setId(1L);
        validUser.setEmail("test@example.com");
        logs = Arrays.asList(
                new ActivityLog(1L, validUser, "LOGIN", LocalDateTime.now().minusDays(1)),
                new ActivityLog(2L, validUser, "LOGOUT", LocalDateTime.now().minusDays(2))
        );
    }

    @Test
    void testGetActivityLogsForLast30Days() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(validUser));
        when(activityLogRepository.findByUserAndTimestampAfter(eq(validUser), any(LocalDateTime.class))).thenReturn(logs);
        List<ActivityLog> result = activityLogService.getActivityLogsForLast30Days(1L);
        assertEquals(2, result.size());
        verify(activityLogRepository).findByUserAndTimestampAfter(eq(validUser), any(LocalDateTime.class));
    }

    @Test
    void testGetActivityLogsForLast30Days_EmptyResult() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(validUser));
        when(activityLogRepository.findByUserAndTimestampAfter(eq(validUser), any(LocalDateTime.class))).thenReturn(Collections.emptyList());
        List<ActivityLog> result = activityLogService.getActivityLogsForLast30Days(1L);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetActivityLogsForInvalidUser() {
        when(userRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> activityLogService.getActivityLogsForLast30Days(2L));
    }

    @Test
    void testGetActivityLogsWithNullUserId() {
        assertThrows(NullPointerException.class, () -> activityLogService.getActivityLogsForLast30Days(null));
    }

    @AfterEach
    void tearDown() {
        validUser = null;
        logs = null;
    }
}
