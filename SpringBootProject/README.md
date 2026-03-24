# Warehouse Employee Management System

## Overview
Production-ready Spring Boot application for managing warehouse employees, shifts, attendance, safety, assets, and more. Implements 20 epics as per technical design.

## Build & Run

### Prerequisites
- Java 17+
- Maven 3.8+
- PostgreSQL (default)

### Steps
1. Clone the repository:
   ```bash
   git clone <repo-url>
   cd warehouse-ems
   ```
2. Configure database in `src/main/resources/application.yml` (default: PostgreSQL).
3. Build the project:
   ```bash
   mvn clean install
   ```
4. Run the application:
   ```bash
   mvn spring-boot:run
   ```
5. Access API docs:
   - [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
6. Health check:
   - [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)

## Project Structure
- `src/main/java/com/wms/` - Java source files
- `src/main/resources/` - configs, messages, migrations
- `pom.xml` - Maven dependencies

## Features
- Employee CRUD with soft delete
- RBAC security
- Attendance tracking
- Shift & schedule management
- Leave management
- Training & certification
- Safety incidents & OSHA reporting
- Asset assignment
- Performance reviews
- Payroll export
- Notifications
- Integration APIs
- Audit trail
- Reporting & analytics
- Mobile PWA
- Onboarding/offboarding
- Localization
- Advanced scheduling
- Self-service portal

## Testing
Run unit tests:
```bash
mvn test
```

## License
MIT
