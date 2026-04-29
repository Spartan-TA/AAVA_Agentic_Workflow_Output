package com.example.ems.service;

import com.example.ems.entity.WebhookEvent;
import com.example.ems.repository.WebhookEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class WebhookEventService {

    @Autowired
    private WebhookEventRepository webhookEventRepository;

    public List<WebhookEvent> getAllEvents() {
        return webhookEventRepository.findAll();
    }

    public Optional<WebhookEvent> getEventById(Long id) {
        return webhookEventRepository.findById(id);
    }

    public WebhookEvent createEvent(WebhookEvent event) {
        return webhookEventRepository.save(event);
    }

    public WebhookEvent updateEvent(Long id, WebhookEvent updatedEvent) {
        return webhookEventRepository.findById(id)
                .map(existing -> {
                    existing.setEventType(updatedEvent.getEventType());
                    existing.setPayload(updatedEvent.getPayload());
                    existing.setTriggeredAt(updatedEvent.getTriggeredAt());
                    existing.setStatus(updatedEvent.getStatus());
                    existing.setTargetUrl(updatedEvent.getTargetUrl());
                    existing.setDeliveryMethod(updatedEvent.getDeliveryMethod());
                    existing.setErrorMessage(updatedEvent.getErrorMessage());
                    return webhookEventRepository.save(existing);
                })
                .orElseThrow(() -> new RuntimeException("WebhookEvent not found"));
    }

    public void deleteEvent(Long id) {
        webhookEventRepository.deleteById(id);
    }
}
