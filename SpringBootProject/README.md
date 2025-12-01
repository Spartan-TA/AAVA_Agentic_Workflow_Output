# Warehouse Employee Management System (EMS)

## Overview
Warehouse EMS is a comprehensive Spring Boot 3.x application for managing warehouse employees, attendance, scheduling, safety, assets, reviews, notifications, integrations, audit, and reporting. It supports RBAC, JWT/OAuth2, PostgreSQL, Flyway migrations, and OpenAPI documentation.

## Features
- Employee Master Data CRUD
- Role-Based Access Control (RBAC)
- Time & Attendance (Clock In/Out)
- Shift & Schedule Management
- Leave & Absence Management
- Training & Certification Tracking
- Safety Incidents & OSHA Reporting
- Equipment & Asset Assignment
- Performance Reviews & Goals
- Payroll Export Integration
- Notifications & Announcements
- Integration Layer (HRIS/WMS APIs)
- Audit Trail & Compliance
- Reporting & Analytics
- Mobile Access (PWA)
- Onboarding & Offboarding Workflow
- Localization & Multi-Site
- Advanced Scheduling (AI/Optimization)
- Self-Service Portal

## Tech Stack
- Spring Boot 3.x
- Maven
- PostgreSQL
- Flyway
- Spring Security (JWT/OAuth2)
- OpenAPI/Swagger
- Actuator

## Build & Run
1. **Clone the repository:**
   ```bash
   git clone <repo-url>
   cd SpringBootProject
   ```
2. **Configure PostgreSQL:**
   - Create database `warehouse_ems`
   - Update `src/main/resources/application.yml` with DB credentials
3. **Run migrations:**
   - Flyway will auto-run on startup
4. **Build the project:**
   ```bash
   mvn clean install
   ```
5. **Run the application:**
   ```bash
   mvn spring-boot:run
   ```
6. **Access API docs:**
   - [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
7. **Actuator health:**
   - [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)

## Testing
- Unit and integration tests can be added under `src/test/java`
- Run tests:
  ```bash
  mvn test
  ```

## Project Structure
```
com.warehouse.ems
âââ config
âââ employee
âââ scheduling
âââ attendance
âââ safety
âââ asset
âââ review
âââ notification
âââ integration
âââ audit
âââ reporting
âââ exception
```

## Contribution
- Fork and submit PRs for new features or bug fixes.

## License
MIT
