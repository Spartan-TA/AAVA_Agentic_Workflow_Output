package com.example.app.service.impl;

import com.example.app.dto.NotificationDto;
import com.example.app.entity.Notification;
import com.example.app.entity.User;
import com.example.app.repository.NotificationRepository;
import com.example.app.repository.UserRepository;
import com.example.app.service.NotificationService;
import com.example.app.exception.RegistrationException;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final JavaMailSender mailSender;

    @Override
    public List<NotificationDto> getUserNotifications(String username) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new RegistrationException("User not found"));
        List<Notification> notifications = notificationRepository.findByUser(user);
        return notifications.stream().map(NotificationDto::fromEntity).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId).orElseThrow(() -> new RegistrationException("Notification not found"));
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Override
    public void sendNotification(String username, String message) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new RegistrationException("User not found"));
        Notification notification = Notification.builder()
                .user(user)
                .message(message)
                .read(false)
                .createdAt(LocalDateTime.now())
                .build();
        notificationRepository.save(notification);
        // Optionally send email
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(user.getEmail());
        mailMessage.setSubject("New Notification");
        mailMessage.setText(message);
        mailSender.send(mailMessage);
    }
}
