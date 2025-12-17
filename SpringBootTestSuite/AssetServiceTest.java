package SpringBootTestSuite;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDate;
import java.util.*;

/**
 * AssetServiceTest - Comprehensive unit tests for AssetService covering checkout/return, certification validation, history, boundaries, and edge cases.
 */
public class AssetServiceTest {
    private AssetService assetService;

    @BeforeEach
    public void setUp() {
        assetService = new AssetService();
    }

    @Test
    public void testCreateAssetValid() {
        Asset asset = new Asset("Forklift", "Good");
        assertDoesNotThrow(() -> assetService.createAsset(asset));
    }

    @Test
    public void testCreateAssetInvalidInput() {
        Asset asset = new Asset("", null);
        assertThrows(IllegalArgumentException.class, () -> assetService.createAsset(asset));
    }

    @Test
    public void testCheckoutAssetWithValidCert() {
        Asset asset = new Asset("Forklift", "Good");
        Employee emp = new Employee(100, "John Doe");
        Certification cert = new Certification("Forklift", LocalDate.now().plusDays(30));
        assertTrue(assetService.checkoutAsset(asset, emp, cert));
    }

    @Test
    public void testCheckoutAssetWithExpiredCert() {
        Asset asset = new Asset("Forklift", "Good");
        Employee emp = new Employee(101, "Jane Doe");
        Certification cert = new Certification("Forklift", LocalDate.now().minusDays(1));
        assertFalse(assetService.checkoutAsset(asset, emp, cert));
    }

    @Test
    public void testReturnAsset() {
        Asset asset = new Asset("Scanner", "Good");
        Employee emp = new Employee(102, "Bob");
        assertTrue(assetService.returnAsset(asset, emp));
    }

    @Test
    public void testGetAssetHistory() {
        Asset asset = new Asset("Forklift", "Good");
        List<AssetHistory> history = assetService.getAssetHistory(asset);
        assertNotNull(history);
    }

    @Test
    public void testGetAssetsByEmployee() {
        Employee emp = new Employee(103, "Alice");
        List<Asset> assets = assetService.getAssetsByEmployee(emp);
        assertNotNull(assets);
    }

    @Test
    public void testGetOverdueAssets() {
        List<Asset> overdue = assetService.getOverdueAssets();
        assertNotNull(overdue);
    }

    @Test
    public void testUpdateAssetCondition() {
        Asset asset = new Asset("Forklift", "Damaged");
        assertTrue(assetService.updateAssetCondition(asset, "Repaired"));
    }

    @Test
    public void testBlockCheckoutForExpiredCert() {
        Asset asset = new Asset("Forklift", "Good");
        Employee emp = new Employee(104, "Eve");
        Certification cert = new Certification("Forklift", LocalDate.now().minusDays(10));
        assertTrue(assetService.blockCheckoutForExpiredCert(asset, emp, cert));
    }

    @Test
    public void testBulkCheckoutAssets() {
        Asset asset = new Asset("Scanner", "Good");
        List<Employee> employees = Arrays.asList(new Employee(105, "A"), new Employee(106, "B"));
        Certification cert = new Certification("Scanner", LocalDate.now().plusDays(30));
        assertEquals(2, assetService.bulkCheckoutAssets(asset, employees, cert));
    }

    @Test
    public void testGetAvailableAssets() {
        List<Asset> available = assetService.getAvailableAssets();
        assertNotNull(available);
    }

    @ParameterizedTest
    @ValueSource(strings = {"2023-01-01", "2024-12-31"})
    public void testBoundaryCheckoutDates(String dateStr) {
        LocalDate date = LocalDate.parse(dateStr);
        Asset asset = new Asset("Forklift", "Good");
        Employee emp = new Employee(107, "C");
        Certification cert = new Certification("Forklift", date);
        assertDoesNotThrow(() -> assetService.checkoutAsset(asset, emp, cert));
    }

    @Test
    public void testAlreadyCheckedOutAssets() {
        Asset asset = new Asset("Forklift", "Good");
        Employee emp = new Employee(108, "D");
        assetService.checkoutAsset(asset, emp, new Certification("Forklift", LocalDate.now().plusDays(30)));
        assertFalse(assetService.checkoutAsset(asset, emp, new Certification("Forklift", LocalDate.now().plusDays(30))));
    }

    @Test
    public void testMissingCertifications() {
        Asset asset = new Asset("Forklift", "Good");
        Employee emp = new Employee(109, "E");
        assertFalse(assetService.checkoutAsset(asset, emp, null));
    }
}
