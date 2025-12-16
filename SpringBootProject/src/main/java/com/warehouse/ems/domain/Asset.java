package com.warehouse.ems.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Asset entity for equipment and asset assignment.
 */
@Entity
@Table(name = "asset")
public class Asset {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to")
    private Employee assignedTo;

    private LocalDateTime checkoutDate;
    private LocalDateTime returnDate;
    private String condition;
    private String certificationRequired;
    private Boolean overdue = false;

    // Getters and setters omitted for brevity
}
