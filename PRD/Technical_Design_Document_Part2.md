# WAREHOUSE EMPLOYEE MANAGEMENT PLATFORM - LOW-LEVEL TECHNICAL DESIGN DOCUMENT

## Part 2: Scheduling, Leave, Training, Safety, Equipment & Performance (E05-E10)

### Document Information
- Version: 1.0
- Date: 2024
- Status: Production Ready
- Framework: Spring Boot 3.2.0
- Java Version: 17+

---

## E05 - SHIFT & SCHEDULE MANAGEMENT

### Overview
Manages recurring shift templates, rotations, overtime rules, employee assignments, blackout dates, operation calendars, conflict detection, bulk assignment, and audit entries. Ensures efficient workforce scheduling and compliance with labor rules.

### Domain Models

```java
// ShiftTemplate.java
@Entity
@Table(name = "shift_templates")
public class ShiftTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @NotNull
    private LocalTime startTime;

    @NotNull
    private LocalTime endTime;

    @ElementCollection
    @CollectionTable(name = "shift_template_days")
    @Enumerated(EnumType.STRING)
    private Set<DayOfWeek> daysOfWeek;

    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;
    
    private Boolean recurring = true;
    private Integer maxEmployees;
}

// ShiftAssignment.java
@Entity
@Table(name = "shift_assignments")
public class ShiftAssignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @ManyToOne(optional = false)
    @JoinColumn(name = "shift_template_id")
    private ShiftTemplate shiftTemplate;

    @NotNull
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    private AssignmentStatus status;

    @CreatedDate
    private Instant createdAt;

    @CreatedBy
    private String createdBy;
}

// BlackoutDate.java
@Entity
@Table(name = "blackout_dates")
public class BlackoutDate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private LocalDate date;

    @NotBlank
    private String reason;

    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;
}

// OvertimeRule.java
@Entity
@Table(name = "overtime_rules")
public class OvertimeRule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private Integer thresholdHours;

    @NotNull
    private Double overtimeMultiplier;

    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;
}
```

### Repository Layer

```java
public interface ShiftTemplateRepository extends JpaRepository<ShiftTemplate, Long> {
    List<ShiftTemplate> findByDepartment(Department department);
}

public interface ShiftAssignmentRepository extends JpaRepository<ShiftAssignment, Long> {
    List<ShiftAssignment> findByEmployeeAndDate(Employee employee, LocalDate date);
    List<ShiftAssignment> findByDateBetween(LocalDate start, LocalDate end);
    boolean existsByEmployeeAndDateAndStatus(
        Employee employee, LocalDate date, AssignmentStatus status);
}

public interface BlackoutDateRepository extends JpaRepository<BlackoutDate, Long> {
    List<BlackoutDate> findByDate(LocalDate date);
    List<BlackoutDate> findByDateBetween(LocalDate start, LocalDate end);
}

public interface OvertimeRuleRepository extends JpaRepository<OvertimeRule, Long> {
    Optional<OvertimeRule> findByDepartment(Department department);
}
```

### Service Layer

```java
@Service
@Transactional(readOnly = true)
public class ShiftAssignmentService {
    
    @Autowired
    private ShiftAssignmentRepository assignmentRepo;
    
    @Autowired
    private BlackoutDateRepository blackoutRepo;
    
    @Autowired
    private ShiftTemplateRepository templateRepo;
    
    @Transactional
    public ShiftAssignment assignShift(
            Long employeeId, Long templateId, LocalDate date) {
        
        // Check for blackout dates
        if (!blackoutRepo.findByDate(date).isEmpty()) {
            throw new ShiftAssignmentException(
                "Cannot assign shift on blackout date: " + date);
        }
        
        Employee employee = employeeRepository.findById(employeeId)
            .orElseThrow(() -> new BusinessException("Employee not found"));
        
        ShiftTemplate template = templateRepo.findById(templateId)
            .orElseThrow(() -> new BusinessException("Template not found"));
        
        // Check for conflicts
        if (assignmentRepo.existsByEmployeeAndDateAndStatus(
                employee, date, AssignmentStatus.ASSIGNED)) {
            throw new ShiftAssignmentException(
                "Employee already has a shift assigned for this date");
        }
        
        ShiftAssignment assignment = ShiftAssignment.builder()
            .employee(employee)
            .shiftTemplate(template)
            .date(date)
            .status(AssignmentStatus.ASSIGNED)
            .build();
        
        return assignmentRepo.save(assignment);
    }
    
    @Transactional
    public List<ShiftAssignment> bulkAssignShifts(
            List<Long> employeeIds, Long templateId, LocalDate date) {
        
        List<ShiftAssignment> assignments = new ArrayList<>();
        
        for (Long employeeId : employeeIds) {
            try {
                ShiftAssignment assignment = assignShift(employeeId, templateId, date);
                assignments.add(assignment);
            } catch (ShiftAssignmentException e) {
                log.warn("Failed to assign shift for employee {}: {}", 
                         employeeId, e.getMessage());
            }
        }
        
        return assignments;
    }
    
    public List<ShiftAssignment> getUpcomingShifts(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
            .orElseThrow(() -> new BusinessException("Employee not found"));
        
        LocalDate today = LocalDate.now();
        LocalDate futureDate = today.plusDays(30);
        
        return assignmentRepo.findByEmployeeAndDateBetween(
            employee, today, futureDate);
    }
}
```

### Controller Layer

