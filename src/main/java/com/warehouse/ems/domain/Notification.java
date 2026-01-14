package com.warehouse.ems.domain;

import com.warehouse.ems.enums.NotificationType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Entity representing a notification sent to an employee.
 */
@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @NotNull
    @Enumerated(EnumType.STRING)
    private NotificationType type;

    @NotNull
    private String message;

    @NotNull
    private LocalDateTime sentAt;

    private Boolean delivered;
}
