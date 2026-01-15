package com.warehouse.integration.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;

@Service
public class WmsIntegrationService {
    @Value("${wms.api.url}")
    private String wmsApiUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public String syncInventoryData(Long assetId) {
        // Simulate WMS sync
        String url = wmsApiUrl + "/assets/" + assetId;
        // In real implementation, use restTemplate.getForObject(url, String.class);
        return "WMS sync completed for asset " + assetId;
    }
}
