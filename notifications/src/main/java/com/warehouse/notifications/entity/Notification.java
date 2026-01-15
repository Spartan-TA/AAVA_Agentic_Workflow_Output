package com.warehouse.notifications.entity;

import lombok.*;
import javax.persistence.*;
import javax.validation.constraints.*;
import java.time.LocalDateTime;

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

    @NotBlank
    private String channel;

    @NotBlank
    private String template;

    @NotBlank
    private String recipient;

    @NotBlank
    private String content;

    @NotNull
    private LocalDateTime sentAt;

    @NotBlank
    private String status;
}
