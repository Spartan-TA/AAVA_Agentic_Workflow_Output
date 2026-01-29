@SpringBootTest
public class OnboardingServiceTest {
    @MockBean private EmployeeRepository employeeRepository;
    @MockBean private AssetService assetService;
    @MockBean private TrainingTaskService trainingTaskService;
    @Autowired private OnboardingService onboardingService;

    private Employee testEmployee;

    @BeforeEach
    void setUp() {
        testEmployee = new Employee(1L, "John Doe", "EMP001", null, null, EmployeeStatus.ACTIVE, LocalDate.now());
    }

    @Test
    void testProvisionNewHire_ValidInput_Success() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        doNothing().when(trainingTaskService).generateTrainingTasks(1L);
        doNothing().when(assetService).assignInitialAssets(1L);

        assertDoesNotThrow(() -> onboardingService.provisionNewHire(1L));
    }

    @Test
    void testGenerateTrainingTasks_ValidInput_Success() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        doNothing().when(trainingTaskService).generateTrainingTasks(1L);

        assertDoesNotThrow(() -> onboardingService.generateTrainingTasks(1L));
    }

    @Test
    void testAssignInitialAssets_ValidInput_Success() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        doNothing().when(assetService).assignInitialAssets(1L);

        assertDoesNotThrow(() -> onboardingService.assignInitialAssets(1L));
    }

    @Test
    void testDeprovisionEmployee_ValidInput_Success() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        doNothing().when(assetService).collectAssets(1L);

        assertDoesNotThrow(() -> onboardingService.deprovisionEmployee(1L));
    }

    @Test
    void testCollectAssets_ValidInput_Success() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        doNothing().when(assetService).collectAssets(1L);

        assertDoesNotThrow(() -> onboardingService.collectAssets(1L));
    }
}