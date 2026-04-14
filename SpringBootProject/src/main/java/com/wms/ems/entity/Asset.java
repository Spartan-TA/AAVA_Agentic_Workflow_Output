package com.wms.ems.entity;

import com.wms.ems.entity.enums.AssetType;
import com.wms.ems.entity.enums.AssetCondition;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity representing an asset in the warehouse.
 */
@Entity
@Table(name = "asset")
public class Asset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssetType assetType;

    @Column(nullable = false, unique = true, length = 64)
    private String serialNumber;

    @Column(length = 255)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssetCondition condition;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "certification_required_id")
    private Certification certificationRequired;

    @Column(nullable = false)
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdDate;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public AssetType getAssetType() { return assetType; }
    public void setAssetType(AssetType assetType) { this.assetType = assetType; }
    public String getSerialNumber() { return serialNumber; }
    public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public AssetCondition getCondition() { return condition; }
    public void setCondition(AssetCondition condition) { this.condition = condition; }
    public Certification getCertificationRequired() { return certificationRequired; }
    public void setCertificationRequired(Certification certificationRequired) { this.certificationRequired = certificationRequired; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
}
