# Warehouse Employee Management System

## Overview
This Spring Boot application manages warehouse employee data, attendance, scheduling, safety, assets, and more. It is designed for modularity, security, and compliance, following best practices and supporting PostgreSQL, JWT/OAuth2, and OpenAPI documentation.

## Build & Run Instructions

### Prerequisites
- Java 17 or 21 (LTS)
- Maven 3.8+
- PostgreSQL 14+

### Setup
1. Clone the repository:
   ```bash
   git clone <repo-url>
   cd SpringBootProject
   ```
2. Configure database credentials in `src/main/resources/application.yml`.
3. Run database migrations (Flyway runs automatically on startup).

### Build
```bash
mvn clean install
```

### Run
```bash
mvn spring-boot:run
```

The application runs on port 8080. Health endpoint: `GET /actuator/health`

### API Documentation
- OpenAPI/Swagger UI: `GET /api-docs` or `/swagger-ui.html`

### Testing
```bash
mvn test
```

### Modules
- Employee CRUD
- Attendance (Clock In/Out)
- Shift & Schedule Management
- Leave & Absence
- Training & Certification
- Safety Incidents
- Asset Assignment
- Performance Reviews
- Payroll Export
- Notifications
- Integration Layer
- Audit Trail
- Reporting & Analytics
- Mobile Access (PWA)
- Onboarding/Offboarding

## Contact
For issues or contributions, please open a GitHub issue or pull request.
