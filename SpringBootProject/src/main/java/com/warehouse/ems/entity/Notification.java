package com.warehouse.ems.entity;

import lombok.Data;
import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

/**
 * Entity representing a notification sent to an employee.
 */
@Entity
@Table(name = "notification")
@Data
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @NotBlank
    @Column(name = "message", nullable = false)
    private String message;

    @NotBlank
    @Column(name = "type", nullable = false)
    private String type;

    @NotBlank
    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
