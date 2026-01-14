package com.warehouse.ems.integration;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduledSyncJob {
    @Scheduled(cron = "0 0 * * * *")
    public void syncWithExternalSystems() {
        // Logic to sync with HRIS/WMS
    }
}