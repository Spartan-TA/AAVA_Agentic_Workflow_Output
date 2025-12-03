public class TempStaffServiceTest {

    @Test
    public void testValidTempStaffAssignment() {
        // Arrange
        TempStaffService service = new TempStaffService();
        TempStaff tempStaff = new TempStaff("John Doe", "Warehouse", LocalDate.now(), LocalDate.now().plusDays(30));

        // Act
        boolean result = service.assignTempStaff(tempStaff);

        // Assert
        assertTrue(result);
    }

    @Test
    public void testNullTempStaffAssignment() {
        // Arrange
        TempStaffService service = new TempStaffService();

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> service.assignTempStaff(null));
    }

    @Test
    public void testOverlappingTempStaffAssignment() {
        // Arrange
        TempStaffService service = new TempStaffService();
        TempStaff tempStaff1 = new TempStaff("Jane Doe", "Warehouse", LocalDate.now(), LocalDate.now().plusDays(15));
        TempStaff tempStaff2 = new TempStaff("Jane Doe", "Warehouse", LocalDate.now().plusDays(10), LocalDate.now().plusDays(20));
        service.assignTempStaff(tempStaff1);

        // Act & Assert
        assertThrows(TempStaffOverlapException.class, () -> service.assignTempStaff(tempStaff2));
    }
}