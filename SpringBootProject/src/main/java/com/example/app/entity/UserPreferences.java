package com.example.app.entity;

import lombok.*;
import javax.persistence.*;

@Entity
@Table(name = "user_preferences")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPreferences {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private boolean emailNotificationsEnabled;
    private boolean smsNotificationsEnabled;
    private String theme;
}
