public class ShiftSchedulingServiceTest {

    @Test
    public void testValidShiftCreation() {
        // Arrange
        ShiftSchedulingService service = new ShiftSchedulingService();
        Shift shift = new Shift("Morning", "08:00", "16:00");

        // Act
        boolean result = service.createShift(shift);

        // Assert
        assertTrue(result);
    }

    @Test
    public void testNullShiftCreation() {
        // Arrange
        ShiftSchedulingService service = new ShiftSchedulingService();

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> service.createShift(null));
    }

    @Test
    public void testOverlappingShiftCreation() {
        // Arrange
        ShiftSchedulingService service = new ShiftSchedulingService();
        Shift shift1 = new Shift("Morning", "08:00", "16:00");
        Shift shift2 = new Shift("Afternoon", "12:00", "20:00");
        service.createShift(shift1);

        // Act & Assert
        assertThrows(ShiftOverlapException.class, () -> service.createShift(shift2));
    }
}