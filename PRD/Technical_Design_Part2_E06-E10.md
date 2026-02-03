# Warehouse Employee Management System - Technical Design Document
## Part 2: Epics E06-E10

### E06: Leave & Absence Management

**Entity Design:**
```java
@Entity
public class LeaveRequest extends BaseEntity {
    @ManyToOne private Employee employee;
    @Enumerated(EnumType.STRING) private LeaveType type;
    private LocalDate startDate;
    private LocalDate endDate;
    @Enumerated(EnumType.STRING) private LeaveStatus status;
}

@Entity
public class LeaveBalance extends BaseEntity {
    @ManyToOne private Employee employee;
    @Enumerated(EnumType.STRING) private LeaveType type;
    private int accrued;
    private int used;
}
```

**Service Layer:**
```java
public interface LeaveService {
    LeaveRequestDTO requestLeave(LeaveRequestDTO dto);
    void approveLeave(Long requestId);
    void denyLeave(Long requestId, String reason);
    LeaveBalanceDTO getBalance(Long employeeId, LeaveType type);
}
```

**REST Endpoints:**
- POST /api/leave/request
- POST /api/leave/approve/{id}
- POST /api/leave/deny/{id}
- GET /api/leave/balance/{employeeId}

---

### E07: Training & Certification Tracking

**Entity Design:**
```java
@Entity
public class Certification extends BaseEntity {
    private String name;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    @ManyToOne private Employee employee;
    private String documentUrl;
}
```

**Service Layer:**
```java
public interface CertificationService {
    CertificationDTO addCertification(Long employeeId, CertificationDTO dto);
    void renewCertification(Long certId, CertificationDTO dto);
    List<CertificationDTO> getExpiringCerts(int days);
}
```

**Scheduled Job:**
```java
@Scheduled(cron = "0 0 8 * * ?")
public void sendExpiryAlerts() {
    List<Certification> expiring = certificationRepository.findByExpiryDateBetween(
        LocalDate.now(), LocalDate.now().plusDays(30)
    );
    // Send notifications
}
```

**REST Endpoints:**
- POST /api/certifications/{employeeId}
- PUT /api/certifications/{certId}/renew
- GET /api/certifications/expiring

---

### E08: Safety Incidents & OSHA Reporting

**Entity Design:**
```java
@Entity
public class SafetyIncident extends BaseEntity {
    private String description;
    private String location;
    @ManyToMany private List<Employee> involvedEmployees;
    @Enumerated(EnumType.STRING) private IncidentSeverity severity;
    @Enumerated(EnumType.STRING) private IncidentStatus status;
}
```

**Service Layer:**
```java
public interface SafetyService {
    SafetyIncidentDTO reportIncident(SafetyIncidentDTO dto);
    void updateStatus(Long incidentId, IncidentStatus status);
    List<SafetyIncidentDTO> getIncidents(LocalDate from, LocalDate to);
    byte[] exportOSHA300(int year);
}
```

**REST Endpoints:**
- POST /api/safety/incidents
- PUT /api/safety/incidents/{id}/status
- GET /api/safety/incidents
- GET /api/safety/osha300/{year}

---

### E09: Equipment & Asset Assignment

**Entity Design:**
```java
@Entity
public class Asset extends BaseEntity {
    private String assetTag;
    private String type;
    private String condition;
    private boolean checkedOut;
    @ManyToOne private Employee assignedTo;
}

@Entity
public class AssetHistory extends BaseEntity {
    @ManyToOne private Asset asset;
    @ManyToOne private Employee employee;
    private LocalDateTime checkedOutAt;
    private LocalDateTime returnedAt;
}
```

**Service Layer:**
```java
public interface AssetService {
    AssetDTO assignAsset(Long assetId, Long employeeId);
    void returnAsset(Long assetId);
    List<AssetHistoryDTO> getAssetHistory(Long assetId);
    void validateCertification(Long employeeId, String assetType);
}
```

**Business Logic:**
```java
public void assignAsset(Long assetId, Long employeeId) {
    Asset asset = assetRepository.findById(assetId).orElseThrow();
    if (!certificationService.hasValidCertification(employeeId, asset.getType())) {
        throw new AssetAssignmentException("Certification required");
    }
    asset.setAssignedTo(employee);
    asset.setCheckedOut(true);
}
```

**REST Endpoints:**
- POST /api/assets/{assetId}/assign/{employeeId}
- POST /api/assets/{assetId}/return
- GET /api/assets/{assetId}/history

---

### E10: Performance Reviews & Goals

**Entity Design:**
```java
@Entity
public class PerformanceReview extends BaseEntity {
    @ManyToOne private Employee employee;
    private String cycle;
    @Lob private String goals;
    @Lob private String competencies;
    private String ratings;
    @Lob private String comments;
    private boolean acknowledgedByEmployee;
    private boolean acknowledgedBySupervisor;
    private LocalDateTime lockedAt;
}
```

**Service Layer:**
```java
public interface PerformanceService {
    PerformanceReviewDTO createReview(PerformanceReviewDTO dto);
    void acknowledgeReview(Long reviewId, boolean byEmployee);
    List<PerformanceReviewDTO> getReviews(Long employeeId);
    byte[] exportReviewPDF(Long reviewId);
}
```

**Business Logic:**
```java
public void acknowledgeReview(Long reviewId, boolean byEmployee) {
    PerformanceReview review = reviewRepository.findById(reviewId).orElseThrow();
    if (review.getLockedAt() != null) {
        throw new ReviewLockedException("Review is locked");
    }
    if (byEmployee) {
        review.setAcknowledgedByEmployee(true);
    } else {
        review.setAcknowledgedBySupervisor(true);
    }
    if (review.isAcknowledgedByEmployee() && review.isAcknowledgedBySupervisor()) {
        review.setLockedAt(Instant.now());
    }
}
```

**REST Endpoints:**
- POST /api/performance
- POST /api/performance/{id}/acknowledge
- GET /api/performance/employee/{employeeId}
- GET /api/performance/{id}/pdf
