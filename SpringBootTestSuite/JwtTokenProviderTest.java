package SpringBootTestSuite;

import com.example.demo.security.JwtTokenProvider;
import com.example.demo.entity.User;
import com.example.demo.exception.TokenExpiredException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import static org.junit.jupiter.api.Assertions.*;

public class JwtTokenProviderTest {
    @InjectMocks
    private JwtTokenProvider jwtTokenProvider;

    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = new User();
        user.setId(1L);
        user.setUsername("jwtuser");
        user.setEmail("jwt@example.com");
    }

    @Test
    void testGenerateToken_HappyPath() {
        String token = jwtTokenProvider.generateToken(user);
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void testGenerateToken_NullUser() {
        assertThrows(IllegalArgumentException.class, () -> jwtTokenProvider.generateToken(null));
    }

    @Test
    void testValidateToken_HappyPath() {
        String token = jwtTokenProvider.generateToken(user);
        assertTrue(jwtTokenProvider.validateToken(token));
    }

    @Test
    void testValidateToken_InvalidToken() {
        assertFalse(jwtTokenProvider.validateToken("invalid.token"));
    }

    @Test
    void testValidateToken_ExpiredToken() {
        String expiredToken = "expired.token";
        assertThrows(TokenExpiredException.class, () -> jwtTokenProvider.validateToken(expiredToken));
    }

    @Test
    void testGetUsernameFromToken_HappyPath() {
        String token = jwtTokenProvider.generateToken(user);
        String username = jwtTokenProvider.getUsernameFromToken(token);
        assertEquals("jwtuser", username);
    }

    @Test
    void testGetUsernameFromToken_InvalidToken() {
        assertThrows(IllegalArgumentException.class, () -> jwtTokenProvider.getUsernameFromToken("invalid.token"));
    }
}