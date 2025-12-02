package com.wms.ems.integration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/webhooks")
public class WebhookController {

    @Autowired
    private IntegrationService integrationService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','HR','SUPERVISOR')")
    public ResponseEntity<String> handleWebhook(@RequestBody WebhookEventDto event, @RequestHeader("X-Signature") String signature) {
        boolean valid = integrationService.handleWebhook(event, signature);
        return valid ? ResponseEntity.ok("Webhook processed") : ResponseEntity.status(400).body("Invalid signature");
    }
}
