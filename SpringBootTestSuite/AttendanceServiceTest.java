public class AttendanceServiceTest {

    @Test
    public void testValidAttendanceMarking() {
        // Arrange
        AttendanceService service = new AttendanceService();
        Employee employee = new Employee("John Doe", "Engineering");
        LocalDate date = LocalDate.now();

        // Act
        boolean result = service.markAttendance(employee, date);

        // Assert
        assertTrue(result);
    }

    @Test
    public void testNullEmployeeAttendanceMarking() {
        // Arrange
        AttendanceService service = new AttendanceService();
        LocalDate date = LocalDate.now();

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> service.markAttendance(null, date));
    }

    @Test
    public void testFutureDateAttendanceMarking() {
        // Arrange
        AttendanceService service = new AttendanceService();
        Employee employee = new Employee("Jane Doe", "HR");
        LocalDate futureDate = LocalDate.now().plusDays(1);

        // Act & Assert
        assertThrows(InvalidDateException.class, () -> service.markAttendance(employee, futureDate));
    }
}