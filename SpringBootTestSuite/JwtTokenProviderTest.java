package SpringBootTestSuite;

import com.example.app.entity.User;
import com.example.app.exception.InvalidTokenException;
import com.example.app.exception.TokenExpiredException;
import com.example.app.security.JwtTokenProvider;
import org.junit.jupiter.api.*;
import org.mockito.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtTokenProviderTest {
    @InjectMocks
    private JwtTokenProvider jwtTokenProvider;

    private User validUser;

    @BeforeEach
    void setUp() {
        validUser = new User();
        validUser.setId(1L);
        validUser.setEmail("test@example.com");
    }

    @Test
    void testGenerateToken() {
        String token = jwtTokenProvider.generateToken(validUser);
        assertNotNull(token);
        assertTrue(token.length() > 10);
    }

    @Test
    void testValidateTokenValid() {
        String token = jwtTokenProvider.generateToken(validUser);
        assertTrue(jwtTokenProvider.validateToken(token));
    }

    @Test
    void testValidateTokenInvalid() {
        assertThrows(InvalidTokenException.class, () -> jwtTokenProvider.validateToken("invalidToken"));
    }

    @Test
    void testValidateTokenExpired() {
        assertThrows(TokenExpiredException.class, () -> jwtTokenProvider.validateToken("expiredToken"));
    }

    @Test
    void testGetUserIdFromToken() {
        String token = jwtTokenProvider.generateToken(validUser);
        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        assertEquals(validUser.getId(), userId);
    }

    @Test
    void testGetUserIdFromInvalidToken() {
        assertThrows(InvalidTokenException.class, () -> jwtTokenProvider.getUserIdFromToken("invalidToken"));
    }

    @AfterEach
    void tearDown() {
        validUser = null;
    }
}
