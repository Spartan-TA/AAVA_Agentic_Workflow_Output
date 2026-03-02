package com.example.usermanagement.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "backup_codes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BackupCode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String code;

    @Column(nullable = false)
    private boolean used = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
