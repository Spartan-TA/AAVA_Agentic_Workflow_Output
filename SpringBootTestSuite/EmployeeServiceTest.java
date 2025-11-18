public class EmployeeServiceTest {

    @Test
    public void testGetAllEmployees() {
        // Arrange
        EmployeeService service = new EmployeeService();
        // Act
        List<Employee> employees = service.getAllEmployees();
        // Assert
        assertNotNull(employees);
        assertTrue(employees.size() > 0);
    }

    @Test
    public void testGetEmployeeByBadgeId() {
        // Arrange
        EmployeeService service = new EmployeeService();
        String badgeId = "12345";
        // Act
        Employee employee = service.getEmployeeByBadgeId(badgeId);
        // Assert
        assertNotNull(employee);
        assertEquals(badgeId, employee.getBadgeId());
    }

    @Test
    public void testCreateEmployee() {
        // Arrange
        EmployeeService service = new EmployeeService();
        Employee newEmployee = new Employee("John Doe", "12345", "Worker", "Warehouse", "A", LocalDate.now(), "Active");
        // Act
        Employee createdEmployee = service.createEmployee(newEmployee);
        // Assert
        assertNotNull(createdEmployee);
        assertEquals(newEmployee.getBadgeId(), createdEmployee.getBadgeId());
    }

    @Test
    public void testUpdateEmployee() {
        // Arrange
        EmployeeService service = new EmployeeService();
        Employee existingEmployee = new Employee("John Doe", "12345", "Worker", "Warehouse", "A", LocalDate.now(), "Active");
        existingEmployee.setDepartment("Updated Department");
        // Act
        Employee updatedEmployee = service.updateEmployee(existingEmployee);
        // Assert
        assertNotNull(updatedEmployee);
        assertEquals("Updated Department", updatedEmployee.getDepartment());
    }

    @Test
    public void testSoftDeleteEmployee() {
        // Arrange
        EmployeeService service = new EmployeeService();
        String badgeId = "12345";
        // Act
        boolean isDeleted = service.softDeleteEmployee(badgeId);
        // Assert
        assertTrue(isDeleted);
    }
}