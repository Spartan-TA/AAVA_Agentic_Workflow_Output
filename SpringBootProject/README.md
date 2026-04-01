# Warehouse Employee Management System

## Overview
A production-ready Spring Boot application for managing warehouse employees, attendance, scheduling, safety, assets, and integrations. Built with best practices and modular architecture.

## Build & Run

### Prerequisites
- Java 17+
- Maven 3.8+
- PostgreSQL 13+

### Setup
1. Clone the repository.
2. Configure `application.properties` for your database and integrations.
3. Run Flyway migrations:
   ```bash
   mvn flyway:migrate
   ```
4. Build and run:
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

### API Documentation
- Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- OpenAPI JSON: [http://localhost:8080/api-docs](http://localhost:8080/api-docs)

### Health Check
- Actuator Health: [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)

## Project Structure
- `com.warehouse.employee` - Employee domain
- `com.warehouse.scheduling` - Shift & schedule management
- `com.warehouse.attendance` - Time & attendance
- `com.warehouse.safety` - Safety incidents
- `com.warehouse.config` - Configuration
- `com.warehouse.security` - Security & RBAC
- `com.warehouse.integration` - Integrations

## Features
- Employee CRUD with soft-delete
- RBAC with Spring Security (ADMIN, HR, SUPERVISOR, WORKER)
- Attendance clock-in/out with corrections
- Shift templates, schedules, assignments
- Leave management & balances
- Certification tracking & alerts
- Safety incident reporting & OSHA exports
- Asset assignment & tracking
- Performance reviews & goals
- Payroll export integration (SFTP/API)
- Notifications (email/SMS/in-app)
- Audit logging for compliance
- Reporting & analytics (CSV/PDF)
- Mobile-friendly (PWA)
- Onboarding/offboarding workflows
- Integration layer (HRIS/WMS)

## Testing
- Unit tests for services
- Integration tests for controllers
- Security tests

## Contributing
1. Fork the repo
2. Create a feature branch
3. Submit a PR

## License
MIT
