# Technical Design Document â Part 5 (User Stories 21-25)

---

Section: E06-Leave & Absence Management (User Story 21-22)

Description: Continuation of Leave & Absence Management, focusing on accrual balances, policies, and integration hooks to exclude from scheduling and payroll hours.

Design Specification:
- **Entities:**
  - `AccrualPolicy` (id, leaveType, accrualRate, maxBalance, carryOver, effectiveDate)
- **Repositories:**
  - `AccrualPolicyRepository`
- **Services:**
  - `LeaveAccrualService` (calculate accruals, enforce policies, update balances)
- **Integration:**
  - Exclude approved leaves from scheduling and payroll calculations.

Sample Implementation:
```java
@Entity
public class AccrualPolicy {
    @Id @GeneratedValue
    private Long id;
    private String leaveType;
    private BigDecimal accrualRate; // days/month
    private Integer maxBalance;
    private Boolean carryOver;
    private LocalDate effectiveDate;
}

@Service
public class LeaveAccrualService {
    public void accrueLeave(Long employeeId, String leaveType) { ... }
    public void enforcePolicy(Long employeeId, String leaveType) { ... }
}
```

---

Section: E07-Training & Certification Tracking (User Stories 23-25)

Description: This module tracks required certifications (e.g., forklift), expirations, renewals, and blocks assignment to tasks requiring expired certifications. It supports uploading proof documents and alerts for expiring certifications.

Design Specification:
- **Package Structure:**
  - `com.company.wms.certification` (domain, repository, service, controller, dto)
- **Entities:**
  - `Certification` (id, employeeId, type, issueDate, expiryDate, status, documentUrl)
  - `CertificationRequirement` (id, role, certType, required)
- **Repositories:**
  - `CertificationRepository`, `CertificationRequirementRepository`
- **Services:**
  - `CertificationService` (CRUD, expiry alerts, scheduling checks, upload proof)
- **Controllers:**
  - `CertificationController` (REST endpoints for CRUD, upload, alerts)
- **Security:**
  - Only HR/SUPERVISOR/ADMIN can update certifications; WORKER can view own.
- **Integration:**
  - Scheduling module checks certification validity before assignment; notification triggers for expiring certs.

Sample Implementation:
```java
@Entity
public class Certification {
    @Id @GeneratedValue
    private Long id;
    private Long employeeId;
    private String type;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private String status; // ACTIVE, EXPIRED
    private String documentUrl;
}

@RestController
@RequestMapping("/api/certifications")
public class CertificationController {
    @PostMapping("/upload")
    @PreAuthorize("hasRole('HR') or hasRole('SUPERVISOR') or hasRole('ADMIN')")
    public Certification uploadProof(@RequestParam MultipartFile file, @RequestParam Long certId) { ... }

    @GetMapping("/alerts")
    public List<Certification> getExpiringCerts(@RequestParam int days) { ... }
}
```
