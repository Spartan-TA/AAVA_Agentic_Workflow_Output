import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;
import javax.validation.*;
import java.util.Set;

public class EmployeeDTOValidationTest {
    private Validator validator;

    @BeforeEach
    public void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void testValidEmployeeDTO() {
        EmployeeDTO dto = new EmployeeDTO("John Doe", "B123", "HR", "2022-01-01", "Active");
        Set<ConstraintViolation<EmployeeDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    public void testNullName() {
        EmployeeDTO dto = new EmployeeDTO(null, "B123", "HR", "2022-01-01", "Active");
        Set<ConstraintViolation<EmployeeDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testEmptyBadgeId() {
        EmployeeDTO dto = new EmployeeDTO("Jane Smith", "", "Finance", "2022-01-01", "Active");
        Set<ConstraintViolation<EmployeeDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testInvalidHireDateFormat() {
        EmployeeDTO dto = new EmployeeDTO("Jane Smith", "B124", "Finance", "01-01-2022", "Active");
        Set<ConstraintViolation<EmployeeDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testNullDepartment() {
        EmployeeDTO dto = new EmployeeDTO("Jane Smith", "B124", null, "2022-01-01", "Active");
        Set<ConstraintViolation<EmployeeDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testInactiveStatus() {
        EmployeeDTO dto = new EmployeeDTO("Jane Smith", "B124", "Finance", "2022-01-01", "Inactive");
        Set<ConstraintViolation<EmployeeDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    public void testNullStatus() {
        EmployeeDTO dto = new EmployeeDTO("Jane Smith", "B124", "Finance", "2022-01-01", null);
        Set<ConstraintViolation<EmployeeDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testLongName() {
        String longName = "A".repeat(256);
        EmployeeDTO dto = new EmployeeDTO(longName, "B124", "Finance", "2022-01-01", "Active");
        Set<ConstraintViolation<EmployeeDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testSpecialCharactersInBadgeId() {
        EmployeeDTO dto = new EmployeeDTO("Jane Smith", "B@124!", "Finance", "2022-01-01", "Active");
        Set<ConstraintViolation<EmployeeDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
    }

    @AfterEach
    public void tearDown() {
        // Clean up resources if needed
    }
}
