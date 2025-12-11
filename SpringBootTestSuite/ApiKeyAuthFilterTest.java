package SpringBootTestSuite;

import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import java.io.IOException;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class ApiKeyAuthFilterTest {

    @Mock
    private FilterChain filterChain;

    private ApiKeyAuthFilter apiKeyAuthFilter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        apiKeyAuthFilter = new ApiKeyAuthFilter("valid-api-key");
    }

    @AfterEach
    void tearDown() {
        // Teardown logic if needed
    }

    @Test
    void testValidApiKeyInHeader_AuthenticatesSuccessfully() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-API-Key", "valid-api-key");
        MockHttpServletResponse response = new MockHttpServletResponse();

        apiKeyAuthFilter.doFilter(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        assertEquals(200, response.getStatus());
    }

    @Test
    void testInvalidApiKey_Returns401Unauthorized() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-API-Key", "invalid-api-key");
        MockHttpServletResponse response = new MockHttpServletResponse();

        apiKeyAuthFilter.doFilter(request, response, filterChain);

        verify(filterChain, never()).doFilter(request, response);
        assertEquals(401, response.getStatus());
    }

    @Test
    void testMissingApiKeyHeader_Returns401Unauthorized() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        apiKeyAuthFilter.doFilter(request, response, filterChain);

        verify(filterChain, never()).doFilter(request, response);
        assertEquals(401, response.getStatus());
    }

    @Test
    void testAuthenticationTokenCreatedWithValidKey() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-API-Key", "valid-api-key");
        MockHttpServletResponse response = new MockHttpServletResponse();

        apiKeyAuthFilter.doFilter(request, response, filterChain);

        // Assuming filter sets an attribute or context for authenticated user
        assertNotNull(request.getAttribute("authenticated"));
    }

    @Test
    void testFilterChainContinuesAfterSuccessfulAuthentication() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-API-Key", "valid-api-key");
        MockHttpServletResponse response = new MockHttpServletResponse();

        apiKeyAuthFilter.doFilter(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void testFilterChainBlockedOnAuthenticationFailure() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-API-Key", "invalid-api-key");
        MockHttpServletResponse response = new MockHttpServletResponse();

        apiKeyAuthFilter.doFilter(request, response, filterChain);

        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void testEmptyHeaderValue_Returns401Unauthorized() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-API-Key", "");
        MockHttpServletResponse response = new MockHttpServletResponse();

        apiKeyAuthFilter.doFilter(request, response, filterChain);

        verify(filterChain, never()).doFilter(request, response);
        assertEquals(401, response.getStatus());
    }

    @Test
    void testMalformedHeader_Returns401Unauthorized() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-API-Key", "malformed-key-!@#");
        MockHttpServletResponse response = new MockHttpServletResponse();

        apiKeyAuthFilter.doFilter(request, response, filterChain);

        verify(filterChain, never()).doFilter(request, response);
        assertEquals(401, response.getStatus());
    }
}