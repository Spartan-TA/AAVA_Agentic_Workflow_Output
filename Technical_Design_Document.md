# Warehouse Employee Management System - Low-Level Technical Design Document

## Table of Contents
1. Introduction
2. Architecture Overview
3. Technology Stack
4. Module Breakdown (per User Story)
5. Security & Compliance
6. Integration Points
7. Error Handling & Logging
8. Testing Strategy
9. Deployment & Operations
10. Appendix: Entity-Relationship Diagrams, API Schemas

---

## 1. Introduction
This document provides a comprehensive low-level technical design for the Warehouse Employee Management System, covering all 20 user stories. It is intended for Spring Boot developers and adheres to industry standards for maintainability, scalability, and security.

## 2. Architecture Overview
- **Backend:** Spring Boot (Maven), Java 17+, RESTful APIs
- **Database:** PostgreSQL, Flyway for migrations
- **Security:** Spring Security (RBAC, OAuth2, API Key)
- **Frontend:** (For PWA) Thymeleaf/React (out of scope for backend)
- **Other:** Actuator, OpenAPI/Swagger, SFTP/API for integrations

### Layered Structure
- **Controller Layer:** REST endpoints, DTO mapping
- **Service Layer:** Business logic, transaction management
- **Repository Layer:** Spring Data JPA, custom queries
- **Domain Layer:** Entities, enums, value objects
- **Integration Layer:** External APIs, SFTP, webhooks
- **Utility Layer:** Auditing, notifications, reporting

## 3. Technology Stack
- Spring Boot 3.x
- Spring Data JPA
- Spring Security
- Flyway
- Lombok
- MapStruct
- OpenAPI (springdoc-openapi)
- PostgreSQL
- Actuator
- JUnit 5, Mockito

## 4. Module Breakdown (per User Story)

### E01: Project Scaffolding & Domain Setup
- **Packages:** `com.company.wms.{employee, schedule, attendance, safety, asset, integration, audit}`
- **Flyway:** `V1__baseline.sql` for initial schema
- **Actuator:** `/actuator/health`, `/actuator/info`
- **README:** Build/run instructions

### E02: Employee Master Data (CRUD)
- **Entity:** `Employee` (id, name, badgeId, role, department, shiftGroup, hireDate, status, deleted)
- **Repository:** `EmployeeRepository extends JpaRepository<Employee, Long>`
- **Service:** `EmployeeService` (CRUD, soft-delete, filtering)
- **Controller:** `EmployeeController` (`/employees` endpoints)
- **DTOs:** `EmployeeDTO`, `EmployeeCreateDTO`, `EmployeeUpdateDTO`
- **Validation:** Unique badgeId, required fields
- **OpenAPI:** Schemas with examples

### E03: Role-Based Access Control (RBAC)
- **Roles:** ADMIN, HR, SUPERVISOR, WORKER
- **Spring Security:** Method/endpoint security via `@PreAuthorize`
- **Row-level security:** Service-layer checks (e.g., SUPERVISOR can only access team)
- **Config:** API key/OAuth2 toggle via `application.yml`
- **Exception Handling:** 401/403 responses
- **Tests:** Security coverage via `@WithMockUser`

### E04: Time & Attendance (Clock In/Out)
- **Entity:** `AttendanceEvent` (id, employee, type [IN/OUT], timestamp, deviceId, location, status)
- **Repository:** `AttendanceRepository`
- **Service:** `AttendanceService` (clock-in/out, shift association, corrections)
- **Controller:** `AttendanceController` (`/attendance/clock-in`, `/clock-out`)
- **Geofence:** Optional, via location validation
- **Corrections:** Approval workflow entity (`AttendanceCorrection`)
- **Reports:** Export endpoints (CSV)

### E05: Shift & Schedule Management
- **Entities:** `ShiftTemplate`, `ShiftAssignment`, `OvertimeRule`, `OperationCalendar`
- **Repositories:** For each entity
- **Service:** `ScheduleService` (conflict detection, bulk-assign, audit)
- **Controller:** `/shifts`, `/schedules`
- **Audit:** On assignment changes

### E06: Leave & Absence Management
- **Entities:** `LeaveRequest`, `LeaveBalance`, `LeavePolicy`
- **Service:** `LeaveService` (request, approve/deny, accruals)
- **Controller:** `/leaves`
- **Integration:** Exclude from scheduling/payroll
- **Exports:** Approved leaves

### E07: Training & Certification Tracking
- **Entities:** `Certification`, `EmployeeCertification`
- **Service:** `CertificationService` (CRUD, expiry alerts)
- **Controller:** `/certifications`
- **Document Upload:** S3/local storage, link in entity
- **Scheduling Check:** Block unqualified assignments

### E08: Safety Incidents & OSHA Reporting
- **Entities:** `SafetyIncident`, `IncidentInvestigation`, `CorrectiveAction`
- **Service:** `SafetyService` (workflow, OSHA export)
- **Controller:** `/safety/incidents`
- **Status Workflow:** Enum (OPEN, INVESTIGATING, RESOLVED)
- **Dashboard:** Metrics endpoints

