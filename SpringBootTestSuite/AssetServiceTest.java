import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.*;

public class AssetServiceTest {
    @Mock
    private AssetRepository assetRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private CertificationService certificationService;
    @InjectMocks
    private AssetService assetService;
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
    void testAssignAssetToEmployee_ValidInput() {
        Employee employee = new Employee("John Doe", "B123", "WORKER", "Shipping", "A", new Date(), "ACTIVE");
        Asset asset = new Asset("Forklift", "A001", "Available");
        when(employeeRepository.findByBadgeId("B123")).thenReturn(Optional.of(employee));
        when(assetRepository.findByAssetId("A001")).thenReturn(Optional.of(asset));
        when(certificationService.checkCertificationQualification("B123", "Forklift")).thenReturn(true);
        when(assetRepository.save(any(Asset.class))).thenReturn(asset);
        Asset result = assetService.assignAssetToEmployee("A001", "B123");
        assertEquals("Forklift", result.getType());
        assertEquals("Assigned", result.getStatus());
    }

    @Test
    void testAssignAssetToEmployee_MissingCertification() {
        Employee employee = new Employee("John Doe", "B123", "WORKER", "Shipping", "A", new Date(), "ACTIVE");
        Asset asset = new Asset("Forklift", "A001", "Available");
        when(employeeRepository.findByBadgeId("B123")).thenReturn(Optional.of(employee));
        when(assetRepository.findByAssetId("A001")).thenReturn(Optional.of(asset));
        when(certificationService.checkCertificationQualification("B123", "Forklift")).thenReturn(false);
        assertThrows(CertificationRequiredException.class, () -> assetService.assignAssetToEmployee("A001", "B123"));
    }

    @Test
    void testAssignAssetToEmployee_InvalidAssetId() {
        when(assetRepository.findByAssetId("INVALID")).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> assetService.assignAssetToEmployee("INVALID", "B123"));
    }

    @Test
    void testAssignAssetToEmployee_InvalidBadgeId() {
        when(employeeRepository.findByBadgeId("BADGE999")).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> assetService.assignAssetToEmployee("A001", "BADGE999"));
    }

    @Test
    void testReturnAsset_ValidInput() {
        Asset asset = new Asset("Forklift", "A001", "Assigned");
        when(assetRepository.findByAssetId("A001")).thenReturn(Optional.of(asset));
        when(assetRepository.save(any(Asset.class))).thenReturn(asset);
        Asset result = assetService.returnAsset("A001");
        assertEquals("Available", result.getStatus());
    }

    @Test
    void testReturnAsset_InvalidAssetId() {
        when(assetRepository.findByAssetId("INVALID")).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> assetService.returnAsset("INVALID"));
    }

    @Test
    void testAssignAssetToEmployee_BoundaryValues() {
        Employee employee = new Employee("A", "B126", "WORKER", "Shipping", "A", new Date(), "ACTIVE");
        Asset asset = new Asset("A", "A002", "Available");
        when(employeeRepository.findByBadgeId("B126")).thenReturn(Optional.of(employee));
        when(assetRepository.findByAssetId("A002")).thenReturn(Optional.of(asset));
        when(certificationService.checkCertificationQualification("B126", "A")).thenReturn(true);
        when(assetRepository.save(any(Asset.class))).thenReturn(asset);
        assertDoesNotThrow(() -> assetService.assignAssetToEmployee("A002", "B126"));
    }

    @Test
    void testAssignAssetToEmployee_EmptyAssetType() {
        Employee employee = new Employee("John Doe", "B123", "WORKER", "Shipping", "A", new Date(), "ACTIVE");
        Asset asset = new Asset("", "A003", "Available");
        when(employeeRepository.findByBadgeId("B123")).thenReturn(Optional.of(employee));
        when(assetRepository.findByAssetId("A003")).thenReturn(Optional.of(asset));
        when(certificationService.checkCertificationQualification("B123", "")).thenReturn(true);
        assertThrows(ValidationException.class, () -> assetService.assignAssetToEmployee("A003", "B123"));
    }
}
