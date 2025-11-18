public class EmployeeServiceTest {

    private EmployeeService employeeService;

    @BeforeEach
    public void setUp() {
        employeeService = new EmployeeService();
    }

    @Test
    public void testCreateEmployeeValidInput() {
        // Arrange
        Employee employee = new Employee("John Doe", "12345", "Worker", "Warehouse", "A", LocalDate.now(), "Active");

        // Act
        Employee result = employeeService.createEmployee(employee);

        // Assert
        assertNotNull(result);
        assertEquals("John Doe", result.getName());
        assertEquals("12345", result.getBadgeId());
    }

    @Test
    public void testCreateEmployeeNullInput() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> employeeService.createEmployee(null));
    }

    @Test
    public void testCreateEmployeeDuplicateBadgeId() {
        // Arrange
        Employee employee1 = new Employee("John Doe", "12345", "Worker", "Warehouse", "A", LocalDate.now(), "Active");
        Employee employee2 = new Employee("Jane Smith", "12345", "Supervisor", "Warehouse", "B", LocalDate.now(), "Active");
        employeeService.createEmployee(employee1);

        // Act & Assert
        assertThrows(DuplicateBadgeIdException.class, () -> employeeService.createEmployee(employee2));
    }

    @Test
    public void testGetEmployeeByIdValidId() {
        // Arrange
        Employee employee = new Employee("John Doe", "12345", "Worker", "Warehouse", "A", LocalDate.now(), "Active");
        employeeService.createEmployee(employee);

        // Act
        Employee result = employeeService.getEmployeeById(employee.getId());

        // Assert
        assertNotNull(result);
        assertEquals("John Doe", result.getName());
    }

    @Test
    public void testGetEmployeeByIdInvalidId() {
        // Act & Assert
        assertThrows(EmployeeNotFoundException.class, () -> employeeService.getEmployeeById(999));
    }

    @Test
    public void testUpdateEmployeeValidInput() {
        // Arrange
        Employee employee = new Employee("John Doe", "12345", "Worker", "Warehouse", "A", LocalDate.now(), "Active");
        employeeService.createEmployee(employee);
        employee.setName("John Updated");

        // Act
        Employee result = employeeService.updateEmployee(employee);

        // Assert
        assertNotNull(result);
        assertEquals("John Updated", result.getName());
    }

    @Test
    public void testUpdateEmployeeInvalidId() {
        // Arrange
        Employee employee = new Employee("Invalid", "99999", "Worker", "Warehouse", "A", LocalDate.now(), "Active");

        // Act & Assert
        assertThrows(EmployeeNotFoundException.class, () -> employeeService.updateEmployee(employee));
    }

    @Test
    public void testDeleteEmployeeValidId() {
        // Arrange
        Employee employee = new Employee("John Doe", "12345", "Worker", "Warehouse", "A", LocalDate.now(), "Active");
        employeeService.createEmployee(employee);

        // Act
        employeeService.deleteEmployee(employee.getId());

        // Assert
        assertThrows(EmployeeNotFoundException.class, () -> employeeService.getEmployeeById(employee.getId()));
    }

    @Test
    public void testDeleteEmployeeInvalidId() {
        // Act & Assert
        assertThrows(EmployeeNotFoundException.class, () -> employeeService.deleteEmployee(999));
    }
}