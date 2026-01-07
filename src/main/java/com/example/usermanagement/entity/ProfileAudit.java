package com.example.usermanagement.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity for auditing profile changes.
 */
@Entity
@Table(name = "profile_audits")
public class ProfileAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String fieldChanged;

    @Column(nullable = false)
    private String oldValue;

    @Column(nullable = false)
    private String newValue;

    @Column(nullable = false)
    private LocalDateTime changedAt;

    public ProfileAudit() {}
    public ProfileAudit(User user, String fieldChanged, String oldValue, String newValue, LocalDateTime changedAt) {
        this.user = user;
        this.fieldChanged = fieldChanged;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.changedAt = changedAt;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getFieldChanged() { return fieldChanged; }
    public void setFieldChanged(String fieldChanged) { this.fieldChanged = fieldChanged; }
    public String getOldValue() { return oldValue; }
    public void setOldValue(String oldValue) { this.oldValue = oldValue; }
    public String getNewValue() { return newValue; }
    public void setNewValue(String newValue) { this.newValue = newValue; }
    public LocalDateTime getChangedAt() { return changedAt; }
    public void setChangedAt(LocalDateTime changedAt) { this.changedAt = changedAt; }
}
