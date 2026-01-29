@SpringBootTest
public class PerformanceServiceTest {
    @MockBean private PerformanceReviewRepository reviewRepository;
    @MockBean private EmployeeRepository employeeRepository;
    @Autowired private PerformanceService performanceService;

    private Employee testEmployee;
    private PerformanceReview testReview;
    private ReviewCycleDto reviewCycleDto;
    private PerformanceReviewDto reviewDto;

    @BeforeEach
    void setUp() {
        testEmployee = new Employee(1L, "John Doe", "EMP001", null, null, EmployeeStatus.ACTIVE, LocalDate.now());
        testReview = new PerformanceReview(1L, testEmployee, "2023-Q1", "Meet goals", 5, null);
        reviewCycleDto = new ReviewCycleDto("2023-Q1", LocalDate.now(), LocalDate.now().plusMonths(3));
        reviewDto = new PerformanceReviewDto("Meet goals", 5, "Good job");
    }

    @Test
    void testCreateReviewCycle_ValidInput_Success() {
        when(reviewRepository.saveCycle(any(ReviewCycle.class))).thenReturn(new ReviewCycle());
        ReviewCycle result = performanceService.createReviewCycle(reviewCycleDto);
        assertNotNull(result);
    }

    @Test
    void testAssignReview_ValidInput_Success() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(reviewRepository.save(any(PerformanceReview.class))).thenReturn(testReview);

        PerformanceReview result = performanceService.assignReview(1L, 1L);

        assertNotNull(result);
    }

    @Test
    void testSubmitReview_ValidInput_Success() {
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(testReview));
        when(reviewRepository.save(any(PerformanceReview.class))).thenReturn(testReview);

        PerformanceReview result = performanceService.submitReview(1L, reviewDto);

        assertNotNull(result);
    }

    @Test
    void testAcknowledgeReview_ValidInput_Success() {
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(testReview));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(reviewRepository.save(any(PerformanceReview.class))).thenReturn(testReview);

        PerformanceReview result = performanceService.acknowledgeReview(1L, 1L);

        assertNotNull(result);
    }

    @Test
    void testExportReviewToPdf_ValidInput_Success() {
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(testReview));
        byte[] pdf = performanceService.exportReviewToPdf(1L);
        assertNotNull(pdf);
    }
}