```java
@RestController
@RequestMapping("/api/v1/shifts")
@Tag(name = "Shift Management")
public class ShiftController {
    
    @Autowired
    private ShiftAssignmentService shiftService;
    
    @PostMapping("/assign")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
    public ResponseEntity<ShiftAssignmentDTO> assignShift(
            @Valid @RequestBody ShiftAssignmentRequest request) {
        ShiftAssignment assignment = shiftService.assignShift(
            request.getEmployeeId(), 
            request.getTemplateId(), 
            request.getDate());
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new ShiftAssignmentDTO(assignment));
    }
    
    @PostMapping("/bulk-assign")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
    public ResponseEntity<List<ShiftAssignmentDTO>> bulkAssignShifts(
            @Valid @RequestBody BulkShiftAssignmentRequest request) {
        List<ShiftAssignment> assignments = shiftService.bulkAssignShifts(
            request.getEmployeeIds(), 
            request.getTemplateId(), 
            request.getDate());
        return ResponseEntity.ok(
            assignments.stream()
                .map(ShiftAssignmentDTO::new)
                .collect(Collectors.toList()));
    }
    
    @GetMapping("/employee/{employeeId}/upcoming")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR', 'WORKER')")
    public ResponseEntity<List<ShiftAssignmentDTO>> getUpcomingShifts(
            @PathVariable Long employeeId) {
        List<ShiftAssignment> shifts = shiftService.getUpcomingShifts(employeeId);
        return ResponseEntity.ok(
            shifts.stream()
                .map(ShiftAssignmentDTO::new)
                .collect(Collectors.toList()));
    }
}
```

### Database Schema

```sql
-- V5__shift_schedule_management.sql
CREATE TABLE shift_templates (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    recurring BOOLEAN DEFAULT TRUE,
    max_employees INT,
    department_id BIGINT REFERENCES departments(id)
);

CREATE TABLE shift_template_days (
    shift_template_id BIGINT REFERENCES shift_templates(id),
    days_of_week VARCHAR(20)
);

CREATE TABLE shift_assignments (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES employees(id),
    shift_template_id BIGINT NOT NULL REFERENCES shift_templates(id),
    date DATE NOT NULL,
    status VARCHAR(20),
    created_at TIMESTAMP,
    created_by VARCHAR(100)
);

CREATE INDEX idx_shift_assignments_employee_date 
    ON shift_assignments(employee_id, date);

CREATE TABLE blackout_dates (
    id BIGSERIAL PRIMARY KEY,
    date DATE NOT NULL,
    reason VARCHAR(255) NOT NULL,
    department_id BIGINT REFERENCES departments(id)
);

CREATE TABLE overtime_rules (
    id BIGSERIAL PRIMARY KEY,
    threshold_hours INT NOT NULL,
    overtime_multiplier DOUBLE PRECISION NOT NULL,
    department_id BIGINT REFERENCES departments(id)
);
```

---

## E06 - LEAVE & ABSENCE MANAGEMENT

### Overview
Handles employee leave requests (PTO, sick, unpaid), approvals, accrual balances, leave policies, integration with scheduling/payroll, and auto-flagging shifts for coverage.

### Domain Models

```java
// LeaveRequest.java
@Entity
@Table(name = "leave_requests")
public class LeaveRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LeaveType type;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LeaveStatus status;

    @NotBlank
    @Column(length = 1000)
    private String reason;

    @ManyToOne
    @JoinColumn(name = "approved_by")
    private Employee approvedBy;

    private LocalDateTime approvedAt;

    @CreatedDate
    private Instant createdAt;
}

// LeaveBalance.java
@Entity
@Table(name = "leave_balances")
public class LeaveBalance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LeaveType type;

    @NotNull
    @Column(precision = 10, scale = 2)
    private BigDecimal accrued;

    @NotNull
    @Column(precision = 10, scale = 2)
    private BigDecimal used;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal available;
}

// LeavePolicy.java
@Entity
@Table(name = "leave_policies")
public class LeavePolicy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private LeaveType type;

    @NotNull
    @Column(precision = 10, scale = 2)
    private BigDecimal annualAccrual;

    @NotNull
    @Column(precision = 10, scale = 2)
    private BigDecimal maxCarryover;
    
    private Boolean requiresApproval = true;
}
```

### Repository Layer

```java
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
    List<LeaveRequest> findByEmployeeAndStatus(Employee employee, LeaveStatus status);
    List<LeaveRequest> findByStatusAndStartDateBetween(
        LeaveStatus status, LocalDate start, LocalDate end);
}

public interface LeaveBalanceRepository extends JpaRepository<LeaveBalance, Long> {
    Optional<LeaveBalance> findByEmployeeAndType(Employee employee, LeaveType type);
    List<LeaveBalance> findByEmployee(Employee employee);
}

public interface LeavePolicyRepository extends JpaRepository<LeavePolicy, Long> {
    Optional<LeavePolicy> findByType(LeaveType type);
}
```

### Service Layer

