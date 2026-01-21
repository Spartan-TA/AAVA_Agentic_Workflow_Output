# Warehouse Employee Management System (Warehouse EMS)

A production-ready Spring Boot application for managing warehouse employees, attendance, scheduling, safety, payroll, and more.

## Build & Run

### Prerequisites
- Java 17+
- Maven 3.8+
- PostgreSQL (or compatible DB)

### Build
```bash
mvn clean install
```

### Run (Dev)
```bash
mvn spring-boot:run
```

### Run (Prod)
```bash
SPRING_PROFILES_ACTIVE=prod mvn spring-boot:run
```

### Docker
```bash
docker build -t warehouse-ems .
docker run -p 8080:8080 warehouse-ems
```

## API Documentation
- Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

## Health Check
- Actuator: [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)

## Database Migration
- Flyway runs automatically on startup.

## Profiles
- `dev` (default)
- `prod`

## Modules
- Employee, Scheduling, Attendance, Safety, Leave, Training, Asset, Performance, Payroll, Notification, Integration, Audit, Reporting, Config, Common

## Security
- JWT authentication
- Role-based access control (ADMIN, HR, SUPERVISOR, WORKER)

## License
MIT
