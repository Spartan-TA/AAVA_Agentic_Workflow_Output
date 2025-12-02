package com.wms.ems.integration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wms")
public class WMSController {

    @Autowired
    private IntegrationService integrationService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public ResponseEntity<String> syncWMS(@RequestBody WMSSyncDto dto) {
        integrationService.syncWMS(dto);
        return ResponseEntity.ok("WMS sync completed");
    }
}
