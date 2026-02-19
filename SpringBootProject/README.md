# Warehouse Employee Management System (WEMS)

A production-ready Spring Boot 3.2.x application for comprehensive warehouse employee management.

## Features
- Employee CRUD with soft delete
- Role-based access control (JWT, OAuth2, RBAC)
- Time & Attendance (clock in/out, geofence)
- Shift & Schedule Management
- Leave & Absence Management
- Training & Certification Tracking
- Safety Incidents & OSHA Reporting
- Equipment & Asset Assignment
- Performance Reviews & Goals
- Payroll Export Integration
- Notifications & Announcements
- HRIS/WMS API Integration
- Audit Trail & Compliance
- Reporting & Analytics
- Mobile Access (PWA)
- Onboarding & Offboarding Workflow
- Localization & Multi-Tenant
- Observability & Monitoring
- CI/CD & Deployment Automation

## Tech Stack
- Java 17, Spring Boot 3.2.x
- PostgreSQL 15.x
- Flyway for DB migrations
- Spring Security (JWT)
- Spring Data JPA
- OpenAPI 3.0 (Swagger)
- JUnit 5
- Maven

## Getting Started

### Prerequisites
- Java 17+
- Maven 3.8+
- PostgreSQL 15+

### Setup
1. Clone the repo:
   ```
   git clone <repo-url>
   cd SpringBootProject
   ```
2. Configure `src/main/resources/application.yml` with your DB credentials.
3. Run DB migrations:
   ```
   mvn flyway:migrate
   ```
4. Build the project:
   ```
   mvn clean install
   ```
5. Run the application:
   ```
   mvn spring-boot:run
   ```

### API Docs
- Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

### Running Tests
```
mvn test
```

### CI/CD
- See `.github/workflows/ci-cd.yml` for GitHub Actions pipeline.

## Contributing
- Fork the repo, create a branch, submit PRs.

## License
MIT
