package com.example.warehouse.integration;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/webhook")
public class WebhookController {
    @PostMapping("/event")
    public ResponseEntity<String> receiveEvent(@RequestBody String payload) {
        // TODO: Implement webhook event handling logic
        return ResponseEntity.ok("Webhook event received");
    }
}
