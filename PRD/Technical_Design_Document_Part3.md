Section: E11 - Payroll Export Integration
Description: Generate payroll-ready files from approved attendance and leave; mapping to external payroll provider formats; secure delivery (SFTP/API).
Design Specification:
- PayrollExportService for file generation
- Integration with SFTP/API
- Audit log for exports
- Retry logic for failed deliveries
Sample Implementation:
```
@Service
public class PayrollExportService {
  public void exportPayroll() {
    // gather approved attendance/leave
    // map to provider schema
    // deliver via SFTP/API
    // log export
  }
}
```

Section: E12 - Notifications & Announcements
Description: In-app and email/SMS notifications for shift changes, expiring certs, approvals, announcements; quiet hours configuration.
Design Specification:
- Notification entity and service
- Channel opt-in/out logic
- Localized templates
- Delivery status tracking
- Rate limiting
Sample Implementation:
```
@Entity
public class Notification {
  @Id @GeneratedValue private Long id;
  @ManyToOne private Employee employee;
  private String channel; // EMAIL, SMS, IN_APP
  private String template;
  private String status;
}
```

Section: E13 - Integration Layer (HRIS/WMS APIs)
Description: Expose REST APIs and connectors for HRIS (new hires/terms), WMS (location/department), and IDP for SSO; webhooks for events.
Design Specification:
- REST controllers for HRIS/WMS endpoints
- JWT/OAuth2 security
- Webhook endpoints
- OpenAPI documentation
Sample Implementation:
```
@RestController
@RequestMapping("/api/hris")
public class HRISController {
  @PostMapping("/employees") public ResponseEntity<?> syncEmployee(...)
}
```

Section: E14 - Audit Trail & Compliance
Description: Centralized audit logging for sensitive changes (employee PII, schedules, approvals, payroll); tamper-evident storage.
Design Specification:
- AuditLog entity (actor, timestamp, entity, before/after)
- Service for logging changes
- Export endpoints
- Immutable log table
Sample Implementation:
```
@Entity
public class AuditLog {
  @Id @GeneratedValue private Long id;
  private String actor;
  private LocalDateTime timestamp;
  private String entity;
  private String before;
  private String after;
}
```

Section: E15 - Reporting & Analytics
Description: Operational reports: attendance, overtime, leave balances, certification status, safety KPIs; export CSV/PDF; basic role-based dashboards.
Design Specification:
- ReportingService for report generation
- CSV/PDF export endpoints
- Role-based access
- Metrics endpoints
Sample Implementation:
```
@Service
public class ReportingService {
  public byte[] exportAttendanceReport(...) {...}
}
```
