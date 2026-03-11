package SpringBootTestSuite;

import com.warehouse.security.JwtTokenProvider;
import io.jsonwebtoken.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.Authentication;

import java.lang.reflect.Field;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtTokenProviderTest {
    JwtTokenProvider jwtTokenProvider;
    String secret = "testsecret123456789012345678901234567890";
    long expiration = 1000 * 60 * 60; // 1 hour

    @BeforeEach
    void setUp() throws Exception {
        jwtTokenProvider = new JwtTokenProvider();
        Field secretField = JwtTokenProvider.class.getDeclaredField("jwtSecret");
        secretField.setAccessible(true);
        secretField.set(jwtTokenProvider, secret);
        Field expField = JwtTokenProvider.class.getDeclaredField("jwtExpirationMs");
        expField.setAccessible(true);
        expField.set(jwtTokenProvider, expiration);
    }

    @Test
    @DisplayName("generateToken returns valid JWT and getUsernameFromJWT parses it")
    void generateAndParseToken() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("user1");
        String token = jwtTokenProvider.generateToken(auth);
        assertNotNull(token);
        String username = jwtTokenProvider.getUsernameFromJWT(token);
        assertEquals("user1", username);
    }

    @Test
    @DisplayName("validateToken returns true for valid token")
    void validateToken_valid() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("user2");
        String token = jwtTokenProvider.generateToken(auth);
        assertTrue(jwtTokenProvider.validateToken(token));
    }

    @Test
    @DisplayName("validateToken returns false for malformed token")
    void validateToken_malformed() {
        assertFalse(jwtTokenProvider.validateToken("not.a.jwt"));
    }

    @Test
    @DisplayName("validateToken returns false for expired token")
    void validateToken_expired() throws Exception {
        // Create an expired token manually
        String expiredToken = Jwts.builder()
                .setSubject("user3")
                .setIssuedAt(new Date(System.currentTimeMillis() - 10000))
                .setExpiration(new Date(System.currentTimeMillis() - 5000))
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
        assertFalse(jwtTokenProvider.validateToken(expiredToken));
    }

    @Test
    @DisplayName("validateToken returns false for token with wrong signature")
    void validateToken_wrongSignature() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("user4");
        String token = jwtTokenProvider.generateToken(auth);
        // Tamper with the token
        String tampered = token.substring(0, token.length() - 2) + "ab";
        assertFalse(jwtTokenProvider.validateToken(tampered));
    }

    @Test
    @DisplayName("getUsernameFromJWT throws for invalid token")
    void getUsernameFromJWT_invalid() {
        assertThrows(Exception.class, () -> jwtTokenProvider.getUsernameFromJWT("bad.token"));
    }
}
