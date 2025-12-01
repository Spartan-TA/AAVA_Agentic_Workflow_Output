package com.wms.ems.safety.entity;

import lombok.*;
import javax.persistence.*;
import java.time.LocalDateTime;
import com.wms.ems.employee.entity.Employee;

/**
 * SafetyIncident entity representing reported safety incidents in the warehouse.
 */
@Entity
@Table(name = "safety_incident")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SafetyIncident {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_by", nullable = false)
    private Employee reportedBy;

    @Column(name = "incident_date", nullable = false)
    private LocalDateTime incidentDate;

    @Column(name = "location", length = 128)
    private String location;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "severity", length = 32)
    private String severity;

    @Column(name = "status", nullable = false, length = 32)
    private String status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}