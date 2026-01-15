package com.warehouse.integration.service;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class WebhookService {
    private final RestTemplate restTemplate = new RestTemplate();

    public String sendWebhook(String url, String payload) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(payload, headers);
        restTemplate.postForEntity(url, entity, String.class);
        return "Webhook sent to " + url;
    }
}
