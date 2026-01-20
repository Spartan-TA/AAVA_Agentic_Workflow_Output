# Warehouse Employee Management System (EMS)

## Overview
Production-ready Spring Boot 3.x application for managing warehouse employees, attendance, scheduling, safety, assets, payroll, notifications, integrations, audit, reporting, mobile access, onboarding/offboarding, localization, advanced scheduling, and document management.

## Build & Run

1. **Database Setup**
   - PostgreSQL 14+
   - Create database `warehouse_ems`
   - Create user `ems_user` with password `ems_password`
   - Flyway migrations will auto-run on startup

2. **Build**
   ```
   mvn clean install
   ```

3. **Run**
   ```
   mvn spring-boot:run
   ```

4. **API Docs**
   - OpenAPI/Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

5. **Health Check**
   - Actuator: [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)

## Modules
- employee, scheduling, attendance, safety, config, common, audit, integration, notification, reporting, mobile, onboarding, etc.

## Security
- RBAC: ADMIN, HR, SUPERVISOR, WORKER
- JWT/OAuth2 authentication

## Error Handling
- Global exception handler with validation

## Audit
- Aspect-based logging for all changes

## Reporting
- CSV/PDF export endpoints

## Mobile
- PWA manifest, offline queue for clock events

## Localization
- Multi-warehouse, locale support

## Document Management
- Versioning, retention, e-signature

## Testing
- Ready for unit tests (Spring Boot Starter Test, Mockito)

## Contact
- For support, contact the EMS engineering team.