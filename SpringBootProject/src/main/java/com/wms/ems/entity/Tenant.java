package com.wms.ems.entity;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity representing a tenant in the multi-tenant EMS system.
 */
@Entity
@Table(name = "tenant")
public class Tenant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String tenantId;

    @Column(nullable = false, length = 128)
    private String name;

    @Column(length = 16)
    private String locale;

    @Column(length = 64)
    private String timezone;

    @Column(columnDefinition = "TEXT")
    private String configuration;

    @Column(nullable = false)
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdDate;

    @UpdateTimestamp
    private LocalDateTime lastModifiedDate;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getLocale() { return locale; }
    public void setLocale(String locale) { this.locale = locale; }
    public String getTimezone() { return timezone; }
    public void setTimezone(String timezone) { this.timezone = timezone; }
    public String getConfiguration() { return configuration; }
    public void setConfiguration(String configuration) { this.configuration = configuration; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
    public LocalDateTime getLastModifiedDate() { return lastModifiedDate; }
    public void setLastModifiedDate(LocalDateTime lastModifiedDate) { this.lastModifiedDate = lastModifiedDate; }
}
