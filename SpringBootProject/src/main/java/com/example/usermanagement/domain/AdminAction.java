package com.example.usermanagement.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "admin_actions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminAction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", nullable = false)
    private User admin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_user_id", nullable = false)
    private User targetUser;

    @Column(nullable = false)
    private ActionType actionType;

    @Column(nullable = false)
    private LocalDateTime actionTime;

    public enum ActionType {
        ACTIVATE,
        DEACTIVATE
    }
}
