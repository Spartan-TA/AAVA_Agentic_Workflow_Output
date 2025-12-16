# Technical Design Document â Part 4 (User Stories 16-20)

---

Section: E05-Shift & Schedule Management (User Stories 16-19)

Description: This module manages recurring shift templates, shift rotations, overtime rules, and employee shift assignments. It also handles blackout dates and warehouse operation calendars to ensure adequate staffing and reduce scheduling conflicts.

Design Specification:
- **Package Structure:**
  - `com.company.wms.shift` (domain, repository, service, controller, dto)
- **Entities:**
  - `ShiftTemplate` (id, name, startTime, endTime, recurrencePattern, overtimeRule, blackoutDates, active)
  - `ShiftAssignment` (id, employeeId, shiftTemplateId, date, status, assignedBy, createdAt)
  - `OperationCalendar` (id, date, isBlackout, description)
- **Repositories:**
  - `ShiftTemplateRepository`, `ShiftAssignmentRepository`, `OperationCalendarRepository`
- **Services:**
  - `ShiftService` (CRUD for templates, assign shifts, detect conflicts, bulk assignment, audit logging)
- **Controllers:**
  - `ShiftController` (REST endpoints for CRUD, assignment, conflict check)
- **Security:**
  - RBAC: Only SUPERVISOR/ADMIN can create/assign shifts; WORKER can view own shifts.
- **Integration:**
  - Audit trail for all changes; notification trigger on assignment.

Sample Implementation:
```java
@Entity
public class ShiftTemplate {
    @Id @GeneratedValue
    private Long id;
    private String name;
    private LocalTime startTime;
    private LocalTime endTime;
    private String recurrencePattern; // e.g., CRON or custom
    private String overtimeRule;
    @ElementCollection
    private List<LocalDate> blackoutDates;
    private boolean active;
}

@RestController
@RequestMapping("/api/shifts")
public class ShiftController {
    @PostMapping("/templates")
    @PreAuthorize("hasRole('SUPERVISOR') or hasRole('ADMIN')")
    public ShiftTemplate createTemplate(@RequestBody ShiftTemplateDto dto) { ... }

    @PostMapping("/assign")
    @PreAuthorize("hasRole('SUPERVISOR') or hasRole('ADMIN')")
    public ResponseEntity<?> assignShift(@RequestBody ShiftAssignmentDto dto) { ... }

    @GetMapping("/my-upcoming")
    @PreAuthorize("hasRole('WORKER')")
    public List<ShiftAssignment> getMyShifts(Authentication auth) { ... }
}
```

---

Section: E06-Leave & Absence Management (User Stories 20-22)

Description: This module allows employees to request paid/unpaid leave, sick leave, and tracks accrual balances and policies. It integrates with scheduling and payroll to ensure compliant handling of time off and accurate staffing.

Design Specification:
- **Package Structure:**
  - `com.company.wms.leave` (domain, repository, service, controller, dto)
- **Entities:**
  - `LeaveRequest` (id, employeeId, type, startDate, endDate, status, requestedAt, approvedBy, approvedAt, comments)
  - `LeaveBalance` (id, employeeId, leaveType, balance, accrualPolicy)
- **Repositories:**
  - `LeaveRequestRepository`, `LeaveBalanceRepository`
- **Services:**
  - `LeaveService` (request, approve/deny, update balances, auto-flag scheduled shifts, export leaves)
- **Controllers:**
  - `LeaveController` (REST endpoints for request, approval, export)
- **Security:**
  - Employees can request; supervisors approve/deny; RBAC enforced.
- **Integration:**
  - Hooks to shift scheduling and payroll modules; notification triggers.

Sample Implementation:
```java
@Entity
public class LeaveRequest {
    @Id @GeneratedValue
    private Long id;
    private Long employeeId;
    private String type; // PTO, Sick, Unpaid
    private LocalDate startDate;
    private LocalDate endDate;
    private String status; // REQUESTED, APPROVED, DENIED
    private LocalDateTime requestedAt;
    private Long approvedBy;
    private LocalDateTime approvedAt;
    private String comments;
}

@Service
public class LeaveService {
    public LeaveRequest requestLeave(LeaveRequestDto dto) { ... }
    public LeaveRequest approveLeave(Long requestId, Long supervisorId) { ... }
    public void updateLeaveBalances(Long employeeId, String leaveType, int days) { ... }
}

@RestController
@RequestMapping("/api/leave")
public class LeaveController {
    @PostMapping("/request")
    @PreAuthorize("hasRole('WORKER')")
    public LeaveRequest requestLeave(@RequestBody LeaveRequestDto dto) { ... }

    @PostMapping("/approve/{id}")
    @PreAuthorize("hasRole('SUPERVISOR')")
    public LeaveRequest approveLeave(@PathVariable Long id, Authentication auth) { ... }
}
```
