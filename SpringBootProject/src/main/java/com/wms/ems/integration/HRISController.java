package com.wms.ems.integration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/hris")
public class HRISController {

    @Autowired
    private IntegrationService integrationService;

    @PostMapping("/sync")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public ResponseEntity<String> syncHRIS(@RequestBody HRISSyncDto dto) {
        integrationService.syncHRIS(dto);
        return ResponseEntity.ok("HRIS sync completed");
    }
}
