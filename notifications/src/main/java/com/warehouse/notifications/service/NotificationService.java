package com.warehouse.notifications.service;

import com.warehouse.notifications.entity.Notification;
import com.warehouse.notifications.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class NotificationService {
    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private EmailService emailService;
    @Autowired
    private SmsService smsService;

    private final LocalTime quietStart = LocalTime.of(22, 0);
    private final LocalTime quietEnd = LocalTime.of(6, 0);

    public List<Notification> getAllNotifications() {
        return notificationRepository.findAll();
    }

    public Optional<Notification> getNotificationById(Long id) {
        return notificationRepository.findById(id);
    }

    @Transactional
    public Notification sendNotification(String channel, String template, String recipient, String content) {
        LocalDateTime now = LocalDateTime.now();
        if (isQuietHours(now.toLocalTime())) {
            throw new IllegalStateException("Cannot send notifications during quiet hours");
        }
        Notification notification = Notification.builder()
                .channel(channel)
                .template(template)
                .recipient(recipient)
                .content(content)
                .sentAt(now)
                .status("SENT")
                .build();
        notificationRepository.save(notification);
        if (channel.equalsIgnoreCase("EMAIL")) {
            emailService.sendEmail(recipient, template, content);
        } else if (channel.equalsIgnoreCase("SMS")) {
            smsService.sendSms(recipient, content);
        }
        return notification;
    }

    public boolean isQuietHours(LocalTime time) {
        return time.isAfter(quietStart) || time.isBefore(quietEnd);
    }
}
