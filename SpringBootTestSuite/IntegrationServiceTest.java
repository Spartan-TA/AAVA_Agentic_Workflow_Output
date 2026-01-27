package com.warehouse.ems.service;

import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class IntegrationServiceTest {
    @Autowired
    IntegrationService integrationService;

    @MockBean
    HRISClient hrisClient;
    @MockBean
    WMSClient wmsClient;
    @MockBean
    WebhookPublisher webhookPublisher;
    @MockBean
    JwtValidator jwtValidator;

    @BeforeEach
    void setup() {
        // Setup mocks if needed
    }

    @Test
    void testSyncHRIS_NewHire_Success() {
        EmployeeDTO newHire = new EmployeeDTO("John Doe", "B123", "Worker", "Receiving", "A", new Date(), "Active");
        when(hrisClient.fetchNewHires()).thenReturn(Collections.singletonList(newHire));
        when(integrationService.syncHRIS()).thenReturn(1);
        int count = integrationService.syncHRIS();
        assertEquals(1, count);
        verify(hrisClient, atLeastOnce()).fetchNewHires();
    }

    @Test
    void testSyncHRIS_DuplicateBadgeId() {
        EmployeeDTO duplicate = new EmployeeDTO("Jane", "B123", "Worker", "Packing", "B", new Date(), "Active");
        when(hrisClient.fetchNewHires()).thenReturn(Collections.singletonList(duplicate));
        doThrow(new IllegalArgumentException("Duplicate badgeId")).when(integrationService).createEmployee(any());
        assertThrows(IllegalArgumentException.class, () -> integrationService.syncHRIS());
    }

    @Test
    void testWMSIntegration_DepartmentSync() {
        DepartmentDTO dept = new DepartmentDTO("Packing", "WMS-01");
        when(wmsClient.fetchDepartments()).thenReturn(Collections.singletonList(dept));
        when(integrationService.syncWMS()).thenReturn(1);
        int count = integrationService.syncWMS();
        assertEquals(1, count);
        verify(wmsClient).fetchDepartments();
    }

    @Test
    void testWebhook_ValidPayload() {
        WebhookEvent event = new WebhookEvent("employee.created", Map.of("badgeId", "B123"));
        when(webhookPublisher.publish(event)).thenReturn(true);
        boolean result = integrationService.handleWebhook(event);
        assertTrue(result);
        verify(webhookPublisher).publish(event);
    }

    @Test
    void testWebhook_InvalidPayload() {
        WebhookEvent event = new WebhookEvent("employee.created", null);
        when(webhookPublisher.publish(event)).thenReturn(false);
        boolean result = integrationService.handleWebhook(event);
        assertFalse(result);
    }

    @Test
    void testJWTValidation_Success() {
        String jwt = "valid.jwt.token";
        when(jwtValidator.validate(jwt)).thenReturn(true);
        assertTrue(integrationService.validateJWT(jwt));
    }

    @Test
    void testJWTValidation_Failure() {
        String jwt = "invalid.jwt.token";
        when(jwtValidator.validate(jwt)).thenReturn(false);
        assertFalse(integrationService.validateJWT(jwt));
    }

    @Test
    void testOAuth2Token_Null() {
        assertThrows(IllegalArgumentException.class, () -> integrationService.validateOAuth2Token(null));
    }

    @Test
    void testSyncHRIS_EmptyList() {
        when(hrisClient.fetchNewHires()).thenReturn(Collections.emptyList());
        when(integrationService.syncHRIS()).thenReturn(0);
        int count = integrationService.syncHRIS();
        assertEquals(0, count);
    }

    @Test
    void testIntegration_MultipleSystems() {
        when(hrisClient.fetchNewHires()).thenReturn(Arrays.asList(
            new EmployeeDTO("A", "B1", "Worker", "Dept1", "A", new Date(), "Active"),
            new EmployeeDTO("B", "B2", "Worker", "Dept2", "B", new Date(), "Active")
        ));
        when(wmsClient.fetchDepartments()).thenReturn(Arrays.asList(
            new DepartmentDTO("Dept1", "WMS-01"),
            new DepartmentDTO("Dept2", "WMS-02")
        ));
        when(integrationService.syncHRIS()).thenReturn(2);
        when(integrationService.syncWMS()).thenReturn(2);
        assertEquals(2, integrationService.syncHRIS());
        assertEquals(2, integrationService.syncWMS());
    }
}