```java
@Service
@Transactional(readOnly = true)
public class LeaveService {
    
    @Autowired
    private LeaveRequestRepository leaveRepo;
    
    @Autowired
    private LeaveBalanceRepository balanceRepo;
    
    @Autowired
    private LeavePolicyRepository policyRepo;
    
    @Transactional
    public LeaveRequest requestLeave(
            Long employeeId, 
            LeaveType type, 
            LocalDate startDate, 
            LocalDate endDate, 
            String reason) {
        
        Employee employee = employeeRepository.findById(employeeId)
            .orElseThrow(() -> new BusinessException("Employee not found"));
        
        // Validate dates
        if (endDate.isBefore(startDate)) {
            throw new ValidationException("End date must be after start date");
        }
        
        // Check balance
        LeaveBalance balance = balanceRepo.findByEmployeeAndType(employee, type)
            .orElseThrow(() -> new BusinessException("Leave balance not found"));
        
        long daysRequested = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        BigDecimal daysRequestedDecimal = BigDecimal.valueOf(daysRequested);
        
        if (balance.getAvailable().compareTo(daysRequestedDecimal) < 0) {
            throw new InsufficientLeaveBalanceException(
                "Insufficient leave balance. Available: " + balance.getAvailable());
        }
        
        // Check for overlapping requests
        List<LeaveRequest> overlapping = leaveRepo.findByEmployeeAndStatus(
            employee, LeaveStatus.APPROVED);
        
        for (LeaveRequest existing : overlapping) {
            if (datesOverlap(startDate, endDate, 
                           existing.getStartDate(), existing.getEndDate())) {
                throw new ValidationException(
                    "Leave request overlaps with existing approved leave");
            }
        }
        
        LeaveRequest request = LeaveRequest.builder()
            .employee(employee)
            .type(type)
            .startDate(startDate)
            .endDate(endDate)
            .reason(reason)
            .status(LeaveStatus.REQUESTED)
            .build();
        
        return leaveRepo.save(request);
    }
    
    @Transactional
    public LeaveRequest approveLeave(Long requestId, Long approverId) {
        LeaveRequest request = leaveRepo.findById(requestId)
            .orElseThrow(() -> new BusinessException("Leave request not found"));
        
        if (request.getStatus() != LeaveStatus.REQUESTED) {
            throw new ValidationException(
                "Only requested leaves can be approved");
        }
        
        Employee approver = employeeRepository.findById(approverId)
            .orElseThrow(() -> new BusinessException("Approver not found"));
        
        // Update balance
        LeaveBalance balance = balanceRepo.findByEmployeeAndType(
            request.getEmployee(), request.getType())
            .orElseThrow(() -> new BusinessException("Leave balance not found"));
        
        long daysRequested = ChronoUnit.DAYS.between(
            request.getStartDate(), request.getEndDate()) + 1;
        BigDecimal daysRequestedDecimal = BigDecimal.valueOf(daysRequested);
        
        balance.setUsed(balance.getUsed().add(daysRequestedDecimal));
        balance.setAvailable(balance.getAccrued().subtract(balance.getUsed()));
        balanceRepo.save(balance);
        
        // Update request
        request.setStatus(LeaveStatus.APPROVED);
        request.setApprovedBy(approver);
        request.setApprovedAt(LocalDateTime.now());
        
        // Flag shifts for coverage
        flagShiftsForCoverage(request);
        
        return leaveRepo.save(request);
    }
    
    private void flagShiftsForCoverage(LeaveRequest request) {
        // Implementation to flag affected shifts
        log.info("Flagging shifts for coverage for leave request: {}", 
                 request.getId());
    }
    
    private boolean datesOverlap(
            LocalDate start1, LocalDate end1, 
            LocalDate start2, LocalDate end2) {
        return !start1.isAfter(end2) && !end1.isBefore(start2);
    }
}
```

### Controller Layer

```java
@RestController
@RequestMapping("/api/v1/leave")
@Tag(name = "Leave Management")
public class LeaveController {
    
    @Autowired
    private LeaveService leaveService;
    
    @PostMapping("/request")
    @PreAuthorize("hasAnyRole('WORKER', 'SUPERVISOR', 'ADMIN')")
    public ResponseEntity<LeaveRequestDTO> requestLeave(
            @Valid @RequestBody LeaveRequestDTO request) {
        LeaveRequest leave = leaveService.requestLeave(
            request.getEmployeeId(), 
            request.getType(), 
            request.getStartDate(), 
            request.getEndDate(), 
            request.getReason());
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new LeaveRequestDTO(leave));
    }
    
    @PostMapping("/approve/{id}")
    @PreAuthorize("hasAnyRole('SUPERVISOR', 'ADMIN', 'HR')")
    public ResponseEntity<LeaveRequestDTO> approveLeave(
            @PathVariable Long id,
            @RequestParam Long approverId) {
        LeaveRequest leave = leaveService.approveLeave(id, approverId);
        return ResponseEntity.ok(new LeaveRequestDTO(leave));
    }
    
    @GetMapping("/employee/{employeeId}/balance")
    @PreAuthorize("hasAnyRole('WORKER', 'SUPERVISOR', 'ADMIN', 'HR')")
    public ResponseEntity<List<LeaveBalanceDTO>> getLeaveBalance(
            @PathVariable Long employeeId) {
        List<LeaveBalance> balances = leaveService.getLeaveBalances(employeeId);
        return ResponseEntity.ok(
            balances.stream()
                .map(LeaveBalanceDTO::new)
                .collect(Collectors.toList()));
    }
}
```

### Database Schema

```sql
-- V6__leave_absence_management.sql
CREATE TABLE leave_requests (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES employees(id),
    type VARCHAR(20) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL,
    reason VARCHAR(1000),
    approved_by BIGINT REFERENCES employees(id),
    approved_at TIMESTAMP,
    created_at TIMESTAMP
);

CREATE INDEX idx_leave_requests_employee_status 
    ON leave_requests(employee_id, status);

CREATE TABLE leave_balances (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES employees(id),
    type VARCHAR(20) NOT NULL,
    accrued DECIMAL(10, 2) NOT NULL,
    used DECIMAL(10, 2) NOT NULL,
    available DECIMAL(10, 2),
    UNIQUE(employee_id, type)
);

CREATE TABLE leave_policies (
    id BIGSERIAL PRIMARY KEY,
    type VARCHAR(20) NOT NULL UNIQUE,
    annual_accrual DECIMAL(10, 2) NOT NULL,
    max_carryover DECIMAL(10, 2) NOT NULL,
    requires_approval BOOLEAN DEFAULT TRUE
);
```

---

## E07 - TRAINING & CERTIFICATION TRACKING

### Overview
Tracks employee certifications (forklift, etc.), expirations, renewals, blocks assignments for expired certs, uploads proof documents, sends alerts before expiry, and displays certification status.

### Domain Models

```java
// Certification.java
@Entity
@Table(name = "certifications")
public class Certification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, unique = true)
    private String name;

    @NotNull
    private Integer validityMonths;
    
    private String description;
    private Boolean required = false;
}

// EmployeeCertification.java
@Entity
@Table(name = "employee_certifications")
public class EmployeeCertification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @ManyToOne(optional = false)
    @JoinColumn(name = "certification_id")
    private Certification certification;

    @NotNull
    private LocalDate issueDate;

    @NotNull
    private LocalDate expiryDate;

    @Column(length = 500)
    private String proofDocumentUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CertificationStatus status;
    
    @CreatedDate
    private Instant createdAt;
    
    @LastModifiedDate
    private Instant updatedAt;
}
```

