package com.warehouse.employee.management.repository;

import com.warehouse.employee.management.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;
import java.util.List;

/**
 * Repository for Notification entity.
 * Supports CRUD, pagination, sorting, and soft-delete queries.
 */
public interface NotificationRepository extends JpaRepository<Notification, Long>, JpaSpecificationExecutor<Notification> {
    @Query("SELECT n FROM Notification n WHERE n.deletedAt IS NULL")
    List<Notification> findAllActive();

    @Query("SELECT n FROM Notification n WHERE n.deletedAt IS NULL")
    Page<Notification> findAllActive(Pageable pageable);

    @Query("SELECT n FROM Notification n WHERE n.id = :id AND n.deletedAt IS NULL")
    Optional<Notification> findActiveById(Long id);

    // Custom query example: Find by recipientId and status
    @Query("SELECT n FROM Notification n WHERE n.recipient.id = :recipientId AND n.status = :status AND n.deletedAt IS NULL")
    List<Notification> findActiveByRecipientIdAndStatus(Long recipientId, String status);
}
