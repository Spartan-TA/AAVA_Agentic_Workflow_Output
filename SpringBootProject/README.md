# Warehouse Employee Management System

A production-ready Spring Boot 3.x application for managing warehouse employees, attendance, scheduling, safety, assets, and more.

## Features

- Employee CRUD with RBAC, soft-delete, pagination
- Time & Attendance (clock in/out, geolocation, corrections)
- Shift & Schedule Management
- Leave & Absence Management
- Training & Certification Tracking
- Safety Incident Reporting (OSHA)
- Equipment & Asset Assignment
- Performance Reviews & Goals
- Payroll Export Integration
- Notifications & Announcements
- Integration Layer (HRIS/WMS APIs)
- Audit Trail & Compliance
- Reporting & Analytics
- Mobile Access (PWA-ready APIs)
- Onboarding & Offboarding Workflow
- Localization & Multi-Tenant Support
- Observability & Monitoring (Actuator)
- Disaster Recovery & Backup

## Technology Stack

- Spring Boot 3.x, Java 17+
- Spring Data JPA, Spring Security, JWT/OAuth2
- PostgreSQL, Flyway
- OpenAPI 3.0 (Swagger UI)
- Maven

## Getting Started

### Prerequisites

- Java 17+
- Maven 3.8+
- PostgreSQL

### Build & Run

```bash
# Clone the repo
git clone <repo-url>
cd SpringBootProject

# Configure DB in src/main/resources/application.properties

# Run Flyway migrations (auto on startup)
mvn spring-boot:run
```

### API Documentation

- Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- OpenAPI Docs: [http://localhost:8080/api-docs](http://localhost:8080/api-docs)

### Health Check

- [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)

## Testing

```bash
mvn test
```

## License

MIT