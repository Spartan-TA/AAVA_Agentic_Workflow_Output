package com.wms.integration.controllers;

import com.wms.integration.dtos.EmployeeEventDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for receiving employee event webhooks.
 */
@RestController
@RequestMapping("/api/webhooks")
public class WebhookController {

    /**
     * Receives employee event webhook.
     * @param eventDto EmployeeEventDto
     * @return ResponseEntity
     */
    @PostMapping("/employee")
    public ResponseEntity<Void> receiveEmployeeEvent(@RequestBody EmployeeEventDto eventDto) {
        // TODO: Process event (e.g., update employee status, trigger onboarding/offboarding)
        return ResponseEntity.ok().build();
    }
}
