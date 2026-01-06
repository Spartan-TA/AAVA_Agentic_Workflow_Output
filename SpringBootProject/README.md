# Warehouse Employee Management System (EMS)

This is a production-ready Spring Boot application for managing warehouse employees, shifts, attendance, safety, and more.

## Features
- Employee CRUD (soft delete, pagination, filtering)
- Role-Based Access Control (ADMIN, HR, SUPERVISOR, WORKER)
- Time & Attendance
- Shift & Schedule Management
- Leave & Absence Management
- Training & Certification Tracking
- Safety Incidents & OSHA Reporting
- Equipment & Asset Assignment
- Performance Reviews & Goals
- Payroll Export Integration
- Notifications & Announcements
- Integration Layer (HRIS, WMS, IDP)
- Audit Trail & Compliance
- Reporting & Analytics
- Mobile Access (PWA)
- Onboarding & Offboarding
- Localization & Multi-Tenant
- Observability & Monitoring
- Deployment & CI/CD

## Build & Run

### Prerequisites
- Java 17+
- Maven 3.8+
- PostgreSQL (default config)

### Steps
1. Clone the repository
2. Update `src/main/resources/application.yml` with your DB credentials
3. Run database migrations (Flyway/Liquibase)
4. Build the project:
   ```bash
   mvn clean install
   ```
5. Run the application:
   ```bash
   mvn spring-boot:run
   ```
6. Access API at `http://localhost:8080`
7. Health endpoint: `http://localhost:8080/actuator/health`
8. Swagger UI: `http://localhost:8080/swagger-ui.html`

## Security
- OAuth2/JWT and RBAC enabled
- In-memory users for local dev (see `SecurityConfig.java`)

## Testing
- Unit and integration tests can be added under `src/test/java`

## License
MIT
