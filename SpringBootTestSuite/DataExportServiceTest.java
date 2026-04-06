package SpringBootTestSuite;

import com.example.app.entity.User;
import com.example.app.repository.UserRepository;
import com.example.app.service.DataExportService;
import org.junit.jupiter.api.*;
import org.mockito.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataExportServiceTest {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DataExportService dataExportService;

    private List<User> users;

    @BeforeEach
    void setUp() {
        User u1 = new User();
        u1.setId(1L);
        u1.setEmail("user1@example.com");
        User u2 = new User();
        u2.setId(2L);
        u2.setEmail("user2@example.com");
        users = Arrays.asList(u1, u2);
    }

    @Test
    void testExportUsersToCsvWithValidData() {
        when(userRepository.findAll()).thenReturn(users);
        String csv = dataExportService.exportUsersToCsv();
        assertTrue(csv.contains("user1@example.com"));
        assertTrue(csv.contains("user2@example.com"));
        assertTrue(csv.startsWith("id,email"));
    }

    @Test
    void testExportUsersToCsvWithEmptyData() {
        when(userRepository.findAll()).thenReturn(Collections.emptyList());
        String csv = dataExportService.exportUsersToCsv();
        assertTrue(csv.startsWith("id,email"));
        assertFalse(csv.contains("@example.com"));
    }

    @Test
    void testExportUsersToCsvWithNullData() {
        when(userRepository.findAll()).thenReturn(null);
        assertThrows(NullPointerException.class, () -> dataExportService.exportUsersToCsv());
    }

    @AfterEach
    void tearDown() {
        users = null;
    }
}