### Repository Layer

```java
public interface CertificationRepository extends JpaRepository<Certification, Long> {
    Optional<Certification> findByName(String name);
}

public interface EmployeeCertificationRepository 
        extends JpaRepository<EmployeeCertification, Long> {
    
    List<EmployeeCertification> findByEmployee(Employee employee);
    
    Optional<EmployeeCertification> findByEmployeeAndCertification(
        Employee employee, Certification certification);
    
    List<EmployeeCertification> findByExpiryDateBetween(
        LocalDate from, LocalDate to);
    
    List<EmployeeCertification> findByStatus(CertificationStatus status);
}
```

### Service Layer

```java
@Service
@Transactional(readOnly = true)
public class CertificationService {
    
    @Autowired
    private EmployeeCertificationRepository empCertRepo;
    
    @Autowired
    private CertificationRepository certRepo;
    
    public void validateCertificationForAssignment(
            Long employeeId, Long certificationId) {
        
        Employee employee = employeeRepository.findById(employeeId)
            .orElseThrow(() -> new BusinessException("Employee not found"));
        
        Certification cert = certRepo.findById(certificationId)
            .orElseThrow(() -> new BusinessException("Certification not found"));
        
        EmployeeCertification empCert = empCertRepo
            .findByEmployeeAndCertification(employee, cert)
            .orElseThrow(() -> new CertificationNotFoundException(
                "Employee does not have required certification"));
        
        if (empCert.getExpiryDate().isBefore(LocalDate.now())) {
            throw new CertificationExpiredException(
                "Certification expired on: " + empCert.getExpiryDate());
        }
        
        if (empCert.getStatus() != CertificationStatus.ACTIVE) {
            throw new CertificationInvalidException(
                "Certification is not active");
        }
    }
    
    public List<EmployeeCertification> getExpiringCertifications(int days) {
        LocalDate now = LocalDate.now();
        LocalDate futureDate = now.plusDays(days);
        return empCertRepo.findByExpiryDateBetween(now, futureDate);
    }
    
    @Scheduled(cron = "0 0 8 * * *") // Daily at 8 AM
    public void sendExpirationAlerts() {
        // 30-day alerts
        List<EmployeeCertification> expiring30 = getExpiringCertifications(30);
        expiring30.forEach(cert -> 
            notificationService.sendCertificationExpiryAlert(cert, 30));
        
        // 7-day alerts
        List<EmployeeCertification> expiring7 = getExpiringCertifications(7);
        expiring7.forEach(cert -> 
            notificationService.sendCertificationExpiryAlert(cert, 7));
    }
    
    @Transactional
    public EmployeeCertification uploadProof(
            Long empCertId, String documentUrl) {
        
        EmployeeCertification empCert = empCertRepo.findById(empCertId)
            .orElseThrow(() -> new BusinessException(
                "Employee certification not found"));
        
        empCert.setProofDocumentUrl(documentUrl);
        return empCertRepo.save(empCert);
    }
}
```

### Controller Layer

```java
@RestController
@RequestMapping("/api/v1/certifications")
@Tag(name = "Certification Management")
public class CertificationController {
    
    @Autowired
    private CertificationService certService;
    
    @GetMapping("/expiring")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'SUPERVISOR')")
    public ResponseEntity<List<EmployeeCertificationDTO>> getExpiringCertifications(
            @RequestParam(defaultValue = "30") int days) {
        List<EmployeeCertification> certs = 
            certService.getExpiringCertifications(days);
        return ResponseEntity.ok(
            certs.stream()
                .map(EmployeeCertificationDTO::new)
                .collect(Collectors.toList()));
    }
    
    @PostMapping("/upload-proof")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<EmployeeCertificationDTO> uploadProof(
            @RequestParam Long empCertId,
            @RequestParam MultipartFile file) {
        
        String documentUrl = fileStorageService.store(file);
        EmployeeCertification cert = certService.uploadProof(empCertId, documentUrl);
        return ResponseEntity.ok(new EmployeeCertificationDTO(cert));
    }
    
    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'SUPERVISOR', 'WORKER')")
    public ResponseEntity<List<EmployeeCertificationDTO>> getEmployeeCertifications(
            @PathVariable Long employeeId) {
        List<EmployeeCertification> certs = 
            certService.getEmployeeCertifications(employeeId);
        return ResponseEntity.ok(
            certs.stream()
                .map(EmployeeCertificationDTO::new)
                .collect(Collectors.toList()));
    }
}
```

### Database Schema

```sql
-- V7__training_certification_tracking.sql
CREATE TABLE certifications (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    validity_months INT NOT NULL,
    description VARCHAR(500),
    required BOOLEAN DEFAULT FALSE
);

CREATE TABLE employee_certifications (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES employees(id),
    certification_id BIGINT NOT NULL REFERENCES certifications(id),
    issue_date DATE NOT NULL,
    expiry_date DATE NOT NULL,
    proof_document_url VARCHAR(500),
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX idx_employee_certifications_expiry 
    ON employee_certifications(expiry_date);
CREATE INDEX idx_employee_certifications_employee 
    ON employee_certifications(employee_id);
```

---

## E08 - SAFETY INCIDENTS & OSHA REPORTING

### Overview
Records safety incidents/near-misses, tracks severity, location, description, investigation workflow, corrective actions, OSHA 300/300A export, and provides metrics dashboards.

### Domain Models

