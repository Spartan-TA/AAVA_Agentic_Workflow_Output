package com.wms.ems.integration;

import lombok.Data;

@Data
public class WebhookEventDto {
    private String eventType;
    private String payload;
    // Additional fields as needed
}
