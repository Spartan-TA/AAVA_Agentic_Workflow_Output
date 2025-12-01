package com.example.ems.controller;

import com.example.ems.dto.IntegrationDto;
import com.example.ems.service.IntegrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/integrations")
@Validated
public class IntegrationController {
    private final IntegrationService integrationService;

    @Autowired
    public IntegrationController(IntegrationService integrationService) {
        this.integrationService = integrationService;
    }

    @GetMapping
    public ResponseEntity<List<IntegrationDto>> getAllIntegrations() {
        return ResponseEntity.ok(integrationService.getAllIntegrations());
    }

    @GetMapping("/{id}")
    public ResponseEntity<IntegrationDto> getIntegrationById(@PathVariable Long id) {
        return ResponseEntity.ok(integrationService.getIntegrationById(id));
    }

    @PostMapping
    public ResponseEntity<IntegrationDto> createIntegration(@Valid @RequestBody IntegrationDto integrationDto) {
        return ResponseEntity.ok(integrationService.createIntegration(integrationDto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<IntegrationDto> updateIntegration(@PathVariable Long id, @Valid @RequestBody IntegrationDto integrationDto) {
        return ResponseEntity.ok(integrationService.updateIntegration(id, integrationDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIntegration(@PathVariable Long id) {
        integrationService.deleteIntegration(id);
        return ResponseEntity.noContent().build();
    }
}