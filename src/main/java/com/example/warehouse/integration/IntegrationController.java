package com.example.warehouse.integration;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/integration")
public class IntegrationController {
    @PostMapping("/import-employees")
    public ResponseEntity<String> importEmployees(@RequestBody String csvData) {
        // TODO: Implement CSV import logic
        return ResponseEntity.ok("Import successful");
    }

    @GetMapping("/status")
    public ResponseEntity<String> getStatus() {
        return ResponseEntity.ok("Integration service running");
    }
}
