package com.wems.safety.domain;

import com.wems.employee.domain.Employee;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "safety_incidents")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SafetyIncident {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String incidentNumber;
    
    @Column(nullable = false)
    private LocalDate incidentDate;
    
    private LocalDateTime incidentTime;
    
    @Enumerated(EnumType.STRING)
    private IncidentType type;
    
    @Enumerated(EnumType.STRING)
    private Severity severity;
    
    @Column(nullable = false)
    private String location;
    
    @Column(length = 2000)
    private String description;
    
    @ManyToMany
    @JoinTable(name = "incident_involved_employees")
    private List<Employee> involvedEmployees = new ArrayList<>();
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_by")
    private Employee reportedBy;
    
    @Enumerated(EnumType.STRING)
    private IncidentStatus status;
    
    private String immediateActionTaken;
    private String rootCause;
    private String correctiveActions;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "investigator_id")
    private Employee investigator;
    
    private LocalDateTime investigationStartedAt;
    private LocalDateTime resolvedAt;
    private boolean oshaRecordable = false;
    private Integer daysAwayFromWork;
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (incidentNumber == null) {
            incidentNumber = "INC-" + LocalDate.now().getYear() + "-" + System.currentTimeMillis();
        }
    }
}
