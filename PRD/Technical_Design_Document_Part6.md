# Technical Design Document â Part 6 (User Stories 26-30)

---

Section: E08-Safety Incidents & OSHA Reporting (User Stories 26-28)

Description: This module records safety incidents and near-misses, tracks severity, location, involved employees, and manages investigation workflows. It supports OSHA reporting and provides metrics dashboards.

Design Specification:
- **Package Structure:**
  - `com.company.wms.safety` (domain, repository, service, controller, dto)
- **Entities:**
  - `SafetyIncident` (id, date, location, severity, description, status, reportedBy, involvedEmployeeIds, correctiveActions, oshaFields)
  - `CorrectiveAction` (id, incidentId, action, assignedTo, dueDate, status)
- **Repositories:**
  - `SafetyIncidentRepository`, `CorrectiveActionRepository`
- **Services:**
  - `SafetyIncidentService` (CRUD, workflow, OSHA export, metrics)
- **Controllers:**
  - `SafetyIncidentController` (REST endpoints for reporting, workflow, export)
- **Security:**
  - Only SUPERVISOR/ADMIN can resolve incidents; WORKER can report.
- **Integration:**
  - Dashboard endpoints for KPIs; export OSHA 300/300A.

Sample Implementation:
```java
@Entity
public class SafetyIncident {
    @Id @GeneratedValue
    private Long id;
    private LocalDate date;
    private String location;
    private String severity;
    private String description;
    private String status; // OPEN, INVESTIGATING, RESOLVED
    private Long reportedBy;
    @ElementCollection
    private List<Long> involvedEmployeeIds;
    @OneToMany(mappedBy = "incidentId")
    private List<CorrectiveAction> correctiveActions;
    @Embedded
    private OSHAFields oshaFields;
}

@RestController
@RequestMapping("/api/safety/incidents")
public class SafetyIncidentController {
    @PostMapping
    @PreAuthorize("hasRole('WORKER') or hasRole('SUPERVISOR') or hasRole('ADMIN')")
    public SafetyIncident reportIncident(@RequestBody SafetyIncidentDto dto) { ... }

    @PostMapping("/{id}/resolve")
    @PreAuthorize("hasRole('SUPERVISOR') or hasRole('ADMIN')")
    public SafetyIncident resolveIncident(@PathVariable Long id) { ... }
}
```

---

Section: E09-Equipment & Asset Assignment (User Stories 29-30)

Description: This module manages assignment of equipment (scanners, forklifts, PPE) to employees, tracks check-in/out, enforces certification requirements, and maintains asset condition.

Design Specification:
- **Package Structure:**
  - `com.company.wms.asset` (domain, repository, service, controller, dto)
- **Entities:**
  - `Asset` (id, type, serialNumber, condition, assignedTo, checkedOutAt, checkedInAt, status)
  - `AssetAssignment` (id, assetId, employeeId, checkoutDate, returnDate, status)
- **Repositories:**
  - `AssetRepository`, `AssetAssignmentRepository`
- **Services:**
  - `AssetService` (CRUD, check-in/out, enforce certs, overdue reports)
- **Controllers:**
  - `AssetController` (REST endpoints for registry, assignment, history)
- **Security:**
  - Only SUPERVISOR/ADMIN can assign; WORKER can check-in/out own assets.
- **Integration:**
  - Certification check before assignment; overdue/condition reports.

Sample Implementation:
```java
@Entity
public class Asset {
    @Id @GeneratedValue
    private Long id;
    private String type;
    private String serialNumber;
    private String condition;
    private Long assignedTo;
    private LocalDateTime checkedOutAt;
    private LocalDateTime checkedInAt;
    private String status; // AVAILABLE, ASSIGNED, MAINTENANCE
}

@RestController
@RequestMapping("/api/assets")
public class AssetController {
    @PostMapping("/assign")
    @PreAuthorize("hasRole('SUPERVISOR') or hasRole('ADMIN')")
    public AssetAssignment assignAsset(@RequestBody AssetAssignmentDto dto) { ... }

    @GetMapping("/history/{assetId}")
    public List<AssetAssignment> getAssetHistory(@PathVariable Long assetId) { ... }
}
```