### E09: Equipment & Asset Assignment
- **Entities:** `Asset`, `AssetAssignment`, `AssetCondition`
- **Service:** `AssetService` (check-in/out, condition tracking)
- **Controller:** `/assets`, `/assets/assign`
- **Certification Check:** On assignment
- **History Log:** Per asset/employee

### E10: Performance Reviews & Goals
- **Entities:** `ReviewCycle`, `PerformanceReview`, `Goal`, `Competency`
- **Service:** `ReviewService` (workflow, PDF export)
- **Controller:** `/reviews`
- **Acknowledgement:** Supervisor/employee
- **Immutability:** After sign-off

### E11: Payroll Export Integration
- **Service:** `PayrollExportService` (generate, map, deliver)
- **Integration:** SFTP/API clients
- **Audit:** Export logs
- **Retry Logic:** Exponential backoff
- **Controller:** `/payroll/export`

### E12: Notifications & Announcements
- **Entities:** `Notification`, `Announcement`, `UserPreference`
- **Service:** `NotificationService` (in-app, email, SMS)
- **Controller:** `/notifications`, `/announcements`
- **Opt-in/out:** User preferences
- **Templates:** Localization support
- **Delivery Status:** Tracked per notification

### E13: Integration Layer (HRIS/WMS APIs)
- **APIs:** `/api/hris`, `/api/wms`, `/api/idp`
- **Security:** JWT/OAuth2
- **Sync Jobs:** Scheduled tasks for HRIS
- **Webhooks:** Idempotent event handlers
- **OpenAPI:** Full documentation

### E14: Audit Trail & Compliance
- **Entity:** `AuditLog` (actor, timestamp, entity, before/after, action)
- **Service:** `AuditService` (log on sensitive ops)
- **Storage:** Immutable table
- **Exports:** By date/user/entity
- **Tests:** Audit coverage

### E15: Reporting & Analytics
- **Service:** `ReportingService` (attendance, overtime, leave, certs, safety)
- **Controller:** `/reports`
- **Exports:** CSV/PDF
- **Dashboards:** Role-based endpoints
- **Performance:** Pagination, async for large exports

### E16: Mobile Access (PWA)
- **Endpoints:** Mobile-friendly versions of core flows
- **PWA Manifest:** Served from `/manifest.json`
- **Offline Queue:** Local DB for clock events
- **Conflict Resolution:** On reconnect
- **Lighthouse:** Score validation

### E17: Onboarding & Offboarding Workflow
- **Workflow Engine:** Spring State Machine or custom
- **Tasks:** Account provisioning, training, asset assignment
- **Deprovision:** Access revocation, asset collection
- **Integration:** HRIS triggers

### E18: Bulk Data Import/Export
- **Endpoints:** `/import`, `/export` (CSV, Excel)
- **Validation:** Pre-import checks, error reporting
- **Service:** `BulkImportService`, `BulkExportService`
- **Audit:** Import/export logs

### E19: API Documentation & Developer Portal
- **OpenAPI:** `/v3/api-docs`, `/swagger-ui.html`
- **Portal:** Static site with guides, examples
- **Security:** Docs protected for internal APIs

### E20: Environment Configuration & Secrets Management
- **Profiles:** `dev`, `test`, `prod` in `application-*.yml`
- **Secrets:** Spring Cloud Vault or environment variables
- **Config Server:** Optional for distributed config
- **Sensitive Values:** Never in source control

## 5. Security & Compliance
- **PII Protection:** Field-level encryption for sensitive data
- **Audit Logging:** All sensitive changes
- **Input Validation:** DTO-level, controller-level
- **Output Encoding:** Prevent XSS in web views
- **Rate Limiting:** For sensitive endpoints
- **Compliance:** GDPR, OSHA, local labor laws

## 6. Integration Points
- **HRIS:** REST API, scheduled sync, webhook
- **WMS:** REST API, department/location sync
- **Payroll:** SFTP/API, export mapping
- **IDP:** SSO via OAuth2/OpenID Connect

## 7. Error Handling & Logging
- **Global Exception Handler:** `@ControllerAdvice`
- **Error Responses:** Standardized error DTOs
- **Logging:** SLF4J, logback, structured logs
- **Sensitive Data:** Masked in logs

## 8. Testing Strategy
- **Unit Tests:** JUnit 5, Mockito, 80%+ coverage
- **Integration Tests:** Testcontainers for PostgreSQL
- **Security Tests:** MockMvc with roles
- **Performance Tests:** JMeter/Gatling (for exports)
- **CI/CD:** GitHub Actions, code quality gates

## 9. Deployment & Operations
- **Docker:** Dockerfile, docker-compose for local/dev
- **Kubernetes:** Helm charts (optional)
- **Monitoring:** Actuator, Prometheus, Grafana
- **Health Checks:** Liveness/readiness probes
- **Rollback:** Versioned DB migrations

## 10. Appendix
### Entity-Relationship Diagram (ERD)
- [ERD diagram here, see source]

### Sample API Schema (OpenAPI)
- `/employees`:
  - `GET /employees?filter=...`
  - `POST /employees`
  - `PATCH /employees/{id}`
  - `DELETE /employees/{id}`
- `/attendance/clock-in`:
  - `POST /attendance/clock-in`

---

# End of Document
