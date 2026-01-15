package com.warehouse.integration.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;

@Service
public class HrisIntegrationService {
    @Value("${hris.api.url}")
    private String hrisApiUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public String syncEmployeeData(Long employeeId) {
        // Simulate HRIS sync
        String url = hrisApiUrl + "/employees/" + employeeId;
        // In real implementation, use restTemplate.getForObject(url, String.class);
        return "HRIS sync completed for employee " + employeeId;
    }
}
