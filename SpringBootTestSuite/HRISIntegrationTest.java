import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class HRISIntegrationTest {
    private HRISIntegration integration;

    @BeforeEach
    public void setUp() {
        integration = new HRISIntegration();
    }

    @Test
    public void testSyncEmployeeData_Valid() {
        Employee emp = new Employee("emp1", "John Doe");
        assertDoesNotThrow(() -> integration.syncEmployeeData(emp));
    }

    @Test
    public void testSyncEmployeeData_APIFailure() {
        Employee emp = new Employee("emp2", "Jane Doe");
        integration.setApiAvailable(false);
        assertThrows(ApiException.class, () -> integration.syncEmployeeData(emp));
    }

    @Test
    public void testFetchFromHRIS_Valid() {
        assertNotNull(integration.fetchFromHRIS("emp3"));
    }

    @Test
    public void testFetchFromHRIS_InvalidCredentials() {
        integration.setApiCredentials("invalid", "invalid");
        assertThrows(AuthenticationException.class, () -> integration.fetchFromHRIS("emp4"));
    }

    @Test
    public void testPushToHRIS_Valid() {
        Employee emp = new Employee("emp5", "Alice Smith");
        assertDoesNotThrow(() -> integration.pushToHRIS(emp));
    }

    @Test
    public void testPushToHRIS_NullPayload() {
        assertThrows(IllegalArgumentException.class, () -> integration.pushToHRIS(null));
    }

    @Test
    public void testHandleWebhook_Valid() {
        WebhookPayload payload = new WebhookPayload("emp6", "update");
        assertDoesNotThrow(() -> integration.handleWebhook(payload));
    }

    @Test
    public void testHandleWebhook_NullPayload() {
        assertThrows(IllegalArgumentException.class, () -> integration.handleWebhook(null));
    }

    @Test
    public void testValidateAPICredentials_Valid() {
        integration.setApiCredentials("user", "pass");
        assertTrue(integration.validateAPICredentials());
    }

    @Test
    public void testValidateAPICredentials_Invalid() {
        integration.setApiCredentials("", "");
        assertFalse(integration.validateAPICredentials());
    }

    @Test
    public void testSyncEmployeeData_RetryLogic() {
        Employee emp = new Employee("emp7", "Bob Brown");
        integration.setApiAvailable(false);
        integration.setMaxRetries(2);
        assertThrows(ApiException.class, () -> integration.syncEmployeeData(emp));
    }
}