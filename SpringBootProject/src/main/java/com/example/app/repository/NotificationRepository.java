package com.example.app.repository;

import com.example.app.entity.Notification;
import com.example.app.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUser(User user);
    List<Notification> findByUserAndReadFalse(User user);
}
