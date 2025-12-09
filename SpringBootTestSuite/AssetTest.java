import org.junit.jupiter.api.*;
import javax.validation.*;
import java.time.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Set;

public class AssetTest {
    private Validator validator;
    private Asset asset;
    private Employee employee;

    @BeforeEach
    public void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        employee = new Employee();
        employee.setId(1L);
        asset = new Asset();
        asset.setType("SCANNER");
        asset.setSerialNumber("SN123456");
        asset.setStatus("AVAILABLE");
        asset.setCondition("Good");
        asset.setAssignedTo(employee);
        asset.setCheckoutDate(LocalDateTime.now().minusDays(1));
        asset.setReturnDate(LocalDateTime.now());
    }

    @AfterEach
    public void tearDown() {
        validator = null;
        asset = null;
        employee = null;
    }

    @Test
    public void testValidAsset_ShouldPassValidation() {
        Set<ConstraintViolation<Asset>> violations = validator.validate(asset);
        assertTrue(violations.isEmpty());
    }

    @Test
    public void testNullType_ShouldFailValidation() {
        asset.setType(null);
        Set<ConstraintViolation<Asset>> violations = validator.validate(asset);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testInvalidType_ShouldFailValidation() {
        asset.setType("LAPTOP");
        Set<ConstraintViolation<Asset>> violations = validator.validate(asset);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testNullSerialNumber_ShouldFailValidation() {
        asset.setSerialNumber(null);
        Set<ConstraintViolation<Asset>> violations = validator.validate(asset);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testEmptySerialNumber_ShouldFailValidation() {
        asset.setSerialNumber("");
        Set<ConstraintViolation<Asset>> violations = validator.validate(asset);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testDuplicateSerialNumber_ShouldFailBusinessLogic() {
        Asset asset2 = new Asset();
        asset2.setSerialNumber(asset.getSerialNumber());
        assertFalse(asset.isSerialNumberUnique(asset2));
    }

    @Test
    public void testNullStatus_ShouldFailValidation() {
        asset.setStatus(null);
        Set<ConstraintViolation<Asset>> violations = validator.validate(asset);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testInvalidStatus_ShouldFailValidation() {
        asset.setStatus("LOST");
        Set<ConstraintViolation<Asset>> violations = validator.validate(asset);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testNullCondition_ShouldFailValidation() {
        asset.setCondition(null);
        Set<ConstraintViolation<Asset>> violations = validator.validate(asset);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testAssignmentWithoutEmployee_ShouldFailValidation() {
        asset.setAssignedTo(null);
        Set<ConstraintViolation<Asset>> violations = validator.validate(asset);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testCheckoutWithoutDate_ShouldFailValidation() {
        asset.setCheckoutDate(null);
        Set<ConstraintViolation<Asset>> violations = validator.validate(asset);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testReturnBeforeCheckout_ShouldFailBusinessLogic() {
        asset.setCheckoutDate(LocalDateTime.now());
        asset.setReturnDate(LocalDateTime.now().minusDays(1));
        assertFalse(asset.isReturnAfterCheckout());
    }

    @Test
    public void testAssetAvailabilityCheck_ShouldBeAvailable() {
        asset.setStatus("AVAILABLE");
        assertTrue(asset.isAvailable());
    }
}
