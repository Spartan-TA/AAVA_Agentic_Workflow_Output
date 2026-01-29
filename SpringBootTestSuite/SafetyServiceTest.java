@SpringBootTest
public class SafetyServiceTest {
    @MockBean private SafetyIncidentRepository safetyIncidentRepository;
    @MockBean private EmployeeRepository employeeRepository;
    @Autowired private SafetyService safetyService;

    private Employee testEmployee;
    private SafetyIncident testIncident;
    private SafetyIncidentDto incidentDto;

    @BeforeEach
    void setUp() {
        testEmployee = new Employee(1L, "John Doe", "EMP001", null, null, EmployeeStatus.ACTIVE, LocalDate.now());
        testIncident = new SafetyIncident(1L, testEmployee, LocalDate.now(), Severity.HIGH, "Forklift accident", IncidentStatus.OPEN);
        incidentDto = new SafetyIncidentDto(1L, LocalDate.now(), Severity.HIGH, "Forklift accident");
    }

    @Test
    void testRecordIncident_ValidInput_Success() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(safetyIncidentRepository.save(any(SafetyIncident.class))).thenReturn(testIncident);

        SafetyIncident result = safetyService.recordIncident(incidentDto);

        assertNotNull(result);
        verify(safetyIncidentRepository).save(any(SafetyIncident.class));
    }

    @Test
    void testRecordIncident_NullDto_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> safetyService.recordIncident(null));
    }

    @Test
    void testUpdateIncidentStatus_ValidInput_Success() {
        when(safetyIncidentRepository.findById(1L)).thenReturn(Optional.of(testIncident));
        when(safetyIncidentRepository.save(any(SafetyIncident.class))).thenReturn(testIncident);

        SafetyIncident result = safetyService.updateIncidentStatus(1L, IncidentStatus.CLOSED);

        assertNotNull(result);
        assertEquals(IncidentStatus.CLOSED, result.getStatus());
    }

    @Test
    void testUpdateIncidentStatus_InvalidId_ThrowsException() {
        when(safetyIncidentRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> safetyService.updateIncidentStatus(999L, IncidentStatus.CLOSED));
    }

    @Test
    void testGenerateOSHAReport_ValidYear_Success() {
        List<SafetyIncident> incidents = List.of(testIncident);
        when(safetyIncidentRepository.findByYear(2023)).thenReturn(incidents);

        OSHAReport report = safetyService.generateOSHAReport(2023);

        assertNotNull(report);
    }

    @Test
    void testGetIncidentMetrics_ValidInput_Success() {
        List<SafetyIncident> incidents = List.of(testIncident);
        when(safetyIncidentRepository.findByDateRange(any(), any())).thenReturn(incidents);

        SafetyMetrics metrics = safetyService.getIncidentMetrics(LocalDate.now().minusDays(30), LocalDate.now());

        assertNotNull(metrics);
    }
}