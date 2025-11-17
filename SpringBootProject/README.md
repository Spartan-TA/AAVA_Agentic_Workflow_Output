# Warehouse EMS Spring Boot Project

## Overview
This project implements the Warehouse Employee Management System (EMS) as described in the technical design document. It covers all 27 user stories, including employee master data, attendance, scheduling, safety, asset management, payroll integration, notifications, and more.

## Technology Stack
- Spring Boot 3.2.0
- Java 17
- Maven (multi-module)
- PostgreSQL
- Flyway (DB migrations)
- Spring Security (JWT/OAuth2)
- OpenAPI 3.0 (Swagger)
- Actuator

## Project Structure
```
SpringBootProject/
âââ README.md
âââ pom.xml
âââ src/
â   âââ main/
â   â   âââ java/
â   â   â   âââ com/
â   â   â       âââ warehouse/
â   â   â           âââ ems/
â   â   â               âââ employee/
â   â   â               âââ attendance/
â   â   â               âââ scheduling/
â   â   â               âââ safety/
â   â   â               âââ asset/
â   â   â               âââ payroll/
â   â   â               âââ notification/
â   â   â               âââ integration/
â   â   â               âââ audit/
â   â   â               âââ config/
â   â   âââ resources/
â   â       âââ application.yml
â   â       âââ db/migration/
â   âââ test/
â       âââ java/
â           âââ com/
â               âââ warehouse/
â                   âââ ems/
```

## Build & Run
1. **Clone the repository:**
   ```bash
   git clone <repo-url>
   cd SpringBootProject
   ```
2. **Configure PostgreSQL:**
   - Update `src/main/resources/application.yml` with your DB credentials.
3. **Run DB migrations:**
   - Flyway will auto-run on app startup.
4. **Build the project:**
   ```bash
   mvn clean install
   ```
5. **Run the application:**
   ```bash
   mvn spring-boot:run
   ```
   - The app runs on port 8080 by default.

## API Documentation
- Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- OpenAPI spec: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

## Testing
- Unit tests: `mvn test`
- Integration tests: `mvn verify`
- Test coverage: Target 80%+

## Security
- RBAC: Roles (ADMIN, HR, SUPERVISOR, WORKER)
- Auth: JWT/OAuth2 (toggle via config)

## Modules
- **employee:** CRUD, filtering, OpenAPI
- **attendance:** Clock in/out, corrections, export
- **scheduling:** Shifts, templates, conflict detection
- **safety:** Certification, incident reporting
- **asset:** Assignment, condition tracking
- **payroll:** Export, integration
- **notification:** Announcements, alerts
- **integration:** HRIS/WMS APIs
- **audit:** Centralized logging

## Contribution
- Follow Java coding standards in `Java_Best_Practice.docx`
- All code must be documented with Javadoc
- Use SLF4J for logging

## Contact
- For technical/design questions, contact the Architecture Team.

---
