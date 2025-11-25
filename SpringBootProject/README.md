# Warehouse Employee Management System

## Overview

A comprehensive Spring Boot application for managing warehouse employee operations including employee master data, time & attendance, shift scheduling, leave management, training certifications, safety incidents, equipment assignments, performance reviews, payroll integration, notifications, and more.

## Technology Stack

- **Java**: 17
- **Spring Boot**: 3.2.5
- **Database**: PostgreSQL
- **Build Tool**: Maven
- **Migration Tool**: Flyway
- **Security**: Spring Security with OAuth2/JWT
- **API Documentation**: SpringDoc OpenAPI
- **Monitoring**: Spring Boot Actuator + Prometheus
- **Export**: OpenCSV, iText PDF

## Project Structure

```
SpringBootProject/
âââ pom.xml
âââ README.md
âââ src/
â   âââ main/
â   â   âââ java/com/warehouse/employee/
â   â   â   âââ EmployeeManagementApplication.java
â   â   â   âââ config/
â   â   â   â   âââ SecurityConfig.java
â   â   â   â   âââ ActuatorConfig.java
â   â   â   âââ model/
â   â   â   â   âââ Employee.java
â   â   â   â   âââ Attendance.java
â   â   â   â   âââ Shift.java
â   â   â   â   âââ Leave.java
â   â   â   â   âââ Certification.java
â   â   â   â   âââ SafetyIncident.java
â   â   â   â   âââ Asset.java
â   â   â   â   âââ AuditLog.java
â   â   â   âââ repository/
â   â   â   â   âââ EmployeeRepository.java
â   â   â   â   âââ AttendanceRepository.java
â   â   â   â   âââ ShiftRepository.java
â   â   â   â   âââ LeaveRepository.java
â   â   â   â   âââ CertificationRepository.java
â   â   â   â   âââ SafetyIncidentRepository.java
â   â   â   â   âââ AssetRepository.java
â   â   â   â   âââ AuditLogRepository.java
â   â   â   âââ service/
â   â   â   â   âââ EmployeeService.java
â   â   â   â   âââ AttendanceService.java
â   â   â   â   âââ ShiftService.java
â   â   â   â   âââ LeaveService.java
â   â   â   â   âââ CertificationService.java
â   â   â   â   âââ SafetyIncidentService.java
â   â   â   â   âââ AssetService.java
â   â   â   â   âââ AuditLogService.java
â   â   â   âââ controller/
â   â   â   â   âââ EmployeeController.java
â   â   â   â   âââ AttendanceController.java
â   â   â   â   âââ ShiftController.java
â   â   â   â   âââ LeaveController.java
â   â   â   â   âââ CertificationController.java
â   â   â   â   âââ SafetyIncidentController.java
â   â   â   â   âââ AssetController.java
â   â   â   âââ dto/
â   â   â       âââ EmployeeDTO.java
â   â   â       âââ AttendanceDTO.java
â   â   â       âââ ...
â   â   âââ resources/
â   â       âââ application.properties
â   â       âââ db/migration/
â   â           âââ V1__init.sql
â   âââ test/
â       âââ java/com/warehouse/employee/
â           âââ (Unit tests to be added)
```

## Features by Epic

### E01: Project Scaffolding & Domain Setup
- Maven-based Spring Boot project
- Base package structure (employee, scheduling, attendance, safety)
- Flyway database migrations
- Spring Boot Actuator health endpoint

### E02: Employee Master Data (CRUD)
- Employee entity with badgeId, name, role, department, shiftGroup, hireDate, status
- CRUD REST APIs with pagination and filtering
- Unique badgeId enforcement
- Soft-delete support
- OpenAPI documentation

### E03: Role-Based Access Control (RBAC)
- Spring Security with roles: ADMIN, HR, SUPERVISOR, WORKER
- Method and endpoint-level security
- OAuth2/JWT authentication
- Row-level security for supervisors

### E04: Time & Attendance
- Clock-in/out endpoints with geofence validation
- Automatic shift association
- Hours worked calculation
- Missed punch correction workflow
- CSV export for payroll

### E05: Shift & Schedule Management
- Shift templates and rotations
- Bulk assignment to employees
- Conflict detection
- Blackout dates and operation calendars

