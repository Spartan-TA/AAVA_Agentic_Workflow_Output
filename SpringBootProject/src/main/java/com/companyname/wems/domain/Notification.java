package com.companyname.wems.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import jakarta.validation.constraints.*;

/**
 * Notification entity for sending notifications to users.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "notifications")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(nullable = false)
    private Long userId;

    @NotBlank
    @Size(max = 20)
    private String channel; // EMAIL, SMS, IN_APP

    @NotBlank
    @Size(max = 255)
    private String content;

    @NotBlank
    @Size(max = 20)
    private String status; // SENT, FAILED, QUEUED

    private LocalDateTime sentAt;

    @NotNull
    @Column(nullable = false)
    private Long tenantId;
}
