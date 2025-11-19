# Warehouse Employee Management System

## Overview
A comprehensive Spring Boot-based system for managing warehouse employees, attendance, scheduling, safety, and integrations. Implements 20 epics including RBAC, reporting, mobile access, and CI/CD automation.

## Project Structure
- **core**: Employee master data, domain entities, CRUD APIs
- **attendance**: Time & attendance tracking
- **scheduling**: Shift & schedule management
- **safety**: Safety incidents & OSHA reporting
- **integration**: HRIS/WMS/API connectors
- **reporting**: Analytics & operational reports
- **mobile**: PWA/mobile access

## Build & Run

### Prerequisites
- Java 17+
- Maven 3.8+

### Build
```bash
mvn clean install
```

### Run (Core Module Example)
```bash
cd core
mvn spring-boot:run
```

Application runs on `http://localhost:8080`.

### API Documentation
- Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- OpenAPI: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

### Health Check
- Actuator: [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)

### Testing
```bash
mvn test
```

## Modules & Epics
Refer to the technical design document for epic breakdown and acceptance criteria.

## Configuration
- See `application.yml` for DB, actuator, and OpenAPI settings.

## Contribution
- Fork, branch, and submit PRs for new features or bug fixes.

## License
MIT
