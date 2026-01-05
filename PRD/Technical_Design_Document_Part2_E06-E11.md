# WAREHOUSE EMPLOYEE MANAGEMENT SYSTEM â LOW-LEVEL TECHNICAL DESIGN DOCUMENT
## PART 2: EPICS E06-E11

## TABLE OF CONTENTS - PART 2
1. [Epic E06: Leave & Absence Management](#epic-e06)
2. [Epic E07: Training & Certification Tracking](#epic-e07)
3. [Epic E08: Safety Incidents & OSHA Reporting](#epic-e08)
4. [Epic E09: Equipment & Asset Assignment](#epic-e09)
5. [Epic E10: Performance Reviews & Goals](#epic-e10)
6. [Epic E11: Payroll Export Integration](#epic-e11)

---

## <a name="epic-e06"></a>EPIC E06: LEAVE & ABSENCE MANAGEMENT

### Section: Spring Boot Architecture Overview

**Description:** Handles leave requests, approvals, accruals, and integration with scheduling/payroll.

**Design Specification:**
- LeaveRequest entity: id, employee, type, startDate, endDate, status, accrualBalance
- LeavePolicy and LeaveBalance entities
- Approval workflow with supervisor notifications

**Sample Implementation:**
```java
package com.company.wms.leave.domain;

import com.company.wms.employee.domain.Employee;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "leave_requests")
public class LeaveRequest {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LeaveType type;
    
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;
    
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LeaveStatus status = LeaveStatus.PENDING;
    
    @Column(columnDefinition = "TEXT")
    private String reason;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approver_id")
    private Employee approver;
    
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;
    
    @Column(name = "denial_reason")
    private String denialReason;
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    // Getters and setters
}

public enum LeaveType {
    PTO,
    SICK,
    UNPAID,
    BEREAVEMENT,
    JURY_DUTY
}

public enum LeaveStatus {
    PENDING,
    APPROVED,
    DENIED,
    CANCELLED
}

@Entity
@Table(name = "leave_balances")
public class LeaveBalance {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LeaveType type;
    
    @Column(name = "available_days", nullable = false)
    private Integer availableDays;
    
    @Column(name = "used_days", nullable = false)
    private Integer usedDays = 0;
    
    @Column(name = "accrual_rate")
    private Double accrualRate; // Days per month
    
    @Column(name = "year")
    private Integer year;
    
    // Getters and setters
}
```

### Section: Service Layer - Leave Management

**Sample Implementation:**
```java
package com.company.wms.leave.service;

import com.company.wms.leave.domain.*;
import com.company.wms.leave.repository.*;
import com.company.wms.common.exception.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@Transactional
public class LeaveService {
    
    @Autowired
    private LeaveRequestRepository leaveRepository;
    
    @Autowired
    private LeaveBalanceRepository balanceRepository;
    
    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Autowired
    private NotificationService notificationService;
    
    public LeaveRequest requestLeave(
            Long employeeId, 
            LeaveType type, 
            LocalDate startDate, 
            LocalDate endDate, 
            String reason) {
        
        Employee employee = employeeRepository.findById(employeeId)
            .orElseThrow(() -> new EmployeeNotFoundException(
                "Employee not found with id: " + employeeId
            ));
        
        // Validate dates
        if (startDate.isAfter(endDate)) {
            throw new InvalidDateRangeException(
                "Start date must be before or equal to end date"
            );
        }
        
        if (startDate.isBefore(LocalDate.now())) {
            throw new InvalidDateRangeException(
                "Cannot request leave for past dates"
            );
        }
        
        // Validate balance for PTO
        if (type == LeaveType.PTO) {
            LeaveBalance balance = balanceRepository
                .findByEmployeeAndTypeAndYear(
                    employee, 
                    type, 
                    LocalDate.now().getYear()
                )
                .orElseThrow(() -> new LeaveBalanceNotFoundException(
                    "Leave balance not found for employee"
                ));
            
            long daysRequested = ChronoUnit.DAYS.between(startDate, endDate) + 1;
            
            if (balance.getAvailableDays() < daysRequested) {
                throw new InsufficientLeaveBalanceException(
                    String.format("Insufficient leave balance. Available: %d days, Requested: %d days",
                        balance.getAvailableDays(), daysRequested)
                );
            }
        }
        
        // Check for overlapping leave requests
        if (hasOverlappingLeave(employee, startDate, endDate)) {
            throw new OverlappingLeaveException(
                "Leave request overlaps with existing approved leave"
            );
        }
        
        LeaveRequest request = new LeaveRequest();
        request.setEmployee(employee);
        request.setType(type);
        request.setStartDate(startDate);
        request.setEndDate(endDate);
        request.setReason(reason);
        request.setStatus(LeaveStatus.PENDING);
        
        LeaveRequest saved = leaveRepository.save(request);
        
        // Notify supervisor
        notificationService.notifySupervisorOfLeaveRequest(employee.getSupervisor(), saved);
        
        return saved;
    }
    
    @PreAuthorize("hasRole('SUPERVISOR') or hasRole('ADMIN')")
    public void approveLeave(Long requestId, Long approverId) {
        LeaveRequest request = leaveRepository.findById(requestId)
            .orElseThrow(() -> new LeaveRequestNotFoundException(
                "Leave request not found with id: " + requestId
            ));
        
        if (request.getStatus() != LeaveStatus.PENDING) {
            throw new InvalidLeaveStatusException(
                "Only pending leave requests can be approved"
            );
        }
        
        Employee approver = employeeRepository.findById(approverId)
            .orElseThrow(() -> new EmployeeNotFoundException(
                "Approver not found with id: " + approverId
            ));
        
        request.setStatus(LeaveStatus.APPROVED);
        request.setApprover(approver);
        request.setApprovedAt(LocalDateTime.now());
        
        leaveRepository.save(request);
        
        // Update balance if PTO
        if (request.getType() == LeaveType.PTO) {
            updateBalance(request.getEmployee(), request.getType(), 
                         request.getStartDate(), request.getEndDate());
        }
        
        // Notify employee
        notificationService.notifyEmployeeOfLeaveApproval(request.getEmployee(), request);
    }
    
    @PreAuthorize("hasRole('SUPERVISOR') or hasRole('ADMIN')")
    public void denyLeave(Long requestId, Long approverId, String denialReason) {
        LeaveRequest request = leaveRepository.findById(requestId)
            .orElseThrow(() -> new LeaveRequestNotFoundException(
                "Leave request not found with id: " + requestId
            ));
        
        if (request.getStatus() != LeaveStatus.PENDING) {
            throw new InvalidLeaveStatusException(
                "Only pending leave requests can be denied"
            );
        }
        
        Employee approver = employeeRepository.findById(approverId)
            .orElseThrow(() -> new EmployeeNotFoundException(
                "Approver not found with id: " + approverId
            ));
        
        request.setStatus(LeaveStatus.DENIED);
        request.setApprover(approver);
        request.setApprovedAt(LocalDateTime.now());
        request.setDenialReason(denialReason);
        
        leaveRepository.save(request);
        
        // Notify employee
        notificationService.notifyEmployeeOfLeaveDenial(request.getEmployee(), request);
    }
    
    private void updateBalance(
            Employee employee, 
            LeaveType type, 
            LocalDate startDate, 
            LocalDate endDate) {
        
        LeaveBalance balance = balanceRepository
            .findByEmployeeAndTypeAndYear(employee, type, LocalDate.now().getYear())
            .orElseThrow(() -> new LeaveBalanceNotFoundException(
                "Leave balance not found"
            ));
        
        long daysUsed = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        balance.setAvailableDays(balance.getAvailableDays() - (int) daysUsed);
        balance.setUsedDays(balance.getUsedDays() + (int) daysUsed);
        
        balanceRepository.save(balance);
    }
    
    private boolean hasOverlappingLeave(
            Employee employee, 
            LocalDate startDate, 
            LocalDate endDate) {
        
        List<LeaveRequest> existingLeaves = leaveRepository
            .findByEmployeeAndStatusAndDateRange(
                employee, 
                LeaveStatus.APPROVED, 
                startDate, 
                endDate
            );
        
        return !existingLeaves.isEmpty();
    }
    
    @Transactional(readOnly = true)
    public boolean isAvailableForShift(Employee employee, LocalDate date) {
        return !leaveRepository.existsByEmployeeAndDateRangeAndStatus(
            employee, 
            date, 
            LeaveStatus.APPROVED
        );
    }
}
```

---

## <a name="epic-e07"></a>EPIC E07: TRAINING & CERTIFICATION TRACKING

### Section: Spring Boot Architecture Overview

**Description:** Tracks certifications, expirations, renewals, and blocks assignments if expired.

**Design Specification:**
- Certification entity: id, employee, type, issueDate, expiryDate, documentUrl
- Scheduled job for expiry alerts
- Integration with asset assignment and shift scheduling

**Sample Implementation:**
```java
package com.company.wms.certification.domain;

import com.company.wms.employee.domain.Employee;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "certifications")
public class Certification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CertificationType type;
    
    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;
    
    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;
    
    @Column(name = "document_url")
    private String documentUrl;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CertificationStatus status = CertificationStatus.ACTIVE;
    
    @Column(name = "issuing_authority")
    private String issuingAuthority;
    
    @Column(name = "certification_number")
    private String certificationNumber;
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updateStatus();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updateStatus();
    }
    
    private void updateStatus() {
        if (expiryDate.isBefore(LocalDate.now())) {
            status = CertificationStatus.EXPIRED;
        } else if (expiryDate.isBefore(LocalDate.now().plusDays(30))) {
            status = CertificationStatus.EXPIRING_SOON;
        } else {
            status = CertificationStatus.ACTIVE;
        }
    }
    
    // Getters and setters
}

public enum CertificationType {
    FORKLIFT,
    SAFETY,
    HAZMAT,
    FIRST_AID,
    REACH_TRUCK,
    ORDER_PICKER,
    PALLET_JACK
}

public enum CertificationStatus {
    ACTIVE,
    EXPIRING_SOON,
    EXPIRED,
    PENDING_RENEWAL,
    REVOKED
}
```

### Section: Service Layer - Certification Management

**Sample Implementation:**
```java
package com.company.wms.certification.service;

import com.company.wms.certification.domain.*;
import com.company.wms.certification.repository.CertificationRepository;
import com.company.wms.common.exception.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CertificationService {
    
    @Autowired
    private CertificationRepository certificationRepository;
    
    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private FileStorageService fileStorageService;
    
    public Certification createCertification(
            Long employeeId, 
            CertificationType type, 
            LocalDate issueDate, 
            LocalDate expiryDate,
            String issuingAuthority,
            String certificationNumber) {
        
        Employee employee = employeeRepository.findById(employeeId)
            .orElseThrow(() -> new EmployeeNotFoundException(
                "Employee not found with id: " + employeeId
            ));
        
        if (issueDate.isAfter(expiryDate)) {
            throw new InvalidDateRangeException(
                "Issue date must be before expiry date"
            );
        }
        
        Certification certification = new Certification();
        certification.setEmployee(employee);
        certification.setType(type);
        certification.setIssueDate(issueDate);
        certification.setExpiryDate(expiryDate);
        certification.setIssuingAuthority(issuingAuthority);
        certification.setCertificationNumber(certificationNumber);
        certification.setStatus(CertificationStatus.ACTIVE);
        
        return certificationRepository.save(certification);
    }
    
    public Certification renewCertification(
            Long certificationId, 
            LocalDate newExpiryDate) {
        
        Certification certification = certificationRepository.findById(certificationId)
            .orElseThrow(() -> new CertificationNotFoundException(
                "Certification not found with id: " + certificationId
            ));
        
        certification.setExpiryDate(newExpiryDate);
        certification.setStatus(CertificationStatus.ACTIVE);
        
        return certificationRepository.save(certification);
    }
    
    public void uploadProofDocument(
            Long certificationId, 
            MultipartFile file) throws IOException {
        
        Certification certification = certificationRepository.findById(certificationId)
            .orElseThrow(() -> new CertificationNotFoundException(
                "Certification not found with id: " + certificationId
            ));
        
        String documentUrl = fileStorageService.store(file, "certifications");
        certification.setDocumentUrl(documentUrl);
        
        certificationRepository.save(certification);
    }
    
    @Scheduled(cron = "0 0 9 * * *") // Daily at 9 AM
    public void alertExpiringCertifications() {
        LocalDate thirtyDaysFromNow = LocalDate.now().plusDays(30);
        LocalDate sevenDaysFromNow = LocalDate.now().plusDays(7);
        
        List<Certification> expiringSoon = certificationRepository
            .findByExpiryDateBetweenAndStatus(
                LocalDate.now(), 
                thirtyDaysFromNow,
                CertificationStatus.ACTIVE
            );
        
        for (Certification cert : expiringSoon) {
            long daysUntilExpiry = ChronoUnit.DAYS.between(
                LocalDate.now(), 
                cert.getExpiryDate()
            );
            
            if (daysUntilExpiry == 30 || daysUntilExpiry == 7 || daysUntilExpiry == 1) {
                notificationService.sendCertificationExpiryAlert(
                    cert.getEmployee(),
                    cert.getType(),
                    daysUntilExpiry
                );
                
                // Also notify supervisor
                if (cert.getEmployee().getSupervisor() != null) {
                    notificationService.sendCertificationExpiryAlert(
                        cert.getEmployee().getSupervisor(),
                        cert.getType(),
                        daysUntilExpiry
                    );
                }
            }
        }
    }
    
    @Transactional(readOnly = true)
    public boolean isValidForAssignment(
            Employee employee, 
            CertificationType requiredCert) {
        
        Optional<Certification> cert = certificationRepository
            .findByEmployeeAndTypeAndStatus(
                employee, 
                requiredCert, 
                CertificationStatus.ACTIVE
            );
        
        if (cert.isEmpty()) {
            return false;
        }
        
        return cert.get().getExpiryDate().isAfter(LocalDate.now());
    }
    
    @Transactional(readOnly = true)
    public List<Certification> getEmployeeCertifications(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
            .orElseThrow(() -> new EmployeeNotFoundException(
                "Employee not found with id: " + employeeId
            ));
        
        return certificationRepository.findByEmployee(employee);
    }
}
```

---

## <a name="epic-e08"></a>EPIC E08: SAFETY INCIDENTS & OSHA REPORTING

### Section: Spring Boot Architecture Overview

**Description:** Records safety incidents, manages investigation workflow, and generates OSHA reports.

**Design Specification:**
- SafetyIncident entity: id, date, severity, location, description, status, involvedEmployees
- Investigation workflow with corrective actions
- OSHA 300/300A report generation

**Sample Implementation:**
```java
package com.company.wms.safety.domain;

import com.company.wms.employee.domain.Employee;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "safety_incidents")
public class SafetyIncident {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private LocalDate date;
    
    @Column(nullable = false)
    private LocalDateTime reportedAt;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Severity severity;
    
    @Column(nullable = false)
    private String location;
    
    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IncidentStatus status = IncidentStatus.OPEN;
    
    @ManyToMany
    @JoinTable(
        name = "incident_employees",
        joinColumns = @JoinColumn(name = "incident_id"),
        inverseJoinColumns = @JoinColumn(name = "employee_id")
    )
    private List<Employee> involvedEmployees = new ArrayList<>();
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_by")
    private Employee reportedBy;
    
    @Lob
    @Column(columnDefinition = "TEXT")
    private String correctiveActions;
    
    @Column(name = "investigation_started_at")
    private LocalDateTime investigationStartedAt;
    
    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "investigator_id")
    private Employee investigator;
    
    @Column(name = "osha_recordable")
    private Boolean oshaRecordable = false;
    
    @Column(name = "days_away_from_work")
    private Integer daysAwayFromWork = 0;
    
    @Column(name = "days_restricted_work")
    private Integer daysRestrictedWork = 0;
    
    // Getters and setters
}

public enum Severity {
    MINOR,
    MODERATE,
    SEVERE,
    FATAL,
    NEAR_MISS
}

public enum IncidentStatus {
    OPEN,
    INVESTIGATING,
    RESOLVED,
    CLOSED
}
```

### Section: Service Layer - Safety Management

**Sample Implementation:**
```java
package com.company.wms.safety.service;

import com.company.wms.safety.domain.*;
import com.company.wms.safety.repository.SafetyIncidentRepository;
import com.company.wms.common.exception.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class SafetyService {
    
    @Autowired
    private SafetyIncidentRepository incidentRepository;
    
    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Autowired
    private NotificationService notificationService;
    
    public SafetyIncident createIncident(
            LocalDate date,
            Severity severity,
            String location,
            String description,
            List<Long> involvedEmployeeIds,
            Long reportedById) {
        
        Employee reportedBy = employeeRepository.findById(reportedById)
            .orElseThrow(() -> new EmployeeNotFoundException(
                "Reporter not found with id: " + reportedById
            ));
        
        SafetyIncident incident = new SafetyIncident();
        incident.setDate(date);
        incident.setReportedAt(LocalDateTime.now());
        incident.setSeverity(severity);
        incident.setLocation(location);
        incident.setDescription(description);
        incident.setStatus(IncidentStatus.OPEN);
        incident.setReportedBy(reportedBy);
        
        if (involvedEmployeeIds != null && !involvedEmployeeIds.isEmpty()) {
            List<Employee> employees = employeeRepository.findAllById(involvedEmployeeIds);
            incident.setInvolvedEmployees(employees);
        }
        
        // Determine if OSHA recordable
        incident.setOshaRecordable(isOSHARecordable(severity));
        
        SafetyIncident saved = incidentRepository.save(incident);
        
        // Notify safety officer
        notificationService.notifySafetyOfficerOfIncident(saved);
        
        return saved;
    }
    
    @PreAuthorize("hasRole('SAFETY_OFFICER') or hasRole('ADMIN')")
    public void startInvestigation(
            Long incidentId, 
            Long investigatorId) {
        
        SafetyIncident incident = incidentRepository.findById(incidentId)
            .orElseThrow(() -> new SafetyIncidentNotFoundException(
                "Incident not found with id: " + incidentId
            ));
        
        Employee investigator = employeeRepository.findById(investigatorId)
            .orElseThrow(() -> new EmployeeNotFoundException(
                "Investigator not found with id: " + investigatorId
            ));
        
        incident.setStatus(IncidentStatus.INVESTIGATING);
        incident.setInvestigator(investigator);
        incident.setInvestigationStartedAt(LocalDateTime.now());
        
        incidentRepository.save(incident);
    }
    
    @PreAuthorize("hasRole('SAFETY_OFFICER') or hasRole('ADMIN')")
    public void resolveIncident(
            Long incidentId, 
            String correctiveActions,
            Integer daysAwayFromWork,
            Integer daysRestrictedWork) {
        
        SafetyIncident incident = incidentRepository.findById(incidentId)
            .orElseThrow(() -> new SafetyIncidentNotFoundException(
                "Incident not found with id: " + incidentId
            ));
        
        if (incident.getStatus() != IncidentStatus.INVESTIGATING) {
            throw new InvalidIncidentStatusException(
                "Only incidents under investigation can be resolved"
            );
        }
        
        incident.setStatus(IncidentStatus.RESOLVED);
        incident.setCorrectiveActions(correctiveActions);
        incident.setDaysAwayFromWork(daysAwayFromWork);
        incident.setDaysRestrictedWork(daysRestrictedWork);
        incident.setResolvedAt(LocalDateTime.now());
        
        incidentRepository.save(incident);
        
        // Notify involved employees and management
        notificationService.notifyIncidentResolution(incident);
    }
    
    @Transactional(readOnly = true)
    public byte[] generateOSHAReport(int year) {
        List<SafetyIncident> incidents = incidentRepository
            .findByDateBetweenAndOshaRecordableTrue(
                LocalDate.of(year, 1, 1),
                LocalDate.of(year, 12, 31)
            );
        
        // Generate OSHA 300/300A report
        return generateOSHA300Report(incidents, year);
    }
    
    private byte[] generateOSHA300Report(List<SafetyIncident> incidents, int year) {
        // Implementation for OSHA 300 report generation
        // This would typically use a PDF library like iText or Apache PDFBox
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        // PDF generation logic here
        // Include: Case number, employee name, job title, date of injury,
        // where event occurred, describe injury/illness, classify case,
        // days away from work, days of restricted work
        
        return baos.toByteArray();
    }
    
    private boolean isOSHARecordable(Severity severity) {
        return severity == Severity.SEVERE || severity == Severity.FATAL;
    }
    
    @Transactional(readOnly = true)
    public List<SafetyIncident> getIncidentsByStatus(IncidentStatus status) {
        return incidentRepository.findByStatus(status);
    }
}
```

---

## <a name="epic-e09"></a>EPIC E09: EQUIPMENT & ASSET ASSIGNMENT

### Section: Spring Boot Architecture Overview

**Description:** Assigns assets to employees, tracks check-in/out, blocks use if certification missing.

**Design Specification:**
- Asset entity: id, type, serialNumber, condition, assignedTo, checkedOutAt, checkedInAt
- AssetHistory for audit trail
- Integration with certification validation

**Sample Implementation:**
```java
package com.company.wms.asset.domain;

import com.company.wms.employee.domain.Employee;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "assets")
public class Asset {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssetType type;
    
    @Column(name = "serial_number", nullable = false, unique = true)
    private String serialNumber;
    
    @Column(nullable = false)
    private String name;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssetCondition condition = AssetCondition.GOOD;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to")
    private Employee assignedTo;
    
    @Column(name = "checked_out_at")
    private LocalDateTime checkedOutAt;
    
    @Column(name = "checked_in_at")
    private LocalDateTime checkedInAt;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssetStatus status = AssetStatus.AVAILABLE;
    
    @Column(name = "last_maintenance_date")
    private LocalDateTime lastMaintenanceDate;
    
    @Column(name = "next_maintenance_date")
    private LocalDateTime nextMaintenanceDate;
    
    @Column(name = "purchase_date")
    private LocalDateTime purchaseDate;
    
    @Column(name = "purchase_cost")
    private Double purchaseCost;
    
    // Getters and setters
}

public enum AssetType {
    SCANNER,
    FORKLIFT,
    PPE,
    PALLET_JACK,
    REACH_TRUCK,
    ORDER_PICKER,
    RADIO,
    TABLET
}

public enum AssetCondition {
    EXCELLENT,
    GOOD,
    FAIR,
    POOR,
    NEEDS_REPAIR
}

public enum AssetStatus {
    AVAILABLE,
    CHECKED_OUT,
    MAINTENANCE,
    RETIRED,
    LOST
}

@Entity
@Table(name = "asset_history")
public class AssetHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssetAction action;
    
    @Column(nullable = false)
    private LocalDateTime timestamp;
    
    private String notes;
    
    // Getters and setters
}

public enum AssetAction {
    CHECKED_OUT,
    CHECKED_IN,
    ASSIGNED,
    UNASSIGNED,
    MAINTENANCE_STARTED,
    MAINTENANCE_COMPLETED,
    REPORTED_LOST,
    REPORTED_DAMAGED
}
```

### Section: Service Layer - Asset Management

**Sample Implementation:**
```java
package com.company.wms.asset.service;

import com.company.wms.asset.domain.*;
import com.company.wms.asset.repository.*;
import com.company.wms.certification.domain.CertificationType;
import com.company.wms.certification.service.CertificationService;
import com.company.wms.common.exception.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class AssetService {
    
    @Autowired
    private AssetRepository assetRepository;
    
    @Autowired
    private AssetHistoryRepository historyRepository;
    
    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Autowired
    private CertificationService certificationService;
    
    public void checkOut(Long assetId, Long employeeId) {
        Asset asset = assetRepository.findById(assetId)
            .orElseThrow(() -> new AssetNotFoundException(
                "Asset not found with id: " + assetId
            ));
        
        Employee employee = employeeRepository.findById(employeeId)
            .orElseThrow(() -> new EmployeeNotFoundException(
                "Employee not found with id: " + employeeId
            ));
        
        // Validate asset is available
        if (asset.getStatus() != AssetStatus.AVAILABLE) {
            throw new AssetNotAvailableException(
                "Asset is not available for checkout. Current status: " + asset.getStatus()
            );
        }
        
        // Validate certification if required
        CertificationType requiredCert = getRequiredCertification(asset.getType());
        if (requiredCert != null) {
            if (!certificationService.isValidForAssignment(employee, requiredCert)) {
                throw new CertificationExpiredException(
                    String.format("Valid %s certification required to use this asset", 
                                 requiredCert)
                );
            }
        }
        
        // Check out asset
        asset.setAssignedTo(employee);
        asset.setCheckedOutAt(LocalDateTime.now());
        asset.setStatus(AssetStatus.CHECKED_OUT);
        
        assetRepository.save(asset);
        
        // Record history
        recordHistory(asset, employee, AssetAction.CHECKED_OUT, null);
    }
    
    public void checkIn(Long assetId, AssetCondition condition, String notes) {
        Asset asset = assetRepository.findById(assetId)
            .orElseThrow(() -> new AssetNotFoundException(
                "Asset not found with id: " + assetId
            ));
        
        if (asset.getStatus() != AssetStatus.CHECKED_OUT) {
            throw new InvalidAssetOperationException(
                "Asset is not checked out. Current status: " + asset.getStatus()
            );
        }
        
        Employee employee = asset.getAssignedTo();
        
        // Check in asset
        asset.setCheckedInAt(LocalDateTime.now());
        asset.setCondition(condition);
        asset.setStatus(AssetStatus.AVAILABLE);
        asset.setAssignedTo(null);
        
        assetRepository.save(asset);
        
        // Record history
        recordHistory(asset, employee, AssetAction.CHECKED_IN, notes);
        
        // If condition is poor or needs repair, flag for maintenance
        if (condition == AssetCondition.POOR || condition == AssetCondition.NEEDS_REPAIR) {
            flagForMaintenance(asset);
        }
    }
    
    public void assignAsset(Long assetId, Long employeeId) {
        Asset asset = assetRepository.findById(assetId)
            .orElseThrow(() -> new AssetNotFoundException(
                "Asset not found with id: " + assetId
            ));
        
        Employee employee = employeeRepository.findById(employeeId)
            .orElseThrow(() -> new EmployeeNotFoundException(
                "Employee not found with id: " + employeeId
            ));
        
        asset.setAssignedTo(employee);
        assetRepository.save(asset);
        
        recordHistory(asset, employee, AssetAction.ASSIGNED, null);
    }
    
    private void flagForMaintenance(Asset asset) {
        asset.setStatus(AssetStatus.MAINTENANCE);
        assetRepository.save(asset);
        
        // Notify maintenance team
        // notificationService.notifyMaintenanceTeam(asset);
    }
    
    private void recordHistory(
            Asset asset, 
            Employee employee, 
            AssetAction action, 
            String notes) {
        
        AssetHistory history = new AssetHistory();
        history.setAsset(asset);
        history.setEmployee(employee);
        history.setAction(action);
        history.setTimestamp(LocalDateTime.now());
        history.setNotes(notes);
        
        historyRepository.save(history);
    }
    
    private CertificationType getRequiredCertification(AssetType assetType) {
        return switch (assetType) {
            case FORKLIFT -> CertificationType.FORKLIFT;
            case REACH_TRUCK -> CertificationType.REACH_TRUCK;
            case ORDER_PICKER -> CertificationType.ORDER_PICKER;
            default -> null;
        };
    }
    
    @Transactional(readOnly = true)
    public List<Asset> getOverdueAssets() {
        LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(3);
        return assetRepository.findByStatusAndCheckedOutAtBefore(
            AssetStatus.CHECKED_OUT, 
            threeDaysAgo
        );
    }
}
```

---

## <a name="epic-e10"></a>EPIC E10: PERFORMANCE REVIEWS & GOALS

### Section: Spring Boot Architecture Overview

**Description:** Manages review templates, goals, ratings, and acknowledgements.

**Design Specification:**
- PerformanceReview entity: id, employee, cycle, goals, ratings, supervisor, status, comments
- ReviewTemplate for standardized review criteria
- Workflow for submission and acknowledgement

**Sample Implementation:**
```java
package com.company.wms.performance.domain;

import com.company.wms.employee.domain.Employee;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "performance_reviews")
public class PerformanceReview {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;
    
    @Column(nullable = false)
    private String cycle; // e.g., "Q1 2024", "Annual 2024"
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id")
    private ReviewTemplate template;
    
    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReviewGoal> goals = new ArrayList<>();
    
    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReviewRating> ratings = new ArrayList<>();
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supervisor_id")
    private Employee supervisor;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReviewStatus status = ReviewStatus.DRAFT;
    
    @Lob
    @Column(columnDefinition = "TEXT")
    private String supervisorComments;
    
    @Lob
    @Column(columnDefinition = "TEXT")
    private String employeeComments;
    
    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;
    
    @Column(name = "acknowledged_at")
    private LocalDateTime acknowledgedAt;
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    // Getters and setters
}

public enum ReviewStatus {
    DRAFT,
    SUBMITTED,
    ACKNOWLEDGED,
    FINALIZED
}

@Entity
@Table(name = "review_goals")
public class ReviewGoal {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    private PerformanceReview review;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;
    
    @Enumerated(EnumType.STRING)
    private GoalStatus status;
    
    private String outcome;
    
    // Getters and setters
}

public enum GoalStatus {
    NOT_STARTED,
    IN_PROGRESS,
    ACHIEVED,
    PARTIALLY_ACHIEVED,
    NOT_ACHIEVED
}
```

---

## <a name="epic-e11"></a>EPIC E11: PAYROLL EXPORT INTEGRATION

### Section: Spring Boot Architecture Overview

**Description:** Generates payroll-ready files from attendance/leave, maps to provider formats, delivers securely.

**Design Specification:**
- PayrollExport entity: id, period, fileUrl, status, deliveryMethod, attempts
- Integration with SFTP/API for secure delivery
- Reconciliation with attendance reports

**Sample Implementation:**
```java
package com.company.wms.payroll.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "payroll_exports")
public class PayrollExport {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String period; // e.g., "2024-01"
    
    @Column(name = "file_url")
    private String fileUrl;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExportStatus status = ExportStatus.PENDING;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeliveryMethod deliveryMethod;
    
    @Column(nullable = false)
    private Integer attempts = 0;
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @Column(name = "error_message")
    private String errorMessage;
    
    @Column(name = "total_records")
    private Integer totalRecords;
    
    @Column(name = "total_hours")
    private Double totalHours;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    // Getters and setters
}

public enum ExportStatus {
    PENDING,
    GENERATING,
    READY,
    DELIVERING,
    COMPLETED,
    FAILED
}

public enum DeliveryMethod {
    SFTP,
    API,
    EMAIL
}
```

### Section: Service Layer - Payroll Export

**Sample Implementation:**
```java
package com.company.wms.payroll.service;

import com.company.wms.payroll.domain.*;
import com.company.wms.payroll.repository.PayrollExportRepository;
import com.company.wms.attendance.service.AttendanceService;
import com.company.wms.leave.service.LeaveService;
import com.company.wms.common.exception.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class PayrollExportService {
    
    @Autowired
    private PayrollExportRepository exportRepository;
    
    @Autowired
    private AttendanceService attendanceService;
    
    @Autowired
    private LeaveService leaveService;
    
    @Autowired
    private FileStorageService fileStorageService;
    
    @Autowired
    private SFTPClient sftpClient;
    
    @Autowired
    private AuditService auditService;
    
    public PayrollExport generateExport(String period, DeliveryMethod deliveryMethod) {
        // Parse period (e.g., "2024-01")
        YearMonth yearMonth = YearMonth.parse(period);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();
        
        PayrollExport export = new PayrollExport();
        export.setPeriod(period);
        export.setDeliveryMethod(deliveryMethod);
        export.setStatus(ExportStatus.GENERATING);
        
        PayrollExport saved = exportRepository.save(export);
        
        try {
            // Gather attendance and leave data
            List<AttendanceEvent> attendanceEvents = attendanceService
                .getEventsInRange(startDate, endDate);
            List<LeaveRequest> approvedLeaves = leaveService
                .getApprovedLeavesInRange(startDate, endDate);
            
            // Generate payroll file
            String fileContent = generatePayrollFile(attendanceEvents, approvedLeaves);
            String fileUrl = fileStorageService.store(
                fileContent.getBytes(), 
                "payroll_" + period + ".csv"
            );
            
            export.setFileUrl(fileUrl);
            export.setStatus(ExportStatus.READY);
            export.setTotalRecords(attendanceEvents.size());
            export.setTotalHours(calculateTotalHours(attendanceEvents));
            
            exportRepository.save(export);
            
            // Audit log
            auditService.logChange(
                "SYSTEM", 
                "PayrollExport", 
                "CREATE", 
                null, 
                export
            );
            
            return export;
            
        } catch (Exception e) {
            export.setStatus(ExportStatus.FAILED);
            export.setErrorMessage(e.getMessage());
            exportRepository.save(export);
            throw new PayrollExportException("Failed to generate payroll export", e);
        }
    }
    
    @Async
    public void deliverExport(Long exportId) {
        PayrollExport export = exportRepository.findById(exportId)
            .orElseThrow(() -> new PayrollExportNotFoundException(
                "Export not found with id: " + exportId
            ));
        
        export.setStatus(ExportStatus.DELIVERING);
        export.setAttempts(export.getAttempts() + 1);
        exportRepository.save(export);
        
        try {
            if (export.getDeliveryMethod() == DeliveryMethod.SFTP) {
                sftpClient.upload(export.getFileUrl());
            } else if (export.getDeliveryMethod() == DeliveryMethod.API) {
                // API delivery logic
            }
            
            export.setStatus(ExportStatus.COMPLETED);
            export.setCompletedAt(LocalDateTime.now());
            
            // Audit log
            auditService.logChange(
                "SYSTEM", 
                "PayrollExport", 
                "DELIVER", 
                null, 
                export
            );
            
        } catch (Exception e) {
            export.setStatus(ExportStatus.FAILED);
            export.setErrorMessage(e.getMessage());
            
            // Retry logic with exponential backoff
            if (export.getAttempts() < 3) {
                // Schedule retry
            }
        }
        
        exportRepository.save(export);
    }
    
    private String generatePayrollFile(
            List<AttendanceEvent> events, 
            List<LeaveRequest> leaves) {
        
        StringBuilder csv = new StringBuilder();
        csv.append("Employee ID,Badge ID,Name,Regular Hours,Overtime Hours,Leave Hours,Total Hours
");
        
        // Aggregate data by employee
        // Map employee data to payroll provider format
        // ...
        
        return csv.toString();
    }
    
    private Double calculateTotalHours(List<AttendanceEvent> events) {
        return events.stream()
            .filter(e -> e.getHoursWorked() != null)
            .mapToDouble(AttendanceEvent::getHoursWorked)
            .sum();
    }
}
```

---

**Document Version:** 1.0 - Part 2
**Covers:** Epics E06-E11
**Next Part:** Technical_Design_Document_Part3_E12-E17.md