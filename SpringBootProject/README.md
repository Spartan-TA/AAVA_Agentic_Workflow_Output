# Warehouse Employee Management System

This is a production-ready Spring Boot application for managing warehouse employees, attendance, scheduling, safety, and more.

## Features
- Employee CRUD
- Attendance tracking
- Shift and schedule management
- Leave and absence management
- Certification tracking
- Safety incidents and OSHA reporting
- Equipment and asset assignment
- Performance reviews
- Payroll export integration
- Notifications and announcements
- Integration layer (HRIS/WMS APIs)
- Audit trail and compliance
- Reporting and analytics
- Mobile access (PWA)
- Onboarding/offboarding workflow
- Localization and multi-tenant support
- Observability and monitoring
- CI/CD pipeline

## Prerequisites
- Java 17+
- Maven 3.8+
- PostgreSQL 13+

## Setup
1. Clone the repository
2. Configure your PostgreSQL database and update `src/main/resources/application.yml` if needed
3. Build the project:
   ```bash
   mvn clean install
   ```
4. Run the application:
   ```bash
   mvn spring-boot:run
   ```
5. Access the health endpoint:
   - [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)

## API Documentation
- Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- OpenAPI Docs: [http://localhost:8080/api-docs](http://localhost:8080/api-docs)

## Database Migrations
- Flyway will automatically run baseline and migration scripts on startup.

## Project Structure
- `src/main/java/com/company/warehousemgmt/` - Main application code
- `src/main/resources/` - Configuration and migration scripts

## CI/CD
- See `.github/workflows/ci-cd.yml` for pipeline configuration

---

For more details, see the technical design document and code comments.
