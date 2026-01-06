package com.company.wms.notification.model;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity representing a notification sent to an employee.
 */
@Entity
@Table(name = "notifications")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The employee who receives the notification.
     */
    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    /**
     * Notification message content.
     */
    @Column(nullable = false, length = 1000)
    private String message;

    /**
     * Date and time the notification was sent.
     */
    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;

    /**
     * Whether the notification has been read.
     */
    @Column(name = "is_read", nullable = false)
    private boolean read;

    // Constructors, getters, setters, equals, hashCode, toString

    public Notification() {}

    public Notification(Long employeeId, String message, LocalDateTime sentAt, boolean read) {
        this.employeeId = employeeId;
        this.message = message;
        this.sentAt = sentAt;
        this.read = read;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Notification that = (Notification) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public String toString() {
        return "Notification{" +
                "id=" + id +
                ", employeeId=" + employeeId +
                ", message='" + message + ''' +
                ", sentAt=" + sentAt +
                ", read=" + read +
                '}';
    }
}
