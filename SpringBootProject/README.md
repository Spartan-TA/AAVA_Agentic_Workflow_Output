# Warehouse Employee Management System (EMS)

## Overview
A comprehensive Spring Boot application for managing warehouse employees, attendance, scheduling, safety, assets, and more.

## Build & Run

### Prerequisites
- Java 11+
- Maven 3.6+
- PostgreSQL (or update application.yml for your DB)

### Steps
1. Clone the repository
2. Configure `src/main/resources/application.yml` for your DB credentials
3. Run DB migrations (Flyway auto-runs on startup)
4. Build:
   ```bash
   mvn clean install
   ```
5. Run:
   ```bash
   mvn spring-boot:run
   ```
6. Access:
   - API: http://localhost:8080/api/
   - Actuator: http://localhost:8080/actuator/health
   - OpenAPI docs: http://localhost:8080/swagger-ui.html

## Features
- Employee CRUD
- RBAC Security
- Attendance & Scheduling
- Leave Management
- Training & Certification
- Safety Incidents
- Asset Assignment
- Performance Reviews
- Payroll Export
- Notifications
- Integration Layer
- Audit Trail
- Reporting & Analytics
- Mobile PWA
- Onboarding/Offboarding
- Localization
- Advanced Scheduling
- Self-Service Portal

## Testing
Run:
```bash
mvn test
```

## Contact
For support, contact devops@company.com
