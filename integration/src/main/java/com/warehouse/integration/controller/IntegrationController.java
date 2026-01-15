package com.warehouse.integration.controller;

import com.warehouse.integration.service.HrisIntegrationService;
import com.warehouse.integration.service.WmsIntegrationService;
import com.warehouse.integration.service.WebhookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/integration")
public class IntegrationController {
    @Autowired
    private HrisIntegrationService hrisIntegrationService;
    @Autowired
    private WmsIntegrationService wmsIntegrationService;
    @Autowired
    private WebhookService webhookService;

    @PostMapping("/hris/sync/{employeeId}")
    public ResponseEntity<String> syncEmployee(@PathVariable Long employeeId) {
        String result = hrisIntegrationService.syncEmployeeData(employeeId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/wms/sync/{assetId}")
    public ResponseEntity<String> syncAsset(@PathVariable Long assetId) {
        String result = wmsIntegrationService.syncInventoryData(assetId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> sendWebhook(@RequestParam String url, @RequestBody String payload) {
        String result = webhookService.sendWebhook(url, payload);
        return ResponseEntity.ok(result);
    }
}
