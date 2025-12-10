import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.*;
import javax.validation.ValidationException;

@ExtendWith(MockitoExtension.class)
public class AssetServiceTest {
    @Mock
    private AssetRepository assetRepository;
    @Mock
    private CertificationRepository certificationRepository;
    @InjectMocks
    private AssetServiceImpl assetService;

    private AssetDto validAssetDto;
    private Asset validAsset;
    private Certification validCertification;

    @BeforeEach
    void setUp() {
        validAssetDto = new AssetDto();
        validAssetDto.setAssetTag("FORKLIFT-001");
        validAssetDto.setEmployeeId(1L);
        validAssetDto.setCertificationId(1L);

        validAsset = new Asset();
        validAsset.setId(1L);
        validAsset.setAssetTag("FORKLIFT-001");
        validAsset.setAssignedTo(1L);
        validAsset.setCertificationId(1L);
        validAsset.setAssigned(true);

        validCertification = new Certification();
        validCertification.setId(1L);
        validCertification.setType("Forklift");
        validCertification.setEmployeeId(1L);
    }

    @Test
    void testAssignAsset_ValidInput() {
        when(certificationRepository.findById(1L)).thenReturn(Optional.of(validCertification));
        when(assetRepository.findByAssetTag("FORKLIFT-001")).thenReturn(Optional.of(validAsset));
        validAsset.setAssigned(false);
        when(assetRepository.save(any(Asset.class))).thenReturn(validAsset);
        Asset result = assetService.assign(validAssetDto);
        assertNotNull(result);
        assertTrue(result.isAssigned());
        verify(assetRepository, times(1)).save(any(Asset.class));
    }

    @Test
    void testAssignAsset_MissingCertification() {
        when(certificationRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ValidationException.class, () -> assetService.assign(validAssetDto));
    }

    @Test
    void testAssignAsset_AlreadyAssigned() {
        when(certificationRepository.findById(1L)).thenReturn(Optional.of(validCertification));
        when(assetRepository.findByAssetTag("FORKLIFT-001")).thenReturn(Optional.of(validAsset));
        validAsset.setAssigned(true);
        assertThrows(ValidationException.class, () -> assetService.assign(validAssetDto));
    }

    @Test
    void testReturnAsset_ValidId() {
        validAsset.setAssigned(true);
        when(assetRepository.findById(1L)).thenReturn(Optional.of(validAsset));
        validAsset.setAssigned(false);
        when(assetRepository.save(any(Asset.class))).thenReturn(validAsset);
        Asset result = assetService.returnAsset(1L);
        assertNotNull(result);
        assertFalse(result.isAssigned());
    }

    @Test
    void testReturnAsset_NotAssigned() {
        validAsset.setAssigned(false);
        when(assetRepository.findById(1L)).thenReturn(Optional.of(validAsset));
        assertThrows(ValidationException.class, () -> assetService.returnAsset(1L));
    }

    @Test
    void testReturnAsset_NonExistentId() {
        when(assetRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> assetService.returnAsset(2L));
    }

    @Test
    void testListAssets_WithResults() {
        List<Asset> assets = Arrays.asList(validAsset);
        when(assetRepository.findAll()).thenReturn(assets);
        List<Asset> result = assetService.list();
        assertNotNull(result);
        assertEquals(1, result.size());
    }
}