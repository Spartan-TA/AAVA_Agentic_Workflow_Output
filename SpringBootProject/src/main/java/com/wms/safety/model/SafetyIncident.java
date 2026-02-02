package com.wms.safety.model;

import com.wms.safety.enums.IncidentStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Entity representing a safety incident
 */
@Entity
@Table(name = "safety_incidents")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SafetyIncident {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Date and time of the incident
     */
    @Column(nullable = false)
    private LocalDateTime incidentDateTime;

    /**
     * Location of the incident
     */
    @Column(nullable = false)
    private String location;

    /**
     * Description of the incident
     */
    @Column(nullable = false, length = 2000)
    private String description;

    /**
     * Status of the incident (e.g., OPEN, CLOSED)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IncidentStatus status;

    /**
     * Employee ID who reported the incident
     */
    @Column(nullable = false)
    private Long reportedBy;

    /**
     * OSHA report number (if applicable)
     */
    private String oshaReportNumber;
}
