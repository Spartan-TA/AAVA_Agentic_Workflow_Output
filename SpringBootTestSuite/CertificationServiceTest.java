@SpringBootTest
public class CertificationServiceTest {
    @MockBean private CertificationRepository certificationRepository;
    @MockBean private EmployeeRepository employeeRepository;
    @Autowired private CertificationService certificationService;

    private Employee testEmployee;
    private Certification testCertification;
    private CertificationDto certificationDto;

    @BeforeEach
    void setUp() {
        testEmployee = new Employee(1L, "John Doe", "EMP001", null, null, EmployeeStatus.ACTIVE, LocalDate.now());
        testCertification = new Certification(1L, "Forklift", testEmployee, LocalDate.now().plusDays(30), "url");
        certificationDto = new CertificationDto("Forklift", 1L, LocalDate.now().plusDays(30), "url");
    }

    @Test
    void testCreateCertification_ValidInput_Success() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(certificationRepository.save(any(Certification.class))).thenReturn(testCertification);

        Certification result = certificationService.createCertification(certificationDto);

        assertNotNull(result);
        verify(certificationRepository).save(any(Certification.class));
    }

    @Test
    void testCreateCertification_NullDto_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> certificationService.createCertification(null));
    }

    @Test
    void testUpdateCertification_ValidInput_Success() {
        when(certificationRepository.findById(1L)).thenReturn(Optional.of(testCertification));
        when(certificationRepository.save(any(Certification.class))).thenReturn(testCertification);

        Certification result = certificationService.updateCertification(1L, certificationDto);

        assertNotNull(result);
    }

    @Test
    void testUpdateCertification_InvalidId_ThrowsException() {
        when(certificationRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> certificationService.updateCertification(999L, certificationDto));
    }

    @Test
    void testCheckExpiry_Expired_ReturnsTrue() {
        testCertification.setExpiryDate(LocalDate.now().minusDays(1));
        when(certificationRepository.findById(1L)).thenReturn(Optional.of(testCertification));
        assertTrue(certificationService.checkExpiry(1L));
    }

    @Test
    void testCheckExpiry_NotExpired_ReturnsFalse() {
        when(certificationRepository.findById(1L)).thenReturn(Optional.of(testCertification));
        assertFalse(certificationService.checkExpiry(1L));
    }

    @Test
    void testGetExpiringCertifications_ValidInput_Success() {
        List<Certification> certs = List.of(testCertification);
        when(certificationRepository.findExpiringWithinDays(anyInt())).thenReturn(certs);

        List<Certification> result = certificationService.getExpiringCertifications(30);

        assertEquals(1, result.size());
    }

    @Test
    void testValidateCertificationForAssignment_Valid_Success() {
        when(certificationRepository.findValidByEmployeeAndType(1L, "Forklift", LocalDate.now())).thenReturn(Optional.of(testCertification));
        assertDoesNotThrow(() -> certificationService.validateCertificationForAssignment(1L, "Forklift"));
    }

    @Test
    void testValidateCertificationForAssignment_Expired_ThrowsException() {
        when(certificationRepository.findValidByEmployeeAndType(1L, "Forklift", LocalDate.now())).thenReturn(Optional.empty());
        assertThrows(CertificationExpiredException.class, () -> certificationService.validateCertificationForAssignment(1L, "Forklift"));
    }
}