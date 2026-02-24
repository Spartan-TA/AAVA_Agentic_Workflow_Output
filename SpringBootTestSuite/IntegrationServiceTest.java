package SpringBootTestSuite;

import org.junit.jupiter.api.*;
import org.mockito.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import java.time.*;

class IntegrationServiceTest {

    @Mock
    private HrisClient hrisClient;
    @Mock
    private WmsClient wmsClient;
    @Mock
    private WebhookValidator webhookValidator;
    @InjectMocks
    private IntegrationService integrationService;

    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        mocks.close();
    }

    @Test
    void testSyncHrisData_Valid() {
        when(hrisClient.sync(any())).thenReturn(true);
        boolean result = integrationService.syncHrisData("EMPLOYEE_UPDATE");
        assertTrue(result);
    }

    @Test
    void testSyncHrisData_InvalidEventType() {
        Exception ex = assertThrows(IllegalArgumentException.class, () ->
            integrationService.syncHrisData("UNKNOWN_EVENT"));
        assertEquals("Invalid event type", ex.getMessage());
    }

    @Test
    void testProcessWmsUpdate_MissingFields() {
        Map<String, Object> data = new HashMap<>();
        Exception ex = assertThrows(IllegalArgumentException.class, () ->
            integrationService.processWmsUpdate(data));
        assertEquals("Missing required fields", ex.getMessage());
    }

    @Test
    void testHandleWebhook_InvalidSignature() {
        when(webhookValidator.isValidSignature(any(), any())).thenReturn(false);
        Exception ex = assertThrows(SecurityException.class, () ->
            integrationService.handleWebhook("payload", "bad-signature"));
        assertEquals("Invalid webhook signature", ex.getMessage());
    }

    @Test
    void testValidateIdempotency_DuplicateRequestId() {
        when(hrisClient.isRequestProcessed("req-123")).thenReturn(true);
        Exception ex = assertThrows(IllegalStateException.class, () ->
            integrationService.validateIdempotency("req-123"));
        assertEquals("Duplicate request ID", ex.getMessage());
    }

    @Test
    void testHandleWebhook_NullPayload() {
        Exception ex = assertThrows(IllegalArgumentException.class, () ->
            integrationService.handleWebhook(null, "signature"));
        assertEquals("Payload cannot be null", ex.getMessage());
    }
}