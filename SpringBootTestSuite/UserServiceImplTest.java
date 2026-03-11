package SpringBootTestSuite;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.UserServiceImpl;
import com.example.demo.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.Optional;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class UserServiceImplTest {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
    }

    @Test
    void testFindUserById_HappyPath() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        User found = userService.findUserById(1L);
        assertEquals("testuser", found.getUsername());
    }

    @Test
    void testFindUserById_NotFound() {
        when(userRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> userService.findUserById(2L));
    }

    @Test
    void testCreateUser_HappyPath() {
        when(userRepository.save(any(User.class))).thenReturn(user);
        User created = userService.createUser(user);
        assertEquals("testuser", created.getUsername());
    }

    @Test
    void testCreateUser_NullInput() {
        assertThrows(IllegalArgumentException.class, () -> userService.createUser(null));
    }

    @Test
    void testUpdateUser_HappyPath() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        User updated = userService.updateUser(1L, user);
        assertEquals("testuser", updated.getUsername());
    }

    @Test
    void testUpdateUser_NotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> userService.updateUser(1L, user));
    }

    @Test
    void testDeleteUser_HappyPath() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        doNothing().when(userRepository).delete(user);
        assertDoesNotThrow(() -> userService.deleteUser(1L));
    }

    @Test
    void testDeleteUser_NotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> userService.deleteUser(1L));
    }

    @Test
    void testFindUserByUsername_EmptyString() {
        assertThrows(IllegalArgumentException.class, () -> userService.findUserByUsername(""));
    }

    @Test
    void testFindUserByUsername_NullInput() {
        assertThrows(IllegalArgumentException.class, () -> userService.findUserByUsername(null));
    }
}