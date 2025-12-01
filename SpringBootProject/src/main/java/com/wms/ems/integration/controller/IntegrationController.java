package com.wms.ems.integration.controller;

import com.wms.ems.integration.dto.HrisSyncDto;
import com.wms.ems.integration.dto.WebhookDto;
import com.wms.ems.integration.service.IntegrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/integrations")
@RequiredArgsConstructor
@Tag(name = "Integrations", description = "Endpoints for HRIS and webhook integrations")
public class IntegrationController {
    private final IntegrationService integrationService;

    @Operation(summary = "Sync with HRIS system")
    @PostMapping("/hris/sync")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> syncHris(@Valid @RequestBody HrisSyncDto dto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
        }
        integrationService.syncHris(dto);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Register webhook integration")
    @PostMapping("/webhooks")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> registerWebhook(@Valid @RequestBody WebhookDto dto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
        }
        integrationService.registerWebhook(dto);
        return ResponseEntity.ok().build();
    }
}
