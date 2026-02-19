package com.company.wems.entity;

import lombok.*;
import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Asset entity for equipment assignment.
 */
@Entity
@Table(name = "asset", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"serial_number"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Asset {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "serial_number", nullable = false, unique = true)
    private String serialNumber;

    @Column(name = "assigned_to")
    private Long assignedTo;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "checked_out_at")
    private LocalDateTime checkedOutAt;

    @Column(name = "returned_at")
    private LocalDateTime returnedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
