package com.warehouse.ems.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing an energy reading from equipment.
 * Stores time-series energy consumption data including power demand,
 * voltage, current, power factor, and temperature measurements.
 */
@Entity
@Table(name = "energy_readings", indexes = {
    @Index(name = "idx_equipment_timestamp", columnList = "equipment_id,timestamp"),
    @Index(name = "idx_timestamp", columnList = "timestamp"),
    @Index(name = "idx_status", columnList = "status")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnergyReading {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_id", nullable = false)
    private Equipment equipment;
    
    @Column(nullable = false)
    private LocalDateTime timestamp;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal energyConsumed;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal powerDemand;
    
    @Column(precision = 5, scale = 2)
    private BigDecimal voltage;
    
    @Column(precision = 8, scale = 2)
    private BigDecimal current;
    
    @Column(precision = 5, scale = 2)
    private BigDecimal powerFactor;
    
    @Column(precision = 5, scale = 2)
    private BigDecimal temperature;
    
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ReadingStatus status;
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
    
    public enum ReadingStatus {
        NORMAL, WARNING, CRITICAL, ERROR
    }
}