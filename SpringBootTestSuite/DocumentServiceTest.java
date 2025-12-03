public class DocumentServiceTest {

    @Test
    public void testValidDocumentUpload() {
        // Arrange
        DocumentService service = new DocumentService();
        Document document = new Document("Employee Handbook", "PDF", new byte[1024]);

        // Act
        boolean result = service.uploadDocument(document);

        // Assert
        assertTrue(result);
    }

    @Test
    public void testNullDocumentUpload() {
        // Arrange
        DocumentService service = new DocumentService();

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> service.uploadDocument(null));
    }

    @Test
    public void testUnsupportedFileTypeUpload() {
        // Arrange
        DocumentService service = new DocumentService();
        Document document = new Document("Employee Handbook", "TXT", new byte[1024]);

        // Act & Assert
        assertThrows(UnsupportedFileTypeException.class, () -> service.uploadDocument(document));
    }
}