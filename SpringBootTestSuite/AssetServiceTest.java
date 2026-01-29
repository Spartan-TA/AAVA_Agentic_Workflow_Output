@SpringBootTest
public class AssetServiceTest {
    @MockBean private AssetRepository assetRepository;
    @MockBean private EmployeeRepository employeeRepository;
    @MockBean private CertificationService certificationService;
    @Autowired private AssetService assetService;

    private Employee testEmployee;
    private Asset testAsset;

    @BeforeEach
    void setUp() {
        testEmployee = new Employee(1L, "John Doe", "EMP001", null, null, EmployeeStatus.ACTIVE, LocalDate.now());
        testAsset = new Asset(1L, "Forklift", "VEHICLE", null, null, AssetStatus.AVAILABLE);
    }

    @Test
    void testAssignAsset_ValidInput_Success() {
        when(assetRepository.findById(1L)).thenReturn(Optional.of(testAsset));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        doNothing().when(certificationService).validateCertificationForAsset(1L, 1L);
        when(assetRepository.save(any(Asset.class))).thenReturn(testAsset);

        Asset result = assetService.assignAsset(1L, 1L);

        assertNotNull(result);
        verify(assetRepository).save(any(Asset.class));
    }

    @Test
    void testAssignAsset_InvalidAssetId_ThrowsException() {
        when(assetRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> assetService.assignAsset(999L, 1L));
    }

    @Test
    void testCheckInAsset_ValidInput_Success() {
        testAsset.setAssignedTo(testEmployee);
        when(assetRepository.findById(1L)).thenReturn(Optional.of(testAsset));
        when(assetRepository.save(any(Asset.class))).thenReturn(testAsset);

        Asset result = assetService.checkInAsset(1L);

        assertNotNull(result);
        assertNull(result.getAssignedTo());
    }

    @Test
    void testCheckOutAsset_ValidInput_Success() {
        when(assetRepository.findById(1L)).thenReturn(Optional.of(testAsset));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        doNothing().when(certificationService).validateCertificationForAsset(1L, 1L);
        when(assetRepository.save(any(Asset.class))).thenReturn(testAsset);

        Asset result = assetService.checkOutAsset(1L, 1L);

        assertNotNull(result);
        assertEquals(testEmployee, result.getAssignedTo());
    }

    @Test
    void testGetOverdueAssets_ValidInput_Success() {
        List<Asset> overdue = List.of(testAsset);
        when(assetRepository.findOverdueAssets()).thenReturn(overdue);

        List<Asset> result = assetService.getOverdueAssets();

        assertEquals(1, result.size());
    }

    @Test
    void testValidateCertificationForAsset_Valid_Success() {
        doNothing().when(certificationService).validateCertificationForAssignment(1L, "Forklift");
        assertDoesNotThrow(() -> assetService.validateCertificationForAsset(1L, 1L));
    }
}