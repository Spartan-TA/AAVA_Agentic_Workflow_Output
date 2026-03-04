# Warehouse Employee Management System (EMS)

## Overview
A production-ready Spring Boot application for managing warehouse employees, attendance, scheduling, safety, assets, and more.

## Build & Run

### Prerequisites
- Java 17+
- Maven 3.8+
- PostgreSQL (or compatible DB)

### Steps
1. Clone the repository:
   ```bash
   git clone <repo-url>
   cd SpringBootProject
   ```
2. Build the project:
   ```bash
   mvn clean install
   ```
3. Run the application:
   ```bash
   mvn spring-boot:run
   ```
4. Access API docs:
   - Swagger UI: `http://localhost:8080/swagger-ui.html`
   - Actuator Health: `http://localhost:8080/actuator/health`

### Database Migration
- Flyway will auto-run migrations from `src/main/resources/db/migration`.

### Docker
To run with Docker:
```bash
docker build -t warehouse-ems .
docker run -p 8080:8080 warehouse-ems
```

## CI/CD
- GitHub Actions workflow in `.github/workflows/ci-cd.yml`.

## Modules
- Employee CRUD
- Attendance & Scheduling
- RBAC & Security
- Leave Management
- Certifications & Safety
- Asset Assignment
- Performance Reviews
- Payroll Export
- Notifications
- Audit & Reporting
- Mobile Access (PWA)
- Integration Layer

## Contact
For support, contact the development team.
