public class LeaveManagementServiceTest {

    @Test
    public void testValidLeaveApplication() {
        // Arrange
        LeaveManagementService service = new LeaveManagementService();
        Employee employee = new Employee("John Doe", "Engineering");
        LeaveApplication leave = new LeaveApplication(employee, LocalDate.now(), LocalDate.now().plusDays(5));

        // Act
        boolean result = service.applyLeave(leave);

        // Assert
        assertTrue(result);
    }

    @Test
    public void testNullLeaveApplication() {
        // Arrange
        LeaveManagementService service = new LeaveManagementService();

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> service.applyLeave(null));
    }

    @Test
    public void testOverlappingLeaveApplication() {
        // Arrange
        LeaveManagementService service = new LeaveManagementService();
        Employee employee = new Employee("Jane Doe", "HR");
        LeaveApplication leave1 = new LeaveApplication(employee, LocalDate.now(), LocalDate.now().plusDays(5));
        LeaveApplication leave2 = new LeaveApplication(employee, LocalDate.now().plusDays(3), LocalDate.now().plusDays(7));
        service.applyLeave(leave1);

        // Act & Assert
        assertThrows(LeaveOverlapException.class, () -> service.applyLeave(leave2));
    }
}