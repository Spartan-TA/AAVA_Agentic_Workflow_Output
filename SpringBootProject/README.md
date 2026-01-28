# Warehouse Employee Management System (WMS EMS)

## Build & Run

### Prerequisites
- Java 11+
- Maven 3.6+
- PostgreSQL

### Setup

1. Clone the repository.
2. Configure `application.properties` with your database credentials.
3. Run Flyway migrations:
   ```
   mvn flyway:migrate
   ```
4. Build and run the application:
   ```
   mvn clean install
   mvn spring-boot:run
   ```
5. Access API docs at [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
6. Health endpoint: [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)

## Modules

- Employee Management
- Attendance & Scheduling
- Leave & Certification
- Safety & Asset
- Review & Payroll
- Notification & Integration
- Audit & Reporting
- Mobile, Onboarding, Localization, AI, Self-Service

## Testing

Run unit tests:
```
mvn test
```