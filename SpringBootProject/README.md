# Warehouse Employee Management System (EMS)

A production-ready Spring Boot application for managing warehouse employees, attendance, scheduling, safety, assets, payroll, notifications, integrations, and more.

## Build & Run

1. **Database Setup:**
   - Ensure PostgreSQL is running and create a database named `warehouseems`.
   - Create user `warehouseems` with password `warehouseems123` and grant privileges.

2. **Build:**
   - Run `mvn clean install` from the project root.

3. **Run:**
   - Start the application: `mvn spring-boot:run`
   - The app runs on port `8080` by default.

4. **API Docs:**
   - Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

5. **Actuator Health:**
   - [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)

## Modules
- **employee:** Employee CRUD, soft-delete, pagination
- **attendance:** Clock-in/out, shift association, corrections, CSV export
- **scheduling:** Shift templates, assignments, conflict detection
- **safety:** Incident recording, investigation, OSHA reporting
- **leave:** PTO/sick/unpaid leave, accrual, approval workflow
- **training:** Certification tracking, expiry alerts
- **asset:** Equipment assignment, check-in/out
- **review:** Performance reviews, goals, PDF export
- **payroll:** Payroll export, provider mapping, SFTP/API
- **notification:** In-app/email/SMS, templates, delivery tracking
- **integration:** HRIS/WMS APIs, webhooks, OpenAPI
- **audit:** Immutable logging, export, compliance
- **reporting:** Analytics, dashboards, CSV/PDF export
- **mobile:** PWA manifest, responsive endpoints
- **onboarding:** Automated workflows, task generation
- **config:** Security, localization, common config
- **common:** Exception handling, utilities

## Security
- RBAC: ADMIN, HR, SUPERVISOR, WORKER
- JWT/OAuth2 support
- Method-level security

## Database Migrations
- Flyway scripts in `src/main/resources/db/migration`

## Testing
- Unit test structure in `src/test/java`

## Localization
- Multi-language support via `application.yml`

## Contact
- For issues, contact the development team.
