# Warehouse Employee Management System (EMS)

A modular Spring Boot application for managing warehouse employees, attendance, scheduling, safety, assets, and more.

## Features
- Employee CRUD & master data
- Role-based access control (RBAC)
- Attendance clock-in/out
- Shift & schedule management
- Leave & absence management
- Certification tracking
- Safety incident reporting
- Asset assignment
- Performance reviews
- Payroll export
- Notifications & announcements
- Integration with HRIS/WMS
- Audit trail
- Reporting & analytics
- Mobile PWA support
- Onboarding/offboarding workflows

## Build & Run

### Prerequisites
- Java 17+
- Maven 3.8+
- PostgreSQL (default config: `warehouse_ems`)

### Steps
```bash
# Clone repository
$ git clone <repo-url>
$ cd WarehouseEMS

# Build
$ mvn clean install

# Run
$ mvn spring-boot:run
```

Application runs on [http://localhost:8080](http://localhost:8080)

### Database Migration
Flyway runs automatically on startup. See `src/main/resources/db/migration` for SQL scripts.

### API Docs
OpenAPI UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

## Module Structure
- `employee/` - Employee management
- `attendance/` - Time & attendance
- `scheduling/` - Shift & schedule
- `leave/` - Leave management
- `certification/` - Training & certification
- `safety/` - Safety incidents
- `asset/` - Asset assignment
- `review/` - Performance reviews
- `payroll/` - Payroll export
- `notification/` - Notifications
- `integration/` - HRIS/WMS APIs
- `audit/` - Audit trail
- `reporting/` - Reporting
- `onboarding/` - Onboarding/offboarding
- `common/` - Shared code

## Health Check
Actuator: [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)