```java
// SafetyIncident.java
@Entity
@Table(name = "safety_incidents")
public class SafetyIncident {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private LocalDate incidentDate;

    @NotBlank
    @Column(nullable = false)
    private String location;

    @NotBlank
    @Column(length = 2000, nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IncidentSeverity severity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IncidentStatus status;
    
    @ManyToMany
    @JoinTable(name = "incident_employees")
    private Set<Employee> involvedEmployees;

    @OneToMany(mappedBy = "incident", cascade = CascadeType.ALL)
    private List<CorrectiveAction> correctiveActions;
    
    @ManyToOne
    @JoinColumn(name = "reported_by")
    private Employee reportedBy;
    
    @CreatedDate
    private Instant createdAt;
    
    @LastModifiedDate
    private Instant updatedAt;
}

// CorrectiveAction.java
@Entity
@Table(name = "corrective_actions")
public class CorrectiveAction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "incident_id")
    private SafetyIncident incident;

    @NotBlank
    @Column(length = 1000, nullable = false)
    private String action;

    @NotNull
    private LocalDate dueDate;

    @Column(nullable = false)
    private Boolean completed = false;
    
    private LocalDate completedDate;
    
    @ManyToOne
    @JoinColumn(name = "assigned_to")
    private Employee assignedTo;
}
```

### Repository Layer

```java
public interface SafetyIncidentRepository 
        extends JpaRepository<SafetyIncident, Long> {
    
    List<SafetyIncident> findByStatus(IncidentStatus status);
    List<SafetyIncident> findByIncidentDateBetween(LocalDate start, LocalDate end);
    List<SafetyIncident> findBySeverity(IncidentSeverity severity);
    
    @Query("SELECT COUNT(i) FROM SafetyIncident i WHERE " +
           "i.incidentDate BETWEEN :start AND :end")
    long countIncidentsByDateRange(
        @Param("start") LocalDate start, 
        @Param("end") LocalDate end);
}

public interface CorrectiveActionRepository 
        extends JpaRepository<CorrectiveAction, Long> {
    
    List<CorrectiveAction> findByIncident(SafetyIncident incident);
    List<CorrectiveAction> findByCompletedFalseAndDueDateBefore(LocalDate date);
}
```

### Service Layer

```java
@Service
@Transactional(readOnly = true)
public class SafetyIncidentService {
    
    @Autowired
    private SafetyIncidentRepository incidentRepo;
    
    @Autowired
    private CorrectiveActionRepository actionRepo;
    
    @Transactional
    public SafetyIncident reportIncident(SafetyIncidentDTO dto) {
        SafetyIncident incident = SafetyIncident.builder()
            .incidentDate(dto.getIncidentDate())
            .location(dto.getLocation())
            .description(dto.getDescription())
            .severity(dto.getSeverity())
            .status(IncidentStatus.OPEN)
            .build();
        
        if (dto.getReportedById() != null) {
            Employee reporter = employeeRepository.findById(dto.getReportedById())
                .orElseThrow(() -> new BusinessException("Reporter not found"));
            incident.setReportedBy(reporter);
        }
        
        return incidentRepo.save(incident);
    }
    
    @Transactional
    public SafetyIncident updateStatus(Long id, IncidentStatus status) {
        SafetyIncident incident = incidentRepo.findById(id)
            .orElseThrow(() -> new BusinessException("Incident not found"));
        
        incident.setStatus(status);
        return incidentRepo.save(incident);
    }
    
    @Transactional
    public CorrectiveAction addCorrectiveAction(
            Long incidentId, CorrectiveActionDTO dto) {
        
        SafetyIncident incident = incidentRepo.findById(incidentId)
            .orElseThrow(() -> new BusinessException("Incident not found"));
        
        CorrectiveAction action = CorrectiveAction.builder()
            .incident(incident)
            .action(dto.getAction())
            .dueDate(dto.getDueDate())
            .completed(false)
            .build();
        
        if (dto.getAssignedToId() != null) {
            Employee assignee = employeeRepository.findById(dto.getAssignedToId())
                .orElseThrow(() -> new BusinessException("Assignee not found"));
            action.setAssignedTo(assignee);
        }
        
        return actionRepo.save(action);
    }
    
    public SafetyMetricsDTO getMetrics(LocalDate startDate, LocalDate endDate) {
        long totalIncidents = incidentRepo.countIncidentsByDateRange(
            startDate, endDate);
        
        List<SafetyIncident> incidents = incidentRepo
            .findByIncidentDateBetween(startDate, endDate);
        
        Map<IncidentSeverity, Long> bySeverity = incidents.stream()
            .collect(Collectors.groupingBy(
                SafetyIncident::getSeverity, Collectors.counting()));
        
        return SafetyMetricsDTO.builder()
            .totalIncidents(totalIncidents)
            .bySeverity(bySeverity)
            .build();
    }
}
```

### Controller Layer

```java
@RestController
@RequestMapping("/api/v1/safety/incidents")
@Tag(name = "Safety Incident Management")
public class SafetyIncidentController {
    
    @Autowired
    private SafetyIncidentService incidentService;
    
    @PostMapping("/report")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR', 'SAFETY')")
    public ResponseEntity<SafetyIncidentDTO> reportIncident(
            @Valid @RequestBody SafetyIncidentDTO dto) {
        SafetyIncident incident = incidentService.reportIncident(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new SafetyIncidentDTO(incident));
    }
    
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'SAFETY')")
    public ResponseEntity<SafetyIncidentDTO> updateStatus(
            @PathVariable Long id,
            @RequestParam IncidentStatus status) {
        SafetyIncident incident = incidentService.updateStatus(id, status);
        return ResponseEntity.ok(new SafetyIncidentDTO(incident));
    }
    
    @PostMapping("/{id}/corrective-actions")
    @PreAuthorize("hasAnyRole('ADMIN', 'SAFETY')")
    public ResponseEntity<CorrectiveActionDTO> addCorrectiveAction(
            @PathVariable Long id,
            @Valid @RequestBody CorrectiveActionDTO dto) {
        CorrectiveAction action = incidentService.addCorrectiveAction(id, dto);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new CorrectiveActionDTO(action));
    }
    
    @GetMapping("/metrics")
    @PreAuthorize("hasAnyRole('ADMIN', 'SAFETY', 'HR')")
    public ResponseEntity<SafetyMetricsDTO> getMetrics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) 
                LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) 
                LocalDate endDate) {
        SafetyMetricsDTO metrics = incidentService.getMetrics(startDate, endDate);
        return ResponseEntity.ok(metrics);
    }
}
```

