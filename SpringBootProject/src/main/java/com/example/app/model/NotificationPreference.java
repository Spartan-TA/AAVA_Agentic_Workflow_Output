package com.example.app.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * NotificationPreference entity for managing user notification settings.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "notification_preferences")
public class NotificationPreference {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private boolean criticalOptIn = true;

    @Column(nullable = false)
    private boolean nonCriticalOptIn = false;
}
