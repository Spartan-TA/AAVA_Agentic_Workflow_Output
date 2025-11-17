public class EmployeeServiceTest {

    private EmployeeService employeeService;

    @BeforeEach
    public void setUp() {
        employeeService = new EmployeeService();
    }

    @Test
    public void testCreateEmployeeValidInput() {
        // Arrange
        EmployeeRequestDto requestDto = new EmployeeRequestDto();
        requestDto.setBadgeId("12345");
        requestDto.setFirstName("John");
        requestDto.setLastName("Doe");
        requestDto.setEmail("john.doe@example.com");
        requestDto.setRole(EmployeeRole.WORKER);
        requestDto.setHireDate(LocalDate.now());
        requestDto.setStatus(EmployeeStatus.ACTIVE);

        // Act
        EmployeeResponseDto responseDto = employeeService.createEmployee(requestDto);

        // Assert
        assertNotNull(responseDto);
        assertEquals("12345", responseDto.getBadgeId());
    }

    @Test
    public void testCreateEmployeeDuplicateBadgeId() {
        // Arrange
        EmployeeRequestDto requestDto = new EmployeeRequestDto();
        requestDto.setBadgeId("12345");
        requestDto.setFirstName("John");
        requestDto.setLastName("Doe");
        requestDto.setEmail("john.doe@example.com");
        requestDto.setRole(EmployeeRole.WORKER);
        requestDto.setHireDate(LocalDate.now());
        requestDto.setStatus(EmployeeStatus.ACTIVE);

        employeeService.createEmployee(requestDto);

        // Act & Assert
        assertThrows(DuplicateResourceException.class, () -> employeeService.createEmployee(requestDto));
    }

    @Test
    public void testGetEmployeeByIdValidId() {
        // Arrange
        Long id = 1L;

        // Act
        EmployeeResponseDto responseDto = employeeService.getEmployeeById(id);

        // Assert
        assertNotNull(responseDto);
        assertEquals(id, responseDto.getId());
    }

    @Test
    public void testGetEmployeeByIdInvalidId() {
        // Arrange
        Long id = 999L;

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> employeeService.getEmployeeById(id));
    }

    @Test
    public void testUpdateEmployeeValidInput() {
        // Arrange
        Long id = 1L;
        EmployeeRequestDto requestDto = new EmployeeRequestDto();
        requestDto.setBadgeId("12345");
        requestDto.setFirstName("John");
        requestDto.setLastName("Doe");
        requestDto.setEmail("john.doe@example.com");
        requestDto.setRole(EmployeeRole.WORKER);
        requestDto.setHireDate(LocalDate.now());
        requestDto.setStatus(EmployeeStatus.ACTIVE);

        // Act
        EmployeeResponseDto responseDto = employeeService.updateEmployee(id, requestDto);

        // Assert
        assertNotNull(responseDto);
        assertEquals("12345", responseDto.getBadgeId());
    }

    @Test
    public void testUpdateEmployeeInvalidId() {
        // Arrange
        Long id = 999L;
        EmployeeRequestDto requestDto = new EmployeeRequestDto();
        requestDto.setBadgeId("12345");
        requestDto.setFirstName("John");
        requestDto.setLastName("Doe");
        requestDto.setEmail("john.doe@example.com");
        requestDto.setRole(EmployeeRole.WORKER);
        requestDto.setHireDate(LocalDate.now());
        requestDto.setStatus(EmployeeStatus.ACTIVE);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> employeeService.updateEmployee(id, requestDto));
    }

    @Test
    public void testDeleteEmployeeValidId() {
        // Arrange
        Long id = 1L;

        // Act
        employeeService.deleteEmployee(id);

        // Assert
        assertThrows(ResourceNotFoundException.class, () -> employeeService.getEmployeeById(id));
    }

    @Test
    public void testDeleteEmployeeInvalidId() {
        // Arrange
        Long id = 999L;

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> employeeService.deleteEmployee(id));
    }
}