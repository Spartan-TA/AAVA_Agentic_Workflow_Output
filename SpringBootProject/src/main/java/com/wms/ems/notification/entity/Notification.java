package com.wms.ems.notification.entity;

import lombok.*;
import javax.persistence.*;
import java.time.LocalDateTime;
import com.wms.ems.employee.entity.Employee;

/**
 * Notification entity representing notifications sent to employees (email, SMS, push).
 */
@Entity
@Table(name = "notification")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @Column(name = "type", nullable = false, length = 32)
    private String type; // EMAIL, SMS, PUSH

    @Column(name = "subject", length = 128)
    private String subject;

    @Column(name = "message", nullable = false)
    private String message;

    @Column(name = "status", nullable = false, length = 32)
    private String status;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}