public class DateTimeUtilTest {

    @Autowired
    private DateTimeUtil dateTimeUtil;

    @BeforeEach
    public void setUp() {
        // Initialize mocks and test data
    }

    @Test
    public void testConvertToTimezone() {
        // Arrange
        LocalDateTime dateTime = LocalDateTime.of(2023, 10, 1, 12, 0);
        ZoneId targetZone = ZoneId.of("America/New_York");

        // Act
        ZonedDateTime result = dateTimeUtil.convertToTimezone(dateTime, targetZone);

        // Assert
        assertNotNull(result);
        assertEquals(targetZone, result.getZone());
    }

    @Test
    public void testConvertToTimezoneNullInput() {
        // Arrange
        ZoneId targetZone = ZoneId.of("America/New_York");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> dateTimeUtil.convertToTimezone(null, targetZone));
    }

    @Test
    public void testConvertToTimezoneInvalidZone() {
        // Arrange
        LocalDateTime dateTime = LocalDateTime.of(2023, 10, 1, 12, 0);
        ZoneId invalidZone = ZoneId.of("Invalid/Zone");

        // Act & Assert
        assertThrows(DateTimeException.class, () -> dateTimeUtil.convertToTimezone(dateTime, invalidZone));
    }

    @AfterEach
    public void tearDown() {
        // Clean up resources
    }
}