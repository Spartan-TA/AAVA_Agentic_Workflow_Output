# Warehouse Employee Management System (EMS)

A production-ready Spring Boot application for managing warehouse employees, attendance, scheduling, safety, and more.

## Features
- Employee CRUD (with soft-delete)
- Role-Based Access Control (ADMIN, HR, SUPERVISOR, WORKER)
- Attendance, scheduling, leave, training, safety, equipment, performance, payroll, notifications, integrations, audit, reporting, document management
- OpenAPI/Swagger documentation
- Flyway database migrations
- Actuator health endpoints

## Build & Run

### Prerequisites
- Java 17+
- Maven 3.8+
- PostgreSQL

### Steps
1. Clone the repository
2. Configure your database in `src/main/resources/application.yml`
3. Run migrations: `mvn flyway:migrate`
4. Build: `mvn clean install`
5. Run: `mvn spring-boot:run`

Application runs on [http://localhost:8080](http://localhost:8080)

## API Documentation
- Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- OpenAPI JSON: [http://localhost:8080/api-docs](http://localhost:8080/api-docs)

## Health Check
- [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)

## Project Structure
- `com.warehouse.ems.employee` - Employee domain (CRUD)
- `com.warehouse.ems.security` - Security and RBAC
- `com.warehouse.ems.attendance` - Time & attendance
- `com.warehouse.ems.scheduling` - Shift & schedule management
- ... (see source for all modules)

## Contributing
Pull requests welcome. Please follow code style and add tests.

## License
MIT
