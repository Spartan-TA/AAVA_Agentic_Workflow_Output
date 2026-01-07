package com.example.usermanagement.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity for tracking email notifications.
 */
@Entity
@Table(name = "email_notifications")
public class EmailNotification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String subject;

    @Column(nullable = false, length = 2048)
    private String content;

    @Column(nullable = false)
    private LocalDateTime sentAt;

    public EmailNotification() {}
    public EmailNotification(User user, String subject, String content, LocalDateTime sentAt) {
        this.user = user;
        this.subject = subject;
        this.content = content;
        this.sentAt = sentAt;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }
}
