package SpringBootTestSuite;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.service.AssetService;
import com.example.repository.AssetRepository;
import com.example.repository.EmployeeRepository;
import com.example.repository.CertificationRepository;
import com.example.model.Asset;
import com.example.model.Employee;
import com.example.model.Certification;
import com.example.exception.AssetNotAvailableException;
import com.example.exception.EmployeeNotCertifiedException;
import com.example.exception.AssetNotCheckedOutException;

@ExtendWith(MockitoExtension.class)
public class AssetServiceTest {
    @Mock
    private AssetRepository assetRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private CertificationRepository certificationRepository;
    @InjectMocks
    private AssetService assetService;

    private Asset asset;
    private Employee employee;
    private Certification cert;

    @BeforeEach
    void setUp() {
        asset = new Asset();
        asset.setId(1L);
        asset.setAvailable(true);
        employee = new Employee();
        employee.setId(1L);
        cert = new Certification();
        cert.setEmployee(employee);
        cert.setType("Forklift");
    }

    @Test
    void testCheckoutAsset_Success() {
        when(assetRepository.findById(1L)).thenReturn(Optional.of(asset));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(certificationRepository.findByEmployeeIdAndType(1L, "Forklift")).thenReturn(Optional.of(cert));
        assetService.checkoutAsset(1L, 1L, "Forklift");
        verify(assetRepository).save(asset);
        assertFalse(asset.isAvailable());
    }

    @Test
    void testCheckoutAsset_AssetNotAvailable_ThrowsException() {
        asset.setAvailable(false);
        when(assetRepository.findById(1L)).thenReturn(Optional.of(asset));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(certificationRepository.findByEmployeeIdAndType(1L, "Forklift")).thenReturn(Optional.of(cert));
        assertThrows(AssetNotAvailableException.class, () -> assetService.checkoutAsset(1L, 1L, "Forklift"));
    }

    @Test
    void testCheckoutAsset_EmployeeNotCertified_ThrowsException() {
        when(assetRepository.findById(1L)).thenReturn(Optional.of(asset));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(certificationRepository.findByEmployeeIdAndType(1L, "Forklift")).thenReturn(Optional.empty());
        assertThrows(EmployeeNotCertifiedException.class, () -> assetService.checkoutAsset(1L, 1L, "Forklift"));
    }

    @Test
    void testReturnAsset_Success() {
        asset.setAvailable(false);
        when(assetRepository.findById(1L)).thenReturn(Optional.of(asset));
        assetService.returnAsset(1L);
        verify(assetRepository).save(asset);
        assertTrue(asset.isAvailable());
    }

    @Test
    void testReturnAsset_NotCheckedOut_ThrowsException() {
        asset.setAvailable(true);
        when(assetRepository.findById(1L)).thenReturn(Optional.of(asset));
        assertThrows(AssetNotCheckedOutException.class, () -> assetService.returnAsset(1L));
    }

    @Test
    void testGetOverdueAssets_ReturnsCorrectList() {
        List<Asset> overdue = Arrays.asList(asset);
        when(assetRepository.findOverdueAssets()).thenReturn(overdue);
        List<Asset> result = assetService.getOverdueAssets();
        assertEquals(1, result.size());
    }

    @Test
    void testGetAssetHistory_ReturnsCorrectHistory() {
        List<String> history = Arrays.asList("Checked out by John", "Returned by John");
        when(assetRepository.getAssetHistory(1L)).thenReturn(history);
        List<String> result = assetService.getAssetHistory(1L);
        assertEquals(2, result.size());
        assertEquals("Checked out by John", result.get(0));
    }

    @Test
    void testUpdateAssetCondition_Success() {
        when(assetRepository.findById(1L)).thenReturn(Optional.of(asset));
        assetService.updateAssetCondition(1L, "Good");
        verify(assetRepository).save(asset);
        assertEquals("Good", asset.getCondition());
    }
}
