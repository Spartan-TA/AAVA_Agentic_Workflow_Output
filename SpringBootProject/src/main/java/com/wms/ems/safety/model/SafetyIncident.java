package com.wms.ems.safety.model;

import com.wms.ems.common.BaseEntity;
import com.wms.ems.employee.model.Employee;
import com.wms.ems.common.IncidentSeverity;
import com.wms.ems.common.IncidentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Entity representing a safety incident in the warehouse.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "safety_incidents")
public class SafetyIncident extends BaseEntity {

    /**
     * Description of the incident.
     */
    @Column(nullable = false, length = 1000)
    private String description;

    /**
     * Severity of the incident.
     */
    @Enumerated(EnumType.STRING)
    private IncidentSeverity severity;

    /**
     * Location of the incident.
     */
    private String location;

    /**
     * Status of the incident.
     */
    @Enumerated(EnumType.STRING)
    private IncidentStatus status;

    /**
     * Date and time the incident was reported.
     */
    private LocalDateTime reportedAt;

    /**
     * Employees involved in the incident.
     */
    @ManyToMany
    @JoinTable(
        name = "incident_employees",
        joinColumns = @JoinColumn(name = "incident_id"),
        inverseJoinColumns = @JoinColumn(name = "employee_id")
    )
    private List<Employee> involvedEmployees;
}