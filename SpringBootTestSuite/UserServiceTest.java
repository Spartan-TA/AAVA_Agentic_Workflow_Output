import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.crypto.password.PasswordEncoder;

public class UserServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private UserService userService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testRegisterUser_Success() {
        User user = new User("test", "test@email.com", "pass");
        when(userRepository.findByEmail("test@email.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("pass")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenReturn(user);
        User result = userService.registerUser(user);
        assertEquals(user, result);
        verify(userRepository).save(any(User.class));
    }

    @Test
    public void testRegisterUser_EmailExists() {
        User user = new User("test", "test@email.com", "pass");
        when(userRepository.findByEmail("test@email.com")).thenReturn(Optional.of(user));
        assertThrows(IllegalArgumentException.class, () -> userService.registerUser(user));
    }

    @Test
    public void testFindUserById_Found() {
        User user = new User("test", "test@email.com", "pass");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        User result = userService.findUserById(1L);
        assertEquals(user, result);
    }

    @Test
    public void testFindUserById_NotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> userService.findUserById(1L));
    }

    @Test
    public void testUpdateUser_Success() {
        User user = new User("test", "test@email.com", "pass");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        User updated = new User("new", "test@email.com", "pass");
        User result = userService.updateUser(1L, updated);
        assertEquals("new", result.getUsername());
    }

    @Test
    public void testUpdateUser_NotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        User updated = new User("new", "test@email.com", "pass");
        assertThrows(NoSuchElementException.class, () -> userService.updateUser(1L, updated));
    }

    @Test
    public void testDeleteUser_Success() {
        User user = new User("test", "test@email.com", "pass");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        doNothing().when(userRepository).deleteById(1L);
        assertDoesNotThrow(() -> userService.deleteUser(1L));
        verify(userRepository).deleteById(1L);
    }

    @Test
    public void testDeleteUser_NotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> userService.deleteUser(1L));
    }

    @Test
    public void testListAllUsers() {
        List<User> users = Arrays.asList(new User("a", "a@email.com", "p"), new User("b", "b@email.com", "p"));
        when(userRepository.findAll()).thenReturn(users);
        List<User> result = userService.listAllUsers();
        assertEquals(2, result.size());
    }

    @Test
    public void testChangePassword_Success() {
        User user = new User("test", "test@email.com", "old");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("old", "old")).thenReturn(true);
        when(passwordEncoder.encode("new")).thenReturn("hashed");
        userService.changePassword(1L, "old", "new");
        verify(userRepository).save(any(User.class));
    }

    @Test
    public void testChangePassword_WrongOldPassword() {
        User user = new User("test", "test@email.com", "old");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "old")).thenReturn(false);
        assertThrows(SecurityException.class, () -> userService.changePassword(1L, "wrong", "new"));
    }

    @Test
    public void testChangePassword_UserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> userService.changePassword(1L, "old", "new"));
    }
}
