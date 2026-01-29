# Warehouse Employee Management System (WMS)

This is a Spring Boot application for managing warehouse employees, attendance, scheduling, safety, and audit logs.

## Prerequisites
- Java 17+
- Maven 3.8+
- PostgreSQL database

## Setup
1. Clone the repository:
   ```bash
   git clone <repo-url>
   cd SpringBootProject
   ```
2. Configure your database in `src/main/resources/application.yml`.
3. Run Flyway migrations:
   ```bash
   mvn flyway:migrate
   ```

## Build
```bash
mvn clean install
```

## Run
```bash
mvn spring-boot:run
```

The application will start on port 8080.

## API Documentation
Swagger UI available at: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

## Modules
- Employee Management
- Attendance Tracking
- Shift Scheduling
- Safety Incident Reporting
- Audit Logging

## Useful Endpoints
- `/api/employees` - Employee CRUD
- `/api/attendance` - Attendance records
- `/api/scheduling` - Shift templates and schedules
- `/api/safety` - Safety incidents
- `/api/audit` - Audit logs

## Actuator Endpoints
- `/actuator/health`
- `/actuator/info`
- `/actuator/metrics`
