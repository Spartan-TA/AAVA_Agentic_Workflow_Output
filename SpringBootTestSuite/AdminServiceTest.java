import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class AdminServiceTest {
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private AdminService adminService;

    private User user;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        user = new User("admin", "admin@email.com", "pass");
        user.setId(1L);
    }

    @Test
    public void testGetAllUsers() {
        List<User> users = Arrays.asList(user, new User("u2", "u2@email.com", "p2"));
        when(userRepository.findAll()).thenReturn(users);
        List<User> result = adminService.getAllUsers();
        assertEquals(2, result.size());
    }

    @Test
    public void testDeleteUser_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        doNothing().when(userRepository).deleteById(1L);
        assertDoesNotThrow(() -> adminService.deleteUser(1L));
        verify(userRepository).deleteById(1L);
    }

    @Test
    public void testDeleteUser_NotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> adminService.deleteUser(1L));
    }

    @Test
    public void testUpdateUserRole_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        User result = adminService.updateUserRole(1L, "MODERATOR");
        assertEquals("MODERATOR", result.getRole());
    }

    @Test
    public void testUpdateUserRole_NotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> adminService.updateUserRole(1L, "MODERATOR"));
    }

    @Test
    public void testUpdateUserRole_NullRole() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        assertThrows(IllegalArgumentException.class, () -> adminService.updateUserRole(1L, null));
    }
}
