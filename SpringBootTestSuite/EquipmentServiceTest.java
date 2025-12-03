public class EquipmentServiceTest {

    @Test
    public void testValidEquipmentAllocation() {
        // Arrange
        EquipmentService service = new EquipmentService();
        Equipment equipment = new Equipment("Laptop", "John Doe");

        // Act
        boolean result = service.allocateEquipment(equipment);

        // Assert
        assertTrue(result);
    }

    @Test
    public void testNullEquipmentAllocation() {
        // Arrange
        EquipmentService service = new EquipmentService();

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> service.allocateEquipment(null));
    }

    @Test
    public void testDuplicateEquipmentAllocation() {
        // Arrange
        EquipmentService service = new EquipmentService();
        Equipment equipment = new Equipment("Monitor", "Jane Doe");
        service.allocateEquipment(equipment);

        // Act & Assert
        assertThrows(DuplicateEquipmentException.class, () -> service.allocateEquipment(equipment));
    }
}