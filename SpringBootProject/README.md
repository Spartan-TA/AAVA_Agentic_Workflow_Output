# Warehouse Employee Management System

## Overview
A Spring Boot application for managing warehouse employees, attendance, scheduling, safety, certifications, and more.

## Build Instructions

1. Ensure you have Java 17+ and Maven installed.
2. Clone the repository.
3. Configure your PostgreSQL database in `src/main/resources/application.properties`.
4. Run Flyway migrations automatically on startup.

```
mvn clean install
```

## Run Instructions

```
mvn spring-boot:run
```

Application runs on port 8080 by default.

## API Documentation

- Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- OpenAPI JSON: [http://localhost:8080/api-docs](http://localhost:8080/api-docs)

## Testing Instructions

Run unit and integration tests:

```
mvn test
```

## Modules
- Employee Management (CRUD)
- Role-Based Access Control (RBAC)
- Time & Attendance
- Shift & Schedule Management
- Leave & Absence Management
- Training & Certification Tracking
- Safety Incidents & OSHA Reporting
- Equipment & Asset Assignment
- Performance Reviews & Goals
- Payroll Export Integration
- Notifications & Announcements
- Integration Layer
- Audit Trail & Compliance
- Reporting & Analytics
- Mobile Access (PWA)
- Onboarding & Offboarding

## Contact
For issues or contributions, please open a GitHub issue or pull request.
