public class LocalizationServiceTest {

    @Test
    public void testValidLocalizationRetrieval() {
        // Arrange
        LocalizationService service = new LocalizationService();
        String key = "welcome.message";
        String locale = "en_US";

        // Act
        String result = service.getLocalizedMessage(key, locale);

        // Assert
        assertNotNull(result);
        assertEquals("Welcome", result);
    }

    @Test
    public void testNullKeyLocalizationRetrieval() {
        // Arrange
        LocalizationService service = new LocalizationService();
        String locale = "en_US";

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> service.getLocalizedMessage(null, locale));
    }

    @Test
    public void testUnsupportedLocaleLocalizationRetrieval() {
        // Arrange
        LocalizationService service = new LocalizationService();
        String key = "welcome.message";
        String locale = "xx_XX";

        // Act & Assert
        assertThrows(UnsupportedLocaleException.class, () -> service.getLocalizedMessage(key, locale));
    }
}