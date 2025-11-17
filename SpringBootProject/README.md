# Warehouse Employee Management System

This is a modular Spring Boot application for managing warehouse employees, attendance, scheduling, safety, assets, and more. It is designed for extensibility, compliance, and operational efficiency.

## Modules
- Employee Master Data (CRUD)
- Role Based Access Control (RBAC)
- Time & Attendance
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

## Build & Run

### Prerequisites
- Java 17+
- Maven 3.8+
- Docker (optional, for DB)

### Steps
1. Clone the repository
2. Run database (PostgreSQL recommended)
3. Configure `src/main/resources/application.properties` as needed
4. Build the project:
   ```bash
   mvn clean install
   ```
5. Run the application:
   ```bash
   mvn spring-boot:run
   ```
6. Access API at [http://localhost:8080](http://localhost:8080)
7. Health check: [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)

### Database Migrations
- Flyway/Liquibase runs automatically on startup for baseline and incremental migrations.

## Testing
- Unit and integration tests are in `src/test/java`
- Run tests:
   ```bash
   mvn test
   ```

## API Documentation
- OpenAPI/Swagger UI available at `/swagger-ui.html`

## Contribution
- Follow standard Java/Spring Boot coding practices
- See inline comments and documentation in source files

## License
- MIT