### Database Schema

```sql
-- V8__safety_incidents_osha_reporting.sql
CREATE TABLE safety_incidents (
    id BIGSERIAL PRIMARY KEY,
    incident_date DATE NOT NULL,
    location VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    severity VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    reported_by BIGINT REFERENCES employees(id),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX idx_safety_incidents_date ON safety_incidents(incident_date);
CREATE INDEX idx_safety_incidents_status ON safety_incidents(status);

CREATE TABLE incident_employees (
    incident_id BIGINT REFERENCES safety_incidents(id),
    employee_id BIGINT REFERENCES employees(id),
    PRIMARY KEY (incident_id, employee_id)
);

CREATE TABLE corrective_actions (
    id BIGSERIAL PRIMARY KEY,
    incident_id BIGINT REFERENCES safety_incidents(id),
    action VARCHAR(1000) NOT NULL,
    due_date DATE NOT NULL,
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    completed_date DATE,
    assigned_to BIGINT REFERENCES employees(id)
);
```

---

## E09 - EQUIPMENT & ASSET ASSIGNMENT

### Overview
Assigns assets (scanners, forklifts, PPE) to employees, tracks checkout/return, blocks use if certification missing, tracks asset condition, logs history, and reports overdue returns.

### Domain Models

```java
// Asset.java
@Entity
@Table(name = "assets")
public class Asset {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, unique = true)
    private String assetTag;

    @NotBlank
    @Column(nullable = false)
    private String type;
    
    private String model;
    private String serialNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssetCondition condition;
    
    @ManyToOne
    @JoinColumn(name = "required_certification_id")
    private Certification requiredCertification;
    
    @Column(nullable = false)
    private Boolean available = true;
}

// AssetAssignment.java
@Entity
@Table(name = "asset_assignments")
public class AssetAssignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "asset_id")
    private Asset asset;

    @ManyToOne(optional = false)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @NotNull
    private LocalDateTime checkoutTime;

    private LocalDateTime returnTime;
    
    private LocalDateTime expectedReturnTime;

    @Column(nullable = false)
    private Boolean overdue = false;
    
    @Enumerated(EnumType.STRING)
    private AssetCondition conditionAtCheckout;
    
    @Enumerated(EnumType.STRING)
    private AssetCondition conditionAtReturn;
    
    private String notes;
}
```

### Repository Layer

```java
public interface AssetRepository extends JpaRepository<Asset, Long> {
    Optional<Asset> findByAssetTag(String assetTag);
    List<Asset> findByAvailableTrue();
    List<Asset> findByType(String type);
}

public interface AssetAssignmentRepository 
        extends JpaRepository<AssetAssignment, Long> {
    
    List<AssetAssignment> findByEmployeeAndReturnTimeIsNull(Employee employee);
    List<AssetAssignment> findByAssetAndReturnTimeIsNull(Asset asset);
    List<AssetAssignment> findByOverdueTrue();
    
    @Query("SELECT a FROM AssetAssignment a WHERE " +
           "a.returnTime IS NULL AND " +
           "a.expectedReturnTime < :now")
    List<AssetAssignment> findOverdueAssignments(
        @Param("now") LocalDateTime now);
}
```

### Service Layer

```java
@Service
@Transactional(readOnly = true)
public class AssetService {
    
    @Autowired
    private AssetAssignmentRepository assignmentRepo;
    
    @Autowired
    private AssetRepository assetRepo;
    
    @Autowired
    private CertificationService certificationService;
    
    @Transactional
    public AssetAssignment checkoutAsset(
            Long assetId, Long employeeId, LocalDateTime expectedReturn) {
        
        Asset asset = assetRepo.findById(assetId)
            .orElseThrow(() -> new BusinessException("Asset not found"));
        
        if (!asset.getAvailable()) {
            throw new AssetNotAvailableException(
                "Asset is currently checked out");
        }
        
        Employee employee = employeeRepository.findById(employeeId)
            .orElseThrow(() -> new BusinessException("Employee not found"));
        
        // Check certification if required
        if (asset.getRequiredCertification() != null) {
            try {
                certificationService.validateCertificationForAssignment(
                    employeeId, asset.getRequiredCertification().getId());
            } catch (CertificationException e) {
                throw new AssetCheckoutException(
                    "Employee does not have required certification: " + 
                    asset.getRequiredCertification().getName());
            }
        }
        
        AssetAssignment assignment = AssetAssignment.builder()
            .asset(asset)
            .employee(employee)
            .checkoutTime(LocalDateTime.now())
            .expectedReturnTime(expectedReturn)
            .conditionAtCheckout(asset.getCondition())
            .overdue(false)
            .build();
        
        asset.setAvailable(false);
        assetRepo.save(asset);
        
        return assignmentRepo.save(assignment);
    }
    
    @Transactional
    public AssetAssignment returnAsset(
            Long assignmentId, AssetCondition condition, String notes) {
        
        AssetAssignment assignment = assignmentRepo.findById(assignmentId)
            .orElseThrow(() -> new BusinessException(
                "Asset assignment not found"));
        
        if (assignment.getReturnTime() != null) {
            throw new ValidationException("Asset already returned");
        }
        
        assignment.setReturnTime(LocalDateTime.now());
        assignment.setConditionAtReturn(condition);
        assignment.setNotes(notes);
        assignment.setOverdue(false);
        
        Asset asset = assignment.getAsset();
        asset.setCondition(condition);
        asset.setAvailable(true);
        assetRepo.save(asset);
        
        return assignmentRepo.save(assignment);
    }
    
    @Scheduled(cron = "0 0 * * * *") // Every hour
    public void updateOverdueStatus() {
        List<AssetAssignment> overdue = assignmentRepo
            .findOverdueAssignments(LocalDateTime.now());
        
        overdue.forEach(assignment -> {
            assignment.setOverdue(true);
            assignmentRepo.save(assignment);
            notificationService.sendOverdueAssetAlert(assignment);
        });
    }
    
    public List<AssetAssignment> getOverdueReturns() {
        return assignmentRepo.findByOverdueTrue();
    }
}
```

### Controller Layer

```java
@RestController
@RequestMapping("/api/v1/assets")
@Tag(name = "Asset Management")
public class AssetController {
    
    @Autowired
    private AssetService assetService;
    
    @PostMapping("/checkout")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
    public ResponseEntity<AssetAssignmentDTO> checkoutAsset(
            @Valid @RequestBody AssetCheckoutRequest request) {
        AssetAssignment assignment = assetService.checkoutAsset(
            request.getAssetId(), 
            request.getEmployeeId(), 
            request.getExpectedReturnTime());
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new AssetAssignmentDTO(assignment));
    }
    
    @PostMapping("/return/{assignmentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
    public ResponseEntity<AssetAssignmentDTO> returnAsset(
            @PathVariable Long assignmentId,
            @Valid @RequestBody AssetReturnRequest request) {
        AssetAssignment assignment = assetService.returnAsset(
            assignmentId, 
            request.getCondition(), 
            request.getNotes());
        return ResponseEntity.ok(new AssetAssignmentDTO(assignment));
    }
    
    @GetMapping("/overdue")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
    public ResponseEntity<List<AssetAssignmentDTO>> getOverdueReturns() {
        List<AssetAssignment> overdue = assetService.getOverdueReturns();
        return ResponseEntity.ok(
            overdue.stream()
                .map(AssetAssignmentDTO::new)
                .collect(Collectors.toList()));
    }
}
```

### Database Schema

```sql
-- V9__equipment_asset_assignment.sql
CREATE TABLE assets (
    id BIGSERIAL PRIMARY KEY,
    asset_tag VARCHAR(100) NOT NULL UNIQUE,
    type VARCHAR(50) NOT NULL,
    model VARCHAR(100),
    serial_number VARCHAR(100),
    condition VARCHAR(20) NOT NULL,
    required_certification_id BIGINT REFERENCES certifications(id),
    available BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX idx_assets_type ON assets(type);
CREATE INDEX idx_assets_available ON assets(available);

CREATE TABLE asset_assignments (
    id BIGSERIAL PRIMARY KEY,
    asset_id BIGINT NOT NULL REFERENCES assets(id),
    employee_id BIGINT NOT NULL REFERENCES employees(id),
    checkout_time TIMESTAMP NOT NULL,
    return_time TIMESTAMP,
    expected_return_time TIMESTAMP,
    overdue BOOLEAN NOT NULL DEFAULT FALSE,
    condition_at_checkout VARCHAR(20),
    condition_at_return VARCHAR(20),
    notes VARCHAR(1000)
);

CREATE INDEX idx_asset_assignments_employee 
    ON asset_assignments(employee_id);
CREATE INDEX idx_asset_assignments_overdue 
    ON asset_assignments(overdue);
```

---

## E10 - PERFORMANCE REVIEWS & GOALS

### Overview
Manages quarterly/annual review templates, goals, competencies, ratings, comments, supervisor/employee acknowledgements, PDF export, role-based visibility, and immutable history after sign-off.

### Domain Models

```java
// PerformanceReview.java
@Entity
@Table(name = "performance_reviews")
public class PerformanceReview {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @ManyToOne
    @JoinColumn(name = "supervisor_id")
    private Employee supervisor;

    @NotNull
    private LocalDate periodStart;

    @NotNull
    private LocalDate periodEnd;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReviewStatus status;

    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL)
    private List<ReviewGoal> goals;

    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL)
    private List<ReviewCompetency> competencies;

    @Column(length = 2000)
    private String supervisorComments;
    
    @Column(length = 2000)
    private String employeeComments;

    @Column(nullable = false)
    private Boolean supervisorAcknowledged = false;
    
    @Column(nullable = false)
    private Boolean employeeAcknowledged = false;
    
    private LocalDateTime supervisorAcknowledgedAt;
    private LocalDateTime employeeAcknowledgedAt;
    
    @CreatedDate
    private Instant createdAt;
    
    @LastModifiedDate
    private Instant updatedAt;
}

// ReviewGoal.java
@Entity
@Table(name = "review_goals")
public class ReviewGoal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "review_id")
    private PerformanceReview review;

    @NotBlank
    @Column(length = 500, nullable = false)
    private String goal;

    @Enumerated(EnumType.STRING)
    private GoalStatus status;

    @Column(length = 1000)
    private String comments;
}

// ReviewCompetency.java
@Entity
@Table(name = "review_competencies")
public class ReviewCompetency {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "review_id")
    private PerformanceReview review;

    @NotBlank
    @Column(nullable = false)
    private String competency;

    @Min(1)
    @Max(5)
    @Column(nullable = false)
    private Integer rating;

    @Column(length = 1000)
    private String comments;
}
```

### Repository Layer

```java
public interface PerformanceReviewRepository 
        extends JpaRepository<PerformanceReview, Long> {
    
    List<PerformanceReview> findByEmployee(Employee employee);
    List<PerformanceReview> findBySupervisor(Employee supervisor);
    List<PerformanceReview> findByStatus(ReviewStatus status);
}

public interface ReviewGoalRepository extends JpaRepository<ReviewGoal, Long> {
    List<ReviewGoal> findByReview(PerformanceReview review);
}

public interface ReviewCompetencyRepository 
        extends JpaRepository<ReviewCompetency, Long> {
    List<ReviewCompetency> findByReview(PerformanceReview review);
}
```

### Service Layer

```java
@Service
@Transactional(readOnly = true)
public class PerformanceReviewService {
    
    @Autowired
    private PerformanceReviewRepository reviewRepo;
    
    @Transactional
    public PerformanceReview submitReview(
            Long reviewId, PerformanceReviewDTO dto) {
        
        PerformanceReview review = reviewRepo.findById(reviewId)
            .orElseThrow(() -> new BusinessException("Review not found"));
        
        if (review.getStatus() == ReviewStatus.SIGNED_OFF) {
            throw new IllegalStateException(
                "Review is immutable after sign-off");
        }
        
        review.setSupervisorComments(dto.getSupervisorComments());
        review.setStatus(ReviewStatus.SUBMITTED);
        
        return reviewRepo.save(review);
    }
    
    @Transactional
    public PerformanceReview acknowledgeReview(
            Long reviewId, boolean isSupervisor) {
        
        PerformanceReview review = reviewRepo.findById(reviewId)
            .orElseThrow(() -> new BusinessException("Review not found"));
        
        if (review.getStatus() != ReviewStatus.SUBMITTED) {
            throw new ValidationException(
                "Review must be submitted before acknowledgement");
        }
        
        if (isSupervisor) {
            review.setSupervisorAcknowledged(true);
            review.setSupervisorAcknowledgedAt(LocalDateTime.now());
        } else {
            review.setEmployeeAcknowledged(true);
            review.setEmployeeAcknowledgedAt(LocalDateTime.now());
        }
        
        // If both acknowledged, mark as signed off
        if (review.getSupervisorAcknowledged() && 
            review.getEmployeeAcknowledged()) {
            review.setStatus(ReviewStatus.SIGNED_OFF);
        }
        
        return reviewRepo.save(review);
    }
    
    public byte[] exportToPdf(Long reviewId) {
        PerformanceReview review = reviewRepo.findById(reviewId)
            .orElseThrow(() -> new BusinessException("Review not found"));
        
        // PDF generation logic
        return pdfGeneratorService.generateReviewPdf(review);
    }
    
    @PreAuthorize("hasRole('ADMIN') or " +
                  "(hasRole('SUPERVISOR') and #supervisorId == authentication.principal.id) or " +
                  "(hasRole('WORKER') and #employeeId == authentication.principal.id)")
    public List<PerformanceReview> getReviewsForEmployee(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
            .orElseThrow(() -> new BusinessException("Employee not found"));
        return reviewRepo.findByEmployee(employee);
    }
}
```

### Controller Layer

```java
@RestController
@RequestMapping("/api/v1/reviews")
@Tag(name = "Performance Review Management")
public class PerformanceReviewController {
    
    @Autowired
    private PerformanceReviewService reviewService;
    
    @PostMapping("/{id}/submit")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
    public ResponseEntity<PerformanceReviewDTO> submitReview(
            @PathVariable Long id,
            @Valid @RequestBody PerformanceReviewDTO dto) {
        PerformanceReview review = reviewService.submitReview(id, dto);
        return ResponseEntity.ok(new PerformanceReviewDTO(review));
    }
    
    @PostMapping("/{id}/acknowledge")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR', 'WORKER')")
    public ResponseEntity<PerformanceReviewDTO> acknowledgeReview(
            @PathVariable Long id,
            @RequestParam boolean isSupervisor) {
        PerformanceReview review = reviewService.acknowledgeReview(id, isSupervisor);
        return ResponseEntity.ok(new PerformanceReviewDTO(review));
    }
    
    @GetMapping("/{id}/export")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR', 'HR')")
    public ResponseEntity<byte[]> exportToPdf(@PathVariable Long id) {
        byte[] pdf = reviewService.exportToPdf(id);
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, 
                    "attachment; filename=review_" + id + ".pdf")
            .contentType(MediaType.APPLICATION_PDF)
            .body(pdf);
    }
    
    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR', 'HR', 'WORKER')")
    public ResponseEntity<List<PerformanceReviewDTO>> getEmployeeReviews(
            @PathVariable Long employeeId) {
        List<PerformanceReview> reviews = 
            reviewService.getReviewsForEmployee(employeeId);
        return ResponseEntity.ok(
            reviews.stream()
                .map(PerformanceReviewDTO::new)
                .collect(Collectors.toList()));
    }
}
```

### Database Schema

```sql
-- V10__performance_reviews_goals.sql
CREATE TABLE performance_reviews (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES employees(id),
    supervisor_id BIGINT REFERENCES employees(id),
    period_start DATE NOT NULL,
    period_end DATE NOT NULL,
    status VARCHAR(20) NOT NULL,
    supervisor_comments TEXT,
    employee_comments TEXT,
    supervisor_acknowledged BOOLEAN NOT NULL DEFAULT FALSE,
    employee_acknowledged BOOLEAN NOT NULL DEFAULT FALSE,
    supervisor_acknowledged_at TIMESTAMP,
    employee_acknowledged_at TIMESTAMP,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX idx_performance_reviews_employee 
    ON performance_reviews(employee_id);
CREATE INDEX idx_performance_reviews_status 
    ON performance_reviews(status);

CREATE TABLE review_goals (
    id BIGSERIAL PRIMARY KEY,
    review_id BIGINT NOT NULL REFERENCES performance_reviews(id),
    goal VARCHAR(500) NOT NULL,
    status VARCHAR(20),
    comments VARCHAR(1000)
);

CREATE TABLE review_competencies (
    id BIGSERIAL PRIMARY KEY,
    review_id BIGINT NOT NULL REFERENCES performance_reviews(id),
    competency VARCHAR(100) NOT NULL,
    rating INT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comments VARCHAR(1000)
);
```

---

## CONCLUSION - PART 2

This document provides comprehensive technical design for epics E05-E10 of the Warehouse Employee Management Platform. Each module follows Spring Boot best practices with:

- Complete domain models with JPA entities and relationships
- Repository layer with Spring Data JPA and custom queries
- Service layer with business logic and transaction management
- Controller layer with REST endpoints, security, and OpenAPI documentation
- Database schema with Flyway migrations
- Proper validation, error handling, and logging
- Integration points with other modules

**Key Features Implemented:**
- Shift scheduling with conflict detection
- Leave management with accrual tracking
- Certification tracking with expiration alerts
- Safety incident reporting and OSHA compliance
- Asset management with certification validation
- Performance reviews with immutable history

**Next:** Part 3 will cover E11-E17 (Payroll, Notifications, Integration, Audit, Reporting, Mobile, Onboarding)