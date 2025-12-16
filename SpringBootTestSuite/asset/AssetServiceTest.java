import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

public class AssetServiceTest {
    @Mock
    private AssetRepository assetRepository;

    @InjectMocks
    private AssetService assetService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testAssignAsset_ValidInput() {
        Asset asset = new Asset("A123", "Scanner", "EMP123", LocalDate.now(), null, "Good");
        when(assetRepository.save(any())).thenReturn(asset);
        Asset result = assetService.assignAsset(asset);
        assertEquals("EMP123", result.getAssignedTo());
        assertEquals("Scanner", result.getType());
    }

    @Test
    public void testAssignAsset_NullInput_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> assetService.assignAsset(null));
    }

    @Test
    public void testReturnAsset_WithCondition() {
        Asset asset = new Asset("A123", "Scanner", "EMP123", LocalDate.now().minusDays(5), LocalDate.now(), "Damaged");
        when(assetRepository.save(any())).thenReturn(asset);
        Asset returned = assetService.returnAsset(asset, "Damaged");
        assertEquals("Damaged", returned.getCondition());
        assertNotNull(returned.getReturnDate());
    }

    @Test
    public void testDetectOverdueAssets() {
        Asset asset1 = new Asset("A123", "Scanner", "EMP123", LocalDate.now().minusDays(10), null, "Good");
        Asset asset2 = new Asset("A124", "Forklift", "EMP124", LocalDate.now().minusDays(2), null, "Good");
        when(assetRepository.findAllAssigned()).thenReturn(Arrays.asList(asset1, asset2));
        List<Asset> overdue = assetService.getOverdueAssets(7);
        assertEquals(1, overdue.size());
        assertEquals("A123", overdue.get(0).getAssetId());
    }

    @Test
    public void testConditionTracking_BoundaryConditions() {
        Asset asset = new Asset("A125", "PPE", "EMP125", LocalDate.now(), null, "Excellent");
        asset.setCondition("Poor");
        assertEquals("Poor", asset.getCondition());
    }
}
