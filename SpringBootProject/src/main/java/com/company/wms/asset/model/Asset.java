package com.company.wms.asset.model;

import javax.persistence.*;
import java.time.LocalDate;

/**
 * Entity representing a warehouse asset (e.g., forklift, scanner, pallet jack).
 */
@Entity
@Table(name = "assets")
public class Asset {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Asset name or description.
     */
    @Column(nullable = false)
    private String name;

    /**
     * Asset type (e.g., FORKLIFT, SCANNER).
     */
    @Column(nullable = false)
    private String type;

    /**
     * Unique asset code or serial number.
     */
    @Column(unique = true, nullable = false)
    private String assetCode;

    /**
     * Date the asset was acquired.
     */
    @Column(name = "acquired_date")
    private LocalDate acquiredDate;

    /**
     * Current status of the asset (e.g., AVAILABLE, ASSIGNED, MAINTENANCE).
     */
    @Column(nullable = false)
    private String status;

    // Constructors, getters, setters, equals, hashCode, toString

    public Asset() {}

    public Asset(String name, String type, String assetCode, LocalDate acquiredDate, String status) {
        this.name = name;
        this.type = type;
        this.assetCode = assetCode;
        this.acquiredDate = acquiredDate;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAssetCode() {
        return assetCode;
    }

    public void setAssetCode(String assetCode) {
        this.assetCode = assetCode;
    }

    public LocalDate getAcquiredDate() {
        return acquiredDate;
    }

    public void setAcquiredDate(LocalDate acquiredDate) {
        this.acquiredDate = acquiredDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Asset asset = (Asset) o;
        return id != null && id.equals(asset.id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public String toString() {
        return "Asset{" +
                "id=" + id +
                ", name='" + name + ''' +
                ", type='" + type + ''' +
                ", assetCode='" + assetCode + ''' +
                ", acquiredDate=" + acquiredDate +
                ", status='" + status + ''' +
                '}';
    }
}