### E06: Leave & Absence Management
- PTO, sick, and unpaid leave requests
- Approval workflow
- Accrual balance tracking
- Integration with scheduling

### E07: Training & Certification Tracking
- Certification CRUD with expiry dates
- 30/7-day expiry alerts
- Block assignments for expired certifications
- Document upload support

### E08: Safety Incidents & OSHA Reporting
- Incident recording with severity and location
- Investigation workflow (Open â Investigating â Resolved)
- OSHA 300/300A export
- Safety metrics dashboard

### E09: Equipment & Asset Assignment
- Asset registry with check-in/out
- Certification validation for equipment use
- Overdue return reports
- Asset condition tracking

### E10: Performance Reviews & Goals
- Quarterly/annual review cycles
- Goals, competencies, and ratings
- Supervisor/employee acknowledgment
- PDF export and immutable history

### E11: Payroll Export Integration
- Generate payroll-ready files from attendance and leave
- Secure delivery (SFTP/API)
- Reconciliation with reports
- Retry logic with audit trail

### E12: Notifications & Announcements
- In-app, email, and SMS notifications
- Opt-in/out per channel
- Localized templates
- Quiet hours configuration

### E13: Integration Layer
- REST APIs for HRIS, WMS, and IDP
- SSO with OAuth2/JWT
- Idempotent webhooks
- OpenAPI documentation

### E14: Audit Trail & Compliance
- Centralized audit logging for all sensitive changes
- Immutable log table with actor, timestamp, before/after states
- Export by date, user, or entity

### E15: Reporting & Analytics
- Attendance, overtime, leave balance, certification status reports
- Safety KPIs dashboard
- CSV/PDF export
- Role-based access control

### E16: Mobile Access (PWA)
- Responsive views for clock-in/out, schedules, leave requests
- Installable PWA manifest
- Offline queue with conflict resolution

### E17: Onboarding & Offboarding Workflow
- Automated provisioning for new hires
- Deprovision access and assets on termination
- Task generation for training and asset assignment

### E18: Localization & Multi-Tenant
- Multi-warehouse tenant isolation
- Locale-specific date/time, currency, language
- Timezone-aware timestamps

### E19: Observability & Monitoring
- Prometheus metrics at /actuator/prometheus
- Structured JSON logs with correlation IDs
- Distributed tracing with OpenTelemetry

### E20: CI/CD & Deployment Automation
- GitHub Actions/GitLab CI pipeline
- Build, test, security scan, Docker image push
- Staging auto-deploy, production manual approval
- Rollback capability

## Prerequisites

- Java 17 or higher
- Maven 3.8+
- PostgreSQL 13+
- Docker (optional, for containerized deployment)

## Database Setup

1. Create PostgreSQL database:
```sql
CREATE DATABASE warehouse_employee;
CREATE USER warehouse_user WITH PASSWORD 'warehouse_pass';
GRANT ALL PRIVILEGES ON DATABASE warehouse_employee TO warehouse_user;
```

2. Update `application.properties` with your database credentials if different.

## Building the Application

```bash
# Clone the repository
git clone <repository-url>
cd SpringBootProject

# Build with Maven
mvn clean install

# Run tests
mvn test
```

## Running the Application

```bash
# Run with Maven
mvn spring-boot:run

# Or run the JAR file
java -jar target/employee-management-1.0.0.jar
```

The application will start on port 8080 by default.

## Accessing the Application

- **Health Check**: http://localhost:8080/actuator/health
- **API Documentation**: http://localhost:8080/swagger-ui.html
- **OpenAPI Spec**: http://localhost:8080/api-docs
- **Prometheus Metrics**: http://localhost:8080/actuator/prometheus

## API Endpoints

### Employee Management
- `POST /employees` - Create new employee
- `GET /employees` - List all employees (paginated)
- `GET /employees/{id}` - Get employee by ID
- `PUT /employees/{id}` - Update employee
- `DELETE /employees/{id}` - Soft-delete employee

### Attendance
- `POST /attendance/clock-in` - Clock in
- `POST /attendance/clock-out` - Clock out
- `GET /attendance` - List attendance records
- `POST /attendance/corrections` - Request correction

