package com.example.warehouse.repository;

import com.example.warehouse.entity.Notification;
import com.example.warehouse.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByRecipientAndRead(Employee recipient, boolean read);
}
