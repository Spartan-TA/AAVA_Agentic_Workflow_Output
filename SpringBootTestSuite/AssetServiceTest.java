import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.time.LocalDate;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AssetServiceTest {
    @Mock
    private AssetRepository assetRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private CertificationRepository certificationRepository;

    @InjectMocks
    private AssetService assetService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCheckoutAsset_Valid() {
        Asset asset = new Asset();
        asset.setId(1L);
        asset.setRequiresCertification(false);
        Employee employee = new Employee();
        employee.setId(1L);
        when(assetRepository.findById(1L)).thenReturn(Optional.of(asset));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(assetRepository.save(any(Asset.class))).thenReturn(asset);
        Asset result = assetService.checkoutAsset(1L, 1L);
        assertNotNull(result);
        assertEquals(1L, result.getAssignedTo());
        assertNotNull(result.getCheckoutDate());
    }

    @Test
    void testCheckoutAsset_AssetNotFound() {
        when(assetRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> assetService.checkoutAsset(1L, 1L));
    }

    @Test
    void testCheckoutAsset_EmployeeNotFound() {
        Asset asset = new Asset();
        asset.setId(1L);
        when(assetRepository.findById(1L)).thenReturn(Optional.of(asset));
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> assetService.checkoutAsset(1L, 1L));
    }

    @Test
    void testCheckoutAsset_MissingCertification() {
        Asset asset = new Asset();
        asset.setId(1L);
        asset.setRequiresCertification(true);
        asset.setAssetType("Forklift");
        Employee employee = new Employee();
        employee.setId(1L);
        when(assetRepository.findById(1L)).thenReturn(Optional.of(asset));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(certificationRepository.findValidCertificationForEmployee(1L, "Forklift")).thenReturn(Optional.empty());
        assertThrows(ValidationException.class, () -> assetService.checkoutAsset(1L, 1L));
    }

    @Test
    void testReturnAsset_Valid() {
        Asset asset = new Asset();
        asset.setId(1L);
        asset.setAssignedTo(1L);
        asset.setCheckoutDate(LocalDate.now().minusDays(5));
        when(assetRepository.findById(1L)).thenReturn(Optional.of(asset));
        when(assetRepository.save(any(Asset.class))).thenReturn(asset);
        Asset result = assetService.returnAsset(1L, "GOOD");
        assertNotNull(result);
        assertNull(result.getAssignedTo());
        assertNotNull(result.getReturnDate());
        assertEquals("GOOD", result.getCondition());
    }

    @Test
    void testReturnAsset_NotCheckedOut() {
        Asset asset = new Asset();
        asset.setId(1L);
        asset.setAssignedTo(null);
        when(assetRepository.findById(1L)).thenReturn(Optional.of(asset));
        assertThrows(ValidationException.class, () -> assetService.returnAsset(1L, "GOOD"));
    }
}