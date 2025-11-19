public class ValidationUtilTest {

    @Autowired
    private ValidationUtil validationUtil;

    @BeforeEach
    public void setUp() {
        // Initialize mocks and test data
    }

    @Test
    public void testValidateEmailValid() {
        // Arrange
        String email = "test@example.com";

        // Act
        boolean isValid = validationUtil.validateEmail(email);

        // Assert
        assertTrue(isValid);
    }

    @Test
    public void testValidateEmailInvalid() {
        // Arrange
        String email = "invalid-email";

        // Act
        boolean isValid = validationUtil.validateEmail(email);

        // Assert
        assertFalse(isValid);
    }

    @Test
    public void testValidatePhoneNumberValid() {
        // Arrange
        String phoneNumber = "1234567890";

        // Act
        boolean isValid = validationUtil.validatePhoneNumber(phoneNumber);

        // Assert
        assertTrue(isValid);
    }

    @Test
    public void testValidatePhoneNumberInvalid() {
        // Arrange
        String phoneNumber = "12345";

        // Act
        boolean isValid = validationUtil.validatePhoneNumber(phoneNumber);

        // Assert
        assertFalse(isValid);
    }

    @AfterEach
    public void tearDown() {
        // Clean up resources
    }
}