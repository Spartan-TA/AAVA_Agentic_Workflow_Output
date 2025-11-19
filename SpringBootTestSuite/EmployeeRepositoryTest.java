public class EmployeeRepositoryTest {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Container
    private static final PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:latest")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass");

    @BeforeEach
    public void setUp() {
        // Initialize test data
    }

    @Test
    public void testFindByBadgeId() {
        // Arrange
        String badgeId = "12345";
        Employee employee = new Employee("John", "Doe", "john.doe@example.com", "1234567890", badgeId, "WORKER", "ACTIVE", LocalDate.now(), null, 1L, null, 1L);
        employeeRepository.save(employee);

        // Act
        Optional<Employee> result = employeeRepository.findByBadgeId(badgeId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(badgeId, result.get().getBadgeId());
    }

    @Test
    public void testFindByStatus() {
        // Arrange
        String status = "ACTIVE";
        Employee employee = new Employee("John", "Doe", "john.doe@example.com", "1234567890", "12345", "WORKER", status, LocalDate.now(), null, 1L, null, 1L);
        employeeRepository.save(employee);

        // Act
        List<Employee> result = employeeRepository.findByStatus(status);

        // Assert
        assertFalse(result.isEmpty());
        assertEquals(status, result.get(0).getStatus());
    }

    @AfterEach
    public void tearDown() {
        // Clean up resources
    }
}