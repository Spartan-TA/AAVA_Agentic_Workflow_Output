@SpringBootTest
public class IntegrationServiceTest {
    @MockBean private HRISClient hrisClient;
    @MockBean private WMSClient wmsClient;
    @MockBean private WebhookHandler webhookHandler;
    @MockBean private SSOService ssoService;
    @Autowired private IntegrationService integrationService;

    @Test
    void testSyncFromHRIS_Success() {
        when(hrisClient.sync()).thenReturn(true);
        assertTrue(integrationService.syncFromHRIS());
    }

    @Test
    void testSyncToWMS_ValidInput_Success() {
        doNothing().when(wmsClient).pushEmployee(anyLong());
        assertDoesNotThrow(() -> integrationService.syncToWMS(1L));
    }

    @Test
    void testHandleWebhook_ValidInput_Success() {
        WebhookPayload payload = new WebhookPayload();
        when(webhookHandler.handle(payload)).thenReturn(true);
        assertTrue(integrationService.handleWebhook(payload));
    }

    @Test
    void testAuthenticateSSO_ValidToken_Success() {
        SSOToken token = new SSOToken("valid");
        when(ssoService.authenticate(token)).thenReturn(true);
        assertTrue(integrationService.authenticateSSO(token));
    }
}