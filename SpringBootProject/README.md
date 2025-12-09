# Warehouse Employee Management System (EMS)

This is a comprehensive Spring Boot application for managing warehouse employees, attendance, scheduling, safety, equipment, and more.

## Features
- Employee CRUD & master data
- Role-based access control (RBAC)
- Time & attendance (clock in/out)
- Shift & schedule management
- Leave & absence management
- Training & certification tracking
- Safety incidents & OSHA reporting
- Equipment & asset assignment
- Performance reviews & goals
- Payroll export integration
- Notifications & announcements
- Integration layer (HRIS/WMS APIs)
- Audit trail & compliance
- Reporting & analytics
- Mobile access (PWA)
- Onboarding & offboarding workflows

## Build & Run

### Prerequisites
- Java 17+
- Maven 3.8+
- PostgreSQL (or update `application.yml` for your DB)

### Steps
1. Clone the repository:
   ```bash
   git clone https://github.com/Spartan-TA/AAVA_Agentic_Workflow_Output.git
   cd AAVA_Agentic_Workflow_Output/SpringBootProject
   ```
2. Update `src/main/resources/application.yml` with your database credentials.
3. Run Flyway migrations:
   ```bash
   mvn flyway:migrate
   ```
4. Build the project:
   ```bash
   mvn clean install
   ```
5. Start the application:
   ```bash
   mvn spring-boot:run
   ```
6. Access API docs:
   - [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
7. Health check:
   - [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)

## Testing
Run unit and integration tests:
```bash
mvn test
```

## Project Structure
- `src/main/java/com/warehouse/ems/employee` - Employee domain
- `src/main/java/com/warehouse/ems/attendance` - Attendance domain
- `src/main/java/com/warehouse/ems/scheduling` - Scheduling domain
- `src/main/java/com/warehouse/ems/leave` - Leave domain
- `src/main/java/com/warehouse/ems/training` - Training domain
- `src/main/java/com/warehouse/ems/safety` - Safety domain
- `src/main/java/com/warehouse/ems/equipment` - Equipment domain
- `src/main/java/com/warehouse/ems/config` - Configuration classes

## Migration Scripts
Place Flyway migration scripts in `src/main/resources/db/migration`.

## License
MIT
