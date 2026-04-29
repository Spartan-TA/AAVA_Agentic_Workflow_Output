package com.example.ems.service;

import com.example.ems.entity.Notification;
import com.example.ems.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    public List<Notification> getAllNotifications() {
        return notificationRepository.findAll();
    }

    public Optional<Notification> getNotificationById(Long id) {
        return notificationRepository.findById(id);
    }

    public Notification createNotification(Notification notification) {
        return notificationRepository.save(notification);
    }

    public Notification updateNotification(Long id, Notification updatedNotification) {
        return notificationRepository.findById(id)
                .map(existing -> {
                    existing.setType(updatedNotification.getType());
                    existing.setChannel(updatedNotification.getChannel());
                    existing.setRecipient(updatedNotification.getRecipient());
                    existing.setSubject(updatedNotification.getSubject());
                    existing.setMessage(updatedNotification.getMessage());
                    existing.setSentAt(updatedNotification.getSentAt());
                    existing.setStatus(updatedNotification.getStatus());
                    existing.setDeliveryStatus(updatedNotification.getDeliveryStatus());
                    existing.setLocale(updatedNotification.getLocale());
                    return notificationRepository.save(existing);
                })
                .orElseThrow(() -> new RuntimeException("Notification not found"));
    }

    public void deleteNotification(Long id) {
        notificationRepository.deleteById(id);
    }
}
