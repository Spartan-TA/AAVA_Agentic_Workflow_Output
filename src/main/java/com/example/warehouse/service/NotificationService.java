package com.example.warehouse.service;

import com.example.warehouse.dto.NotificationDTO;
import com.example.warehouse.entity.Notification;
import com.example.warehouse.entity.Employee;
import com.example.warehouse.repository.NotificationRepository;
import com.example.warehouse.repository.EmployeeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final EmployeeRepository employeeRepository;

    public NotificationService(NotificationRepository notificationRepository, EmployeeRepository employeeRepository) {
        this.notificationRepository = notificationRepository;
        this.employeeRepository = employeeRepository;
    }

    @Transactional
    public Notification sendNotification(Long employeeId, NotificationDTO dto) {
        Employee employee = employeeRepository.findById(employeeId).orElseThrow();
        // Check opt-in/out, quiet hours, rate limiting, etc.
        Notification notification = new Notification();
        notification.setEmployee(employee);
        notification.setChannel(dto.getChannel());
        notification.setTemplate(dto.getTemplate());
        notification.setContent(dto.getContent());
        notification.setSentAt(LocalDateTime.now());
        notification.setStatus("SENT");
        notificationRepository.save(notification);
        // Delivery tracking logic
        return notification;
    }

    public List<Notification> getNotifications(Long employeeId) {
        return notificationRepository.findByEmployeeId(employeeId);
    }

    @Transactional
    public void updatePreferences(Long employeeId, boolean optIn) {
        Employee employee = employeeRepository.findById(employeeId).orElseThrow();
        employee.setNotificationOptIn(optIn);
        employeeRepository.save(employee);
    }
}
