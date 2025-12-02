package com.wms.ems.notification;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String channel; // IN_APP, EMAIL, SMS

    @Column(nullable = false)
    private String message;

    @Column(nullable = false)
    private String status; // Sent, Failed

    @Column(nullable = false)
    private LocalDateTime timestamp;

    // Getters and setters omitted for brevity
}
