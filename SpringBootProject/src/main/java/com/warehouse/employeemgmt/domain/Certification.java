package com.warehouse.employeemgmt.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

/**
 * Certification Entity - Training and certification tracking
 * 
 * Tracks employee certifications, expirations, and renewals.
 * Supports blocking assignments for expired certifications and document uploads.
 * 
 * Features:
 * - Certification type tracking (e.g., forklift, safety)
 * - Expiry date management with alerts
 * - Document URL for proof storage
 * - Renewal tracking
 * - Assignment blocking for expired certs
 * 
 * @author Warehouse Management Team
 * @version 1.0.0
 */
@Entity
@Table(name = "certifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Certification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(nullable = false, length = 100)
    private String type; // e.g., FORKLIFT, SAFETY, HAZMAT

    @Column(name = "issue_date")
    private LocalDate issueDate;

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @Column(name = "document_url", length = 500)
    private String documentUrl;

    @Column(name = "is_active")
    private boolean isActive = true;

    @Column(name = "renewal_count")
    private Integer renewalCount = 0;

    /**
     * Check if certification is expired
     */
    public boolean isExpired() {
        return LocalDate.now().isAfter(expiryDate);
    }

    /**
     * Check if certification is expiring soon (within days)
     */
    public boolean isExpiringSoon(int days) {
        return LocalDate.now().plusDays(days).isAfter(expiryDate) && !isExpired();
    }
}