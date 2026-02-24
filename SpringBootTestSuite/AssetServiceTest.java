package SpringBootTestSuite;

import org.junit.jupiter.api.*;
import org.mockito.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import java.time.*;

class AssetServiceTest {

    @Mock
    private AssetRepository assetRepository;
    @Mock
    private AssignmentRepository assignmentRepository;
    @Mock
    private CertificationRepository certificationRepository;
    @InjectMocks
    private AssetService assetService;

    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        mocks.close();
    }

    @Test
    void testAssignAsset_Valid() {
        when(assignmentRepository.existsByAssetIdAndActiveTrue(1L)).thenReturn(false);
        when(assetRepository.findById(1L)).thenReturn(Optional.of(new Asset(1L)));
        Assignment assignment = new Assignment(1L, 2L);
        when(assignmentRepository.save(any(Assignment.class))).thenReturn(assignment);
        Assignment result = assetService.assignAsset(1L, 2L);
        assertNotNull(result);
        assertEquals(1L, result.getAssetId());
    }

    @Test
    void testAssignAsset_DuplicateAssignment() {
        when(assignmentRepository.existsByAssetIdAndActiveTrue(1L)).thenReturn(true);
        Exception ex = assertThrows(IllegalStateException.class, () ->
            assetService.assignAsset(1L, 2L));
        assertEquals("Asset already assigned", ex.getMessage());
    }

    @Test
    void testReturnAsset_WithoutCheckout() {
        when(assignmentRepository.findById(10L)).thenReturn(Optional.empty());
        Exception ex = assertThrows(NoSuchElementException.class, () ->
            assetService.returnAsset(10L, "GOOD"));
        assertEquals("Assignment not found", ex.getMessage());
    }

    @Test
    void testReturnAsset_InvalidCondition() {
        Assignment assignment = new Assignment(1L, 2L);
        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(assignment));
        Exception ex = assertThrows(IllegalArgumentException.class, () ->
            assetService.returnAsset(1L, "BROKEN!"));
        assertEquals("Invalid condition value", ex.getMessage());
    }

    @Test
    void testValidateCertification_Expired() {
        Certification cert = new Certification(2L, 1L, LocalDate.now().minusDays(1));
        when(certificationRepository.findByEmployeeIdAndAssetId(2L, 1L)).thenReturn(Optional.of(cert));
        boolean valid = assetService.validateCertification(2L, 1L);
        assertFalse(valid);
    }

    @Test
    void testValidateCertification_NullEmployeeId() {
        Exception ex = assertThrows(IllegalArgumentException.class, () ->
            assetService.validateCertification(null, 1L));
        assertEquals("Employee ID cannot be null", ex.getMessage());
    }
}