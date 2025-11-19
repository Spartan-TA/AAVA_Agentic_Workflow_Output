package com.example.integration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/integration")
public class IntegrationController {
    @Autowired
    private HrisIntegrationService hrisIntegrationService;
    @Autowired
    private WmsIntegrationService wmsIntegrationService;

    @GetMapping("/hris/sync")
    public String syncHris() {
        return hrisIntegrationService.sync();
    }

    @GetMapping("/wms/sync")
    public String syncWms() {
        return wmsIntegrationService.sync();
    }
}