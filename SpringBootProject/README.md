# Warehouse Employee Management System (WMS)

This is a Spring Boot application for managing warehouse employees, attendance, scheduling, leave, certifications, safety, assets, performance, notifications, and audit logging.

## Build & Run

1. **Build the project:**
   ```bash
   mvn clean install
   ```
2. **Run the application:**
   ```bash
   mvn spring-boot:run
   ```
3. **API Documentation:**
   - Swagger UI available at `/swagger-ui.html` after starting the app.

## Configuration

- Edit `src/main/resources/application.yml` for database and other settings.
- Database migrations are managed by Flyway in `src/main/resources/db/migration/`.

## Modules
- Employee Management
- Attendance Tracking
- Shift Scheduling
- Leave Management
- Training & Certification
- Safety & Compliance
- Asset Tracking
- Performance Reviews
- Notifications & Announcements
- Audit Logging

## Security
- JWT and OAuth2 authentication

## Localization
- Multi-language support enabled
