package SpringBootTestSuite;

import com.example.app.entity.User;
import com.example.app.entity.UserRole;
import com.example.app.exception.UserNotFoundException;
import com.example.app.exception.InvalidCredentialsException;
import com.example.app.repository.UserRepository;
import com.example.app.service.AdminUserService;
import org.junit.jupiter.api.*;
import org.mockito.*;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceTest {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AdminUserService adminUserService;

    private User adminUser;
    private User normalUser;

    @BeforeEach
    void setUp() {
        adminUser = new User();
        adminUser.setId(1L);
        adminUser.setEmail("admin@example.com");
        adminUser.setRole(UserRole.ADMIN);

        normalUser = new User();
        normalUser.setId(2L);
        normalUser.setEmail("user@example.com");
        normalUser.setRole(UserRole.USER);
    }

    @Test
    void testAdminCanLockUserAccount() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(normalUser));
        when(userRepository.save(any(User.class))).thenReturn(normalUser);
        adminUserService.lockUserAccount(2L);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testAdminCanUnlockUserAccount() {
        normalUser.setAccountLocked(true);
        when(userRepository.findById(2L)).thenReturn(Optional.of(normalUser));
        when(userRepository.save(any(User.class))).thenReturn(normalUser);
        adminUserService.unlockUserAccount(2L);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testLockUserAccountWithInvalidUserId() {
        when(userRepository.findById(3L)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> adminUserService.lockUserAccount(3L));
    }

    @Test
    void testUnlockUserAccountWithInvalidUserId() {
        when(userRepository.findById(3L)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> adminUserService.unlockUserAccount(3L));
    }

    @Test
    void testLockUserAccountWithNullUserId() {
        assertThrows(NullPointerException.class, () -> adminUserService.lockUserAccount(null));
    }

    @Test
    void testUnlockUserAccountWithNullUserId() {
        assertThrows(NullPointerException.class, () -> adminUserService.unlockUserAccount(null));
    }

    @Test
    void testAdminCanChangeUserRole() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(normalUser));
        when(userRepository.save(any(User.class))).thenReturn(normalUser);
        adminUserService.changeUserRole(2L, UserRole.ADMIN);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testChangeUserRoleWithInvalidRole() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(normalUser));
        assertThrows(InvalidCredentialsException.class, () -> adminUserService.changeUserRole(2L, null));
    }

    @AfterEach
    void tearDown() {
        adminUser = null;
        normalUser = null;
    }
}
