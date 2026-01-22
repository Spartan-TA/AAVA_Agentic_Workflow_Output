package com.warehouse.ems.integration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for integration endpoints (secured with JWT/OAuth2).
 */
@RestController
@RequestMapping("/api")
@Validated
public class IntegrationController {
    private final IntegrationService integrationService;

    @Autowired
    public IntegrationController(IntegrationService integrationService) {
        this.integrationService = integrationService;
    }

    /**
     * HRIS employee sync endpoint.
     */
    @PostMapping("/hris/sync")
    public ResponseEntity<String> syncHris(@RequestBody Object hrisPayload) {
        try {
            integrationService.syncHrisEmployee(hrisPayload);
            return ResponseEntity.ok("HRIS sync successful");
        } catch (Exception e) {
            return new ResponseEntity<>("HRIS sync failed: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * WMS department/location mapping sync endpoint.
     */
    @PostMapping("/wms/sync")
    public ResponseEntity<String> syncWms(@RequestBody Object wmsPayload) {
        try {
            integrationService.syncWmsMapping(wmsPayload);
            return ResponseEntity.ok("WMS sync successful");
        } catch (Exception e) {
            return new ResponseEntity<>("WMS sync failed: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Webhook event publishing endpoint (secured).
     */
    @PostMapping("/webhooks")
    public ResponseEntity<String> publishWebhook(@RequestBody Object eventPayload) {
        try {
            integrationService.publishWebhook(eventPayload);
            return ResponseEntity.ok("Webhook published");
        } catch (Exception e) {
            return new ResponseEntity<>("Webhook publish failed: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
