package com.example.warehouse.service;

import com.example.warehouse.dto.NotificationDTO;
import com.example.warehouse.entity.Employee;
import com.example.warehouse.entity.Notification;
import com.example.warehouse.exception.ResourceNotFoundException;
import com.example.warehouse.exception.ValidationException;
import com.example.warehouse.repository.EmployeeRepository;
import com.example.warehouse.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing notifications.
 */
@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final EmployeeRepository employeeRepository;

    @Autowired
    public NotificationService(NotificationRepository notificationRepository, EmployeeRepository employeeRepository) {
        this.notificationRepository = notificationRepository;
        this.employeeRepository = employeeRepository;
    }

    /**
     * Get all notifications for an employee.
     * @param employeeId Employee ID
     * @return List of NotificationDTO
     */
    @Transactional(readOnly = true)
    public List<NotificationDTO> getNotificationsByEmployee(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));
        return notificationRepository.findByEmployee(employee).stream()
                .map(NotificationDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Send a notification to an employee.
     * @param employeeId Employee ID
     * @param dto NotificationDTO
     * @return NotificationDTO
     */
    @Transactional
    public NotificationDTO sendNotification(Long employeeId, NotificationDTO dto) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));
        if (dto.getMessage() == null || dto.getMessage().isEmpty()) {
            throw new ValidationException("Notification message is required");
        }
        Notification notification = new Notification();
        notification.setEmployee(employee);
        notification.setMessage(dto.getMessage());
        notification.setType(dto.getType());
        notification.setSentAt(dto.getSentAt() != null ? dto.getSentAt() : LocalDateTime.now());
        notificationRepository.save(notification);
        return NotificationDTO.fromEntity(notification);
    }

    /**
     * Get all notifications.
     * @return List of NotificationDTO
     */
    @Transactional(readOnly = true)
    public List<NotificationDTO> getAllNotifications() {
        return notificationRepository.findAll().stream()
                .map(NotificationDTO::fromEntity)
                .collect(Collectors.toList());
    }
}
