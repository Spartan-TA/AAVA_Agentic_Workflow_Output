package com.warehouseems.notification;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "recipient_id")
    private com.warehouseems.employee.Employee recipient;

    @Column(nullable = false)
    private String channel;

    private String template;
    private String content;
    private LocalDateTime sentAt;
    private Boolean delivered;
}