### Shifts
- `POST /shifts` - Create shift template
- `GET /shifts` - List shifts
- `POST /shifts/assign` - Assign shifts to employees

### Leave
- `POST /leave/request` - Request leave
- `PUT /leave/{id}/approve` - Approve leave
- `PUT /leave/{id}/deny` - Deny leave
- `GET /leave` - List leave requests

### Certifications
- `POST /certifications` - Add certification
- `GET /certifications` - List certifications
- `GET /certifications/expiring` - Get expiring certifications

### Safety Incidents
- `POST /safety/incidents` - Report incident
- `GET /safety/incidents` - List incidents
- `PUT /safety/incidents/{id}/status` - Update incident status
- `GET /safety/osha-export` - Export OSHA report

### Assets
- `POST /assets` - Add asset
- `POST /assets/{id}/checkout` - Check out asset
- `POST /assets/{id}/checkin` - Check in asset
- `GET /assets/overdue` - List overdue returns

## Security

### Roles
- **ADMIN**: Full access to all endpoints
- **HR**: Employee management, leave, certifications
- **SUPERVISOR**: Team management, attendance approval, shift assignment
- **WORKER**: Personal data, clock-in/out, leave requests

### Authentication
The application supports OAuth2/JWT authentication. Configure the JWT issuer URI in `application.properties`:
```properties
spring.security.oauth2.resourceserver.jwt.jwk-set-uri=https://your-idp.com/.well-known/jwks.json
```

## Database Migrations

Flyway is configured to run migrations automatically on startup. Migration scripts are located in `src/main/resources/db/migration/`.

To create a new migration:
1. Create a new file: `V{version}__description.sql`
2. Add your SQL statements
3. Restart the application

## Testing

Unit tests should be added in `src/test/java/com/warehouse/employee/`. The project is structured to support:
- Repository tests with @DataJpaTest
- Service tests with @SpringBootTest
- Controller tests with @WebMvcTest
- Integration tests with @SpringBootTest

## Monitoring and Observability

### Health Check
```bash
curl http://localhost:8080/actuator/health
```

### Prometheus Metrics
```bash
curl http://localhost:8080/actuator/prometheus
```

### Logs
Structured JSON logs are written to stdout with correlation IDs for distributed tracing.

## Deployment

### Docker
```bash
# Build Docker image
docker build -t warehouse-employee-management:1.0.0 .

# Run container
docker run -p 8080:8080   -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/warehouse_employee   -e SPRING_DATASOURCE_USERNAME=warehouse_user   -e SPRING_DATASOURCE_PASSWORD=warehouse_pass   warehouse-employee-management:1.0.0
```

### Kubernetes
Kubernetes manifests should be created for:
- Deployment
- Service
- ConfigMap (for application.properties)
- Secret (for database credentials)
- Ingress (for external access)

## CI/CD Pipeline

The project includes a CI/CD pipeline configuration for:
1. Build and test on PR
2. Security scanning
3. Docker image build and push
4. Deploy to staging (automatic)
5. Deploy to production (manual approval)
6. Rollback capability

## Contributing

1. Create a feature branch
2. Make your changes
3. Write/update tests
4. Submit a pull request
5. Ensure CI pipeline passes

## License

Proprietary - All rights reserved

## Support

For issues and questions, please contact the development team.

## Roadmap

- [ ] Complete unit test coverage
- [ ] Add integration tests
- [ ] Implement remaining service and controller layers
- [ ] Add DTO validation
- [ ] Implement notification service
- [ ] Add mobile PWA frontend
- [ ] Implement payroll export connectors
- [ ] Add reporting dashboard
- [ ] Implement multi-tenant support
- [ ] Add localization for multiple languages

## Version History

### 1.0.0 (Current)
- Initial release
- Core domain models and repositories
- Security configuration
- Database migrations
- Actuator and monitoring setup
- OpenAPI documentation

---

**Note**: This is a foundational codebase. Additional service layers, controllers, DTOs, and comprehensive unit tests need to be implemented for full functionality. The structure provided follows Spring Boot best practices and is ready for incremental development and testing.