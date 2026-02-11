# Warehouse Employee Management System

This is a production-ready Spring Boot 3.x application for managing warehouse employees, attendance, scheduling, safety, and more.

## Build & Run

### Prerequisites
- Java 17+
- Maven 3.8+

### Build
```bash
mvn clean install
```

### Run (H2 in-memory DB)
```bash
mvn spring-boot:run
```

The application will start on [http://localhost:8080](http://localhost:8080).

### Health Check
- Actuator health endpoint: [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)

### API Docs
- OpenAPI UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

### Database Migrations
- Flyway runs automatically on startup.

### Profiles
- Default: H2 in-memory
- For PostgreSQL, update `application.yml` datasource section.

## Project Structure
- `com.wms.employee` - Employee management
- `com.wms.scheduling` - Shift & schedule management
- `com.wms.attendance` - Time & attendance
- `com.wms.safety` - Safety incidents
- `com.wms.config` - Configuration
- `com.wms.common` - Shared utilities

## License
MIT
