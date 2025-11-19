public class SecurityUtilTest {

    @Autowired
    private SecurityUtil securityUtil;

    @BeforeEach
    public void setUp() {
        // Initialize mocks and test data
    }

    @Test
    public void testGenerateJwtToken() {
        // Arrange
        String username = "testuser";
        List<String> roles = Arrays.asList("ADMIN", "WORKER");

        // Act
        String token = securityUtil.generateJwtToken(username, roles);

        // Assert
        assertNotNull(token);
        assertTrue(token.startsWith("eyJ"));
    }

    @Test
    public void testValidateJwtTokenValid() {
        // Arrange
        String username = "testuser";
        List<String> roles = Arrays.asList("ADMIN", "WORKER");
        String token = securityUtil.generateJwtToken(username, roles);

        // Act
        boolean isValid = securityUtil.validateJwtToken(token);

        // Assert
        assertTrue(isValid);
    }

    @Test
    public void testValidateJwtTokenInvalid() {
        // Arrange
        String invalidToken = "invalid.token";

        // Act
        boolean isValid = securityUtil.validateJwtToken(invalidToken);

        // Assert
        assertFalse(isValid);
    }

    @AfterEach
    public void tearDown() {
        // Clean up resources
    }
}