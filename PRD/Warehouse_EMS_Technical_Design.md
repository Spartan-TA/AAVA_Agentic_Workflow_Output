# Warehouse Employee Management System - Low-Level Technical Design Document

This document provides comprehensive technical design specifications for all 65 user stories across 20 epics.

## Executive Summary

The Warehouse EMS is built using Spring Boot with Maven, following a layered architecture pattern. The system supports employee management, time tracking, scheduling, leave management, certifications, safety reporting, asset management, performance reviews, payroll integration, notifications, and self-service capabilities.

## Architecture Overview

### Technology Stack
- **Framework:** Spring Boot 2.7+
- **Build Tool:** Maven
- **Database:** PostgreSQL 12+
- **Migration:** Flyway
- **Security:** Spring Security with OAuth2/JWT
- **API Documentation:** SpringDoc OpenAPI
- **Caching:** Redis (optional)

### Package Structure
```
com.warehouseems
âââ config
âââ employee
â   âââ domain
â   âââ repository
â   âââ service
â   âââ controller
âââ attendance
âââ shift
âââ leave
âââ certification
âââ safety
âââ asset
âââ review
âââ payroll
âââ notification
âââ integration
âââ audit
âââ report
âââ mobile
âââ onboarding
âââ warehouse
âââ selfservice
```

## Detailed Design by Epic

### Epic 1: Project Scaffolding & Domain Setup

**User Story 1-3:** Project initialization, documentation, and database migration setup are covered in the architecture overview above.

### Epic 2: Employee Master Data

**Entity Design:**
```java
@Entity
public class Employee {
    @Id @GeneratedValue private Long id;
    @Column(unique=true) private String badgeId;
    private String name, role, department, shiftGroup;
    private LocalDate hireDate;
    private String status; // ACTIVE/INACTIVE
}
```

**Key Features:**
- CRUD operations with pagination
- Unique badge ID enforcement
- Soft delete capability
- OpenAPI documentation

### Epic 3: Role-Based Access Control

**Security Configuration:**
- Roles: ADMIN, HR, SUPERVISOR, WORKER
- Method-level security with @PreAuthorize
- Configurable authentication (API Key/OAuth2)

### Epic 4: Time & Attendance

**Entity Design:**
```java
@Entity
public class AttendanceEvent {
    @Id @GeneratedValue private Long id;
    private Long employeeId, shiftId;
    private LocalDateTime timestamp;
    private EventType type; // CLOCK_IN/CLOCK_OUT
    private EventStatus status; // NORMAL/MISSED/CORRECTED
}
```

**Features:**
- Clock-in/out with geofence validation
- Automatic shift association
- Hours calculation
- Missed punch correction workflow

### Epic 5: Shift & Schedule Management

**Entity Design:**
```java
@Entity
public class ShiftTemplate {
    @Id @GeneratedValue private Long id;
    private String name, department;
    private LocalTime startTime, endTime;
    private Recurrence recurrence;
}

@Entity
public class ShiftAssignment {
    @Id @GeneratedValue private Long id;
    private Long employeeId, shiftTemplateId;
    private LocalDate date;
    private AssignmentStatus status;
}
```

**Features:**
- Recurring shift templates
- Conflict detection
- Bulk assignment
- Audit trail

### Epic 6: Leave & Absence Management

**Entity Design:**
```java
@Entity
public class LeaveRequest {
    @Id @GeneratedValue private Long id;
    private Long employeeId;
    private LeaveType type; // PTO/SICK/UNPAID
    private LocalDate startDate, endDate;
    private LeaveStatus status;
}

@Entity
public class LeaveBalance {
    @Id @GeneratedValue private Long id;
    private Long employeeId;
    private int pto, sick, unpaid;
}
```

**Features:**
- Leave request submission and approval
- Balance tracking with accrual
- Integration with scheduling

### Epic 7: Training & Certification Tracking

**Entity Design:**
```java
@Entity
public class Certification {
    @Id @GeneratedValue private Long id;
    private Long employeeId;
    private CertificationType type;
    private LocalDate issueDate, expiryDate;
    private String proofDocUrl;
}
```

**Features:**
- Certification CRUD
- Expiry alerts (30/7 days)
- Assignment validation

### Epic 8: Safety Incidents & OSHA Reporting

**Entity Design:**
```java
@Entity
public class SafetyIncident {
    @Id @GeneratedValue private Long id;
    private Long reporterId;
    private Severity severity;
    private String location, description;
    private List<Long> involvedEmployeeIds;
}

@Entity
public class IncidentWorkflow {
    @Id @GeneratedValue private Long id;
    private Long incidentId, investigatorId;
    private WorkflowStatus status; // OPEN/INVESTIGATING/RESOLVED
}
```

**Features:**
- Incident recording
- Investigation workflow
- OSHA report generation

### Epic 9: Equipment & Asset Assignment

**Entity Design:**
```java
@Entity
public class Asset {
    @Id @GeneratedValue private Long id;
    private AssetType type;
    private String serialNumber;
    private AssetStatus status;
    private Long assignedTo;
}

@Entity
public class AssetAssignment {
    @Id @GeneratedValue private Long id;
    private Long assetId, employeeId;
    private LocalDateTime checkoutTime, returnTime;
}
```

**Features:**
- Asset registry
- Check-in/out tracking
- Certification validation
- Overdue return reports

### Epic 10: Performance Reviews & Goals

**Entity Design:**
```java
@Entity
public class ReviewTemplate {
    @Id @GeneratedValue private Long id;
    private String name;
    private ReviewCycle cycle;
    private List<String> fields;
}

@Entity
public class PerformanceReview {
    @Id @GeneratedValue private Long id;
    private Long employeeId, templateId, supervisorId;
    private ReviewStatus status;
    private Map<String, Integer> ratings;
}
```

**Features:**
- Template management
- Review assignment
- PDF export

### Epic 11: Payroll Export Integration

**Features:**
- Payroll file generation from attendance/leave
- Secure delivery via SFTP/API
- Retry with exponential backoff
- Audit logging

### Epic 12: Notifications & Announcements

**Entity Design:**
```java
@Entity
public class NotificationPreference {
    @Id @GeneratedValue private Long id;
    private Long userId;
    private boolean inApp, email, sms;
}

@Entity
public class NotificationTemplate {
    @Id @GeneratedValue private Long id;
    private String key, locale, content;
}
```

**Features:**
- Multi-channel notifications
- Template localization
- Delivery tracking
- Announcements dashboard

### Epic 13: Integration Layer

**Features:**
- HRIS sync for employee data
- WMS sync for departments/locations
- SSO integration (OAuth2/JWT)
- OpenAPI documentation

### Epic 14: Audit Trail & Compliance

**Entity Design:**
```java
@Entity
public class AuditLog {
    @Id @GeneratedValue private Long id;
    private Long actorId;
    private String entityType, entityId, action;
    private String before, after;
    private LocalDateTime timestamp;
}
```

**Features:**
- Immutable audit logging
- Export by date/user/entity
- Comprehensive coverage

### Epic 15: Reporting & Analytics

**Reports:**
- Attendance reports
- Overtime reports
- Leave balance reports
- Certification status reports
- Safety KPI dashboard

**Performance:**
- Indexed queries
- Pagination support
- CSV/PDF export
- Sub-10 second generation for 50k rows

### Epic 16: Mobile Access (PWA)

**Features:**
- Progressive Web App with manifest
- Service worker for offline support
- Responsive mobile UI
- Clock-in/out, schedule view, leave requests
- Offline queue with sync

### Epic 17: Onboarding & Offboarding Workflow

**Features:**
- Automated task creation
- Onboarding: training, assets, scheduling
- Offboarding: access revocation, asset collection
- Workflow tracking

### Epic 18: Localization & Multi-Warehouse

**Features:**
- Multi-language support (EN/ES)
- MessageSource configuration
- Warehouse-specific policies
- Localized date/time formatting

### Epic 19: Advanced Scheduling

**Features:**
- AI-based scheduling suggestions
- ML model trained on historical data
- Feedback loop for model improvement
- Explainability logging

### Epic 20: Self-Service Portal

**Features:**
- Pay stub viewing
- Contact information updates
- View-only security for sensitive data
- Input validation

## Security Considerations

1. **Authentication:** OAuth2/JWT or API Key (configurable)
2. **Authorization:** Role-based access control
3. **Data Protection:** Encryption at rest and in transit
4. **Audit Logging:** All sensitive operations logged
5. **Input Validation:** Server-side validation for all inputs

## Performance Optimization

1. **Database Indexing:** On frequently queried fields
2. **Pagination:** For large result sets
3. **Caching:** Redis for frequently accessed data
4. **Async Processing:** For long-running operations
5. **Connection Pooling:** HikariCP configuration

## Testing Strategy

1. **Unit Tests:** Service layer logic
2. **Integration Tests:** Repository and controller layers
3. **Security Tests:** Role-based access validation
4. **Performance Tests:** Load testing for reports
5. **E2E Tests:** Critical user workflows

## Deployment Architecture

1. **Application Server:** Spring Boot embedded Tomcat
2. **Database:** PostgreSQL with replication
3. **Caching:** Redis cluster
4. **Load Balancer:** Nginx or AWS ALB
5. **Monitoring:** Actuator endpoints + Prometheus/Grafana

## API Endpoints Summary

### Employee Management
- GET /api/employees - List employees
- POST /api/employees - Create employee
- PUT /api/employees/{id} - Update employee
- DELETE /api/employees/{id} - Soft delete employee

### Attendance
- POST /api/attendance/clock-in - Clock in
- POST /api/attendance/clock-out - Clock out
- GET /api/attendance/report - Export report

### Shifts
- POST /api/shifts/templates - Create template
- POST /api/shifts/assign - Assign shifts
- GET /api/shifts/my - View my shifts

### Leave
- POST /api/leave/request - Submit request
- POST /api/leave/approve - Approve request
- GET /api/leave/balance - View balance

### Certifications
- POST /api/certifications - Add certification
- GET /api/certifications/{employeeId} - List certifications

### Safety
- POST /api/safety/incidents - Record incident
- POST /api/safety/investigate - Start investigation
- GET /api/safety/osha-report - Export OSHA report

### Assets
- POST /api/assets - Add asset
- POST /api/assets/checkout - Checkout asset
- POST /api/assets/checkin - Checkin asset

### Reviews
- POST /api/reviews/templates - Create template
- POST /api/reviews/assign - Assign review
- GET /api/reviews/{id}/pdf - Export PDF

### Payroll
- GET /api/payroll/export - Generate export

### Notifications
- GET /api/notifications/preferences - Get preferences
- PUT /api/notifications/preferences - Update preferences
- GET /api/announcements - List announcements

### Integration
- POST /api/integration/hris/sync - Sync HRIS
- POST /api/integration/wms/sync - Sync WMS

### Audit
- GET /api/audit/export - Export audit logs

### Reports
- GET /api/reports/attendance - Attendance report
- GET /api/reports/overtime - Overtime report
- GET /api/reports/leave-balance - Leave balance report
- GET /api/reports/certifications - Certification report
- GET /api/safety/kpis - Safety KPIs

### Self-Service
- GET /api/self-service/paystubs - View pay stubs
- PUT /api/self-service/contact - Update contact info

## Database Schema Summary

### Core Tables
- employee
- attendance_event
- shift_template
- shift_assignment
- leave_request
- leave_balance
- certification
- safety_incident
- incident_workflow
- asset
- asset_assignment
- review_template
- performance_review
- notification_preference
- notification_template
- notification_log
- announcement
- audit_log
- warehouse

## Configuration Properties

```properties
# Server
server.port=8080

# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/warehouse_ems
spring.datasource.username=postgres
spring.datasource.password=password

# Flyway
spring.flyway.enabled=true
spring.flyway.baseline-on-migrate=true

# Security
auth.mode=oauth2
spring.security.oauth2.resourceserver.jwt.issuer-uri=https://your-idp.com

# Actuator
management.endpoints.web.exposure.include=health,info,metrics

# Logging
logging.level.com.warehouseems=INFO
```

## Conclusion

This technical design document provides a comprehensive blueprint for implementing all 65 user stories of the Warehouse EMS. The design follows Spring Boot best practices, ensures security and compliance, and provides a scalable foundation for future enhancements.

**Key Strengths:**
- Modular architecture with clear separation of concerns
- Comprehensive security with RBAC and audit logging
- Scalable design supporting multiple warehouses
- Mobile-first approach with PWA support
- Extensive integration capabilities
- Performance-optimized with caching and indexing

**Implementation Priority:**
1. Core infrastructure (Epics 1-3)
2. Employee and attendance management (Epics 2, 4)
3. Scheduling and leave (Epics 5-6)
4. Certifications and safety (Epics 7-8)
5. Assets and reviews (Epics 9-10)
6. Integration and reporting (Epics 11-15)
7. Mobile and advanced features (Epics 16-20)