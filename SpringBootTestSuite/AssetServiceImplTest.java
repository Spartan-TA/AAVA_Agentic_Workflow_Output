package SpringBootTestSuite;

import org.junit.jupiter.api.*;
import org.mockito.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;

class AssetServiceImplTest {

    @Mock
    private AssetRepository assetRepository;
    @Mock
    private CertificationRepository certificationRepository;
    @InjectMocks
    private AssetServiceImpl assetService;

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    @DisplayName("createAsset - valid input - asset created")
    void testCreateAsset_ValidInput_AssetCreated() {
        Asset asset = new Asset(null, "Forklift", "Available");
        when(assetRepository.save(any())).thenAnswer(i -> {
            Asset a = i.getArgument(0);
            a.setId(1L);
            return a;
        });
        Asset result = assetService.createAsset(asset);
        assertNotNull(result.getId());
        assertEquals("Forklift", result.getName());
    }

    @Test
    @DisplayName("checkOutAsset - valid - asset checked out")
    void testCheckOutAsset_Valid_AssetCheckedOut() {
        Asset asset = new Asset(1L, "Scanner", "Available");
        when(assetRepository.findById(1L)).thenReturn(Optional.of(asset));
        assetService.checkOutAsset(1L, 1L);
        assertEquals("CheckedOut", asset.getStatus());
    }

    @Test
    @DisplayName("checkOutAsset - asset not found - throws exception")
    void testCheckOutAsset_AssetNotFound_ThrowsException() {
        when(assetRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(AssetNotFoundException.class, () -> assetService.checkOutAsset(2L, 1L));
    }

    @Test
    @DisplayName("checkInAsset - valid - asset checked in")
    void testCheckInAsset_Valid_AssetCheckedIn() {
        Asset asset = new Asset(1L, "Scanner", "CheckedOut");
        when(assetRepository.findById(1L)).thenReturn(Optional.of(asset));
        assetService.checkInAsset(1L);
        assertEquals("Available", asset.getStatus());
    }

    @Test
    @DisplayName("checkInAsset - asset not found - throws exception")
    void testCheckInAsset_AssetNotFound_ThrowsException() {
        when(assetRepository.findById(3L)).thenReturn(Optional.empty());
        assertThrows(AssetNotFoundException.class, () -> assetService.checkInAsset(3L));
    }

    @Test
    @DisplayName("getAssetHistory - returns history")
    void testGetAssetHistory_ReturnsHistory() {
        List<AssetHistory> history = Arrays.asList(new AssetHistory(1L, 1L, "CheckedOut", "2024-06-01"));
        when(assetRepository.getHistory(1L)).thenReturn(history);
        List<AssetHistory> result = assetService.getAssetHistory(1L);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("validateCertificationForAsset - valid - returns true")
    void testValidateCertificationForAsset_Valid_ReturnsTrue() {
        when(certificationRepository.isValidForAsset(1L, 1L)).thenReturn(true);
        assertTrue(assetService.validateCertificationForAsset(1L, 1L));
    }

    @Test
    @DisplayName("validateCertificationForAsset - invalid - returns false")
    void testValidateCertificationForAsset_Invalid_ReturnsFalse() {
        when(certificationRepository.isValidForAsset(1L, 2L)).thenReturn(false);
        assertFalse(assetService.validateCertificationForAsset(1L, 2L));
    }

    @Test
    @DisplayName("checkOutAsset - missing certification - throws exception")
    void testCheckOutAsset_MissingCertification_ThrowsException() {
        Asset asset = new Asset(1L, "Forklift", "Available");
        when(assetRepository.findById(1L)).thenReturn(Optional.of(asset));
        when(certificationRepository.isValidForAsset(1L, 1L)).thenReturn(false);
        assertThrows(CertificationRequiredException.class, () -> assetService.checkOutAsset(1L, 1L));
    }

    @Test
    @DisplayName("getAssetHistory - no history - returns empty list")
    void testGetAssetHistory_NoHistory_ReturnsEmptyList() {
        when(assetRepository.getHistory(2L)).thenReturn(Collections.emptyList());
        List<AssetHistory> result = assetService.getAssetHistory(2L);
        assertTrue(result.isEmpty());
    }
}