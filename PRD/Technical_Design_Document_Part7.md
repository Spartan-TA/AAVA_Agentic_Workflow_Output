# Technical Design Document â Part 7 (User Stories 31-35)

---

Section: E09-Equipment & Asset Assignment (User Story 31)

Description: Continuation of Equipment & Asset Assignment, focusing on history log visibility per asset and employee, and overdue return reports.

Design Specification:
- **Entities:**
  - `AssetHistory` (id, assetId, employeeId, action, timestamp, notes)
- **Repositories:**
  - `AssetHistoryRepository`
- **Services:**
  - `AssetHistoryService` (log actions, retrieve history)
- **Integration:**
  - Reports for overdue returns, asset usage.

Sample Implementation:
```java
@Entity
public class AssetHistory {
    @Id @GeneratedValue
    private Long id;
    private Long assetId;
    private Long employeeId;
    private String action; // CHECKOUT, RETURN, MAINTENANCE
    private LocalDateTime timestamp;
    private String notes;
}

@Service
public class AssetHistoryService {
    public void logAction(Long assetId, Long employeeId, String action, String notes) { ... }
    public List<AssetHistory> getHistoryByAsset(Long assetId) { ... }
}
```

---

Section: E10-Performance Reviews & Goals (User Stories 32-34)

Description: This module manages quarterly/annual review templates, tracks goals, competencies, ratings, comments, and supports supervisor/employee acknowledgements and immutable history after sign-off.

Design Specification:
- **Package Structure:**
  - `com.company.wms.performance` (domain, repository, service, controller, dto)
- **Entities:**
  - `PerformanceReview` (id, employeeId, cycle, goals, competencies, ratings, comments, supervisorId, employeeAck, supervisorAck, status, createdAt, signedOffAt)
  - `ReviewTemplate` (id, name, fields, active)
- **Repositories:**
  - `PerformanceReviewRepository`, `ReviewTemplateRepository`
- **Services:**
  - `PerformanceReviewService` (create cycles, assign, submit, acknowledge, export PDF)
- **Controllers:**
  - `PerformanceReviewController` (REST endpoints for CRUD, workflow, export)
- **Security:**
  - Only SUPERVISOR/ADMIN can create/assign; WORKER can acknowledge/view own.
- **Integration:**
  - PDF export; immutable after sign-off.

Sample Implementation:
```java
@Entity
public class PerformanceReview {
    @Id @GeneratedValue
    private Long id;
    private Long employeeId;
    private String cycle; // Q1-2024, 2024-Annual
    @ElementCollection
    private List<String> goals;
    @ElementCollection
    private List<String> competencies;
    @ElementCollection
    private List<Integer> ratings;
    private String comments;
    private Long supervisorId;
    private Boolean employeeAck;
    private Boolean supervisorAck;
    private String status; // DRAFT, SUBMITTED, SIGNED_OFF
    private LocalDateTime createdAt;
    private LocalDateTime signedOffAt;
}

@RestController
@RequestMapping("/api/performance/reviews")
public class PerformanceReviewController {
    @PostMapping
    @PreAuthorize("hasRole('SUPERVISOR') or hasRole('ADMIN')")
    public PerformanceReview createReview(@RequestBody PerformanceReviewDto dto) { ... }

    @PostMapping("/{id}/acknowledge")
    @PreAuthorize("hasRole('WORKER')")
    public PerformanceReview acknowledgeReview(@PathVariable Long id) { ... }
}
```

---

Section: E11-Payroll Export Integration (User Story 35)

Description: This module generates payroll-ready files from approved attendance and leave, maps to external payroll provider formats, and supports secure delivery (SFTP/API) with audit logging.

Design Specification:
- **Package Structure:**
  - `com.company.wms.payroll` (domain, repository, service, controller, dto)
- **Entities:**
  - `PayrollExport` (id, period, fileUrl, status, createdAt, deliveredAt, attempts, errorLog)
- **Repositories:**
  - `PayrollExportRepository`
- **Services:**
  - `PayrollExportService` (generate export, map schema, deliver, retry, audit)
- **Controllers:**
  - `PayrollExportController` (REST endpoints for export, status)
- **Security:**
  - Only ADMIN/HR can trigger export.
- **Integration:**
  - SFTP/API delivery; audit log for every export.

Sample Implementation:
```java
@Entity
public class PayrollExport {
    @Id @GeneratedValue
    private Long id;
    private String period;
    private String fileUrl;
    private String status; // PENDING, DELIVERED, FAILED
    private LocalDateTime createdAt;
    private LocalDateTime deliveredAt;
    private Integer attempts;
    private String errorLog;
}

@RestController
@RequestMapping("/api/payroll/export")
public class PayrollExportController {
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public PayrollExport triggerExport(@RequestBody PayrollExportRequestDto dto) { ... }
}
```
