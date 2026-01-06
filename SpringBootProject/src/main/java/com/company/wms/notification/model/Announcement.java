package com.company.wms.notification.model;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity representing a company-wide announcement.
 */
@Entity
@Table(name = "announcements")
public class Announcement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Title of the announcement.
     */
    @Column(nullable = false)
    private String title;

    /**
     * Content of the announcement.
     */
    @Column(nullable = false, length = 2000)
    private String content;

    /**
     * Date and time the announcement was published.
     */
    @Column(name = "published_at", nullable = false)
    private LocalDateTime publishedAt;

    /**
     * The employee who created the announcement.
     */
    @Column(name = "created_by", nullable = false)
    private Long createdByEmployeeId;

    // Constructors, getters, setters, equals, hashCode, toString

    public Announcement() {}

    public Announcement(String title, String content, LocalDateTime publishedAt, Long createdByEmployeeId) {
        this.title = title;
        this.content = content;
        this.publishedAt = publishedAt;
        this.createdByEmployeeId = createdByEmployeeId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }

    public Long getCreatedByEmployeeId() {
        return createdByEmployeeId;
    }

    public void setCreatedByEmployeeId(Long createdByEmployeeId) {
        this.createdByEmployeeId = createdByEmployeeId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Announcement that = (Announcement) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public String toString() {
        return "Announcement{" +
                "id=" + id +
                ", title='" + title + ''' +
                ", content='" + content + ''' +
                ", publishedAt=" + publishedAt +
                ", createdByEmployeeId=" + createdByEmployeeId +
                '}';
    }
}
