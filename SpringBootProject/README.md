# Warehouse Employee Management System

This is a modular Spring Boot application for managing warehouse employees, attendance, scheduling, safety, and more.

## Build & Run

### Prerequisites
- Java 17+
- Maven 3.8+

### Build
```
mvn clean install
```

### Run
```
mvn spring-boot:run
```

The application will start on port `8080` by default.

### Health Check
Visit [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health) to verify the service is running.

## Project Structure
- `src/main/java/com/warehouse/` - Main application code
- `src/main/resources/` - Configuration files
- `src/test/java/com/warehouse/` - JUnit tests

## Modules
- Employee CRUD
- Scheduling & Attendance
- Safety & Compliance
- RBAC & Security
- Reporting & Analytics

## Documentation
OpenAPI docs available at `/swagger-ui.html` after startup.
