public class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeService employeeService;

    @BeforeEach
    public void setUp() {
        // Initialize mocks and test data
    }

    @Test
    public void testCreateEmployee() throws Exception {
        // Arrange
        EmployeeRequest request = new EmployeeRequest("John", "Doe", "john.doe@example.com", "1234567890", "12345", "WORKER", "ACTIVE", LocalDate.now(), null, 1L, null, 1L);
        when(employeeService.createEmployee(any(EmployeeRequest.class))).thenReturn(new EmployeeResponse(1L, "John", "Doe", "john.doe@example.com", "1234567890", "12345", "WORKER", "ACTIVE", LocalDate.now(), null, 1L, null, 1L));

        // Act & Assert
        mockMvc.perform(post("/api/v1/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("John"));
    }

    @Test
    public void testGetEmployeeById() throws Exception {
        // Arrange
        Long employeeId = 1L;
        when(employeeService.getEmployeeById(employeeId)).thenReturn(new EmployeeResponse(employeeId, "John", "Doe", "john.doe@example.com", "1234567890", "12345", "WORKER", "ACTIVE", LocalDate.now(), null, 1L, null, 1L));

        // Act & Assert
        mockMvc.perform(get("/api/v1/employees/{id}", employeeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(employeeId));
    }

    @Test
    public void testDeleteEmployee() throws Exception {
        // Arrange
        Long employeeId = 1L;
        doNothing().when(employeeService).softDeleteEmployee(employeeId);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/employees/{id}", employeeId))
                .andExpect(status().isNoContent());
    }

    @AfterEach
    public void tearDown() {
        // Clean up resources
    }
}