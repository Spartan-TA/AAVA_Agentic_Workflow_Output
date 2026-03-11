package com.example.app.entity;

import lombok.*;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "account_activities")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountActivity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String activityType;

    private String description;

    private LocalDateTime activityTime;
}
