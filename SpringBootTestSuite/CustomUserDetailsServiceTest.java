package SpringBootTestSuite;

import com.example.demo.security.CustomUserDetailsService;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UserDetails;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class CustomUserDetailsServiceTest {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = new User();
        user.setId(1L);
        user.setUsername("securityuser");
        user.setEmail("security@example.com");
        user.setPassword("password");
    }

    @Test
    void testLoadUserByUsername_HappyPath() {
        when(userRepository.findByUsername("securityuser")).thenReturn(java.util.Optional.of(user));
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("securityuser");
        assertEquals("securityuser", userDetails.getUsername());
    }

    @Test
    void testLoadUserByUsername_NotFound() {
        when(userRepository.findByUsername("unknown")).thenReturn(java.util.Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> customUserDetailsService.loadUserByUsername("unknown"));
    }

    @Test
    void testLoadUserByUsername_NullInput() {
        assertThrows(IllegalArgumentException.class, () -> customUserDetailsService.loadUserByUsername(null));
    }

    @Test
    void testLoadUserByUsername_EmptyString() {
        assertThrows(IllegalArgumentException.class, () -> customUserDetailsService.loadUserByUsername(""));
    }

    @Test
    void testLoadUserByUsername_InvalidFormat() {
        assertThrows(IllegalArgumentException.class, () -> customUserDetailsService.loadUserByUsername("!@#"));
    }
}