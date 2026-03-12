# Warehouse Employee Management System (EMS)

A comprehensive Spring Boot application for managing warehouse employees, attendance, scheduling, safety, and more.

## Features
- Employee CRUD with soft-delete, pagination, filtering
- Role-based access control (ADMIN, HR, SUPERVISOR, WORKER)
- Time & attendance clock-in/out, corrections workflow
- Shift & schedule management
- Leave & absence management
- Training & certification tracking
- Safety incidents & OSHA reporting
- Equipment & asset assignment
- Performance reviews & goals
- Payroll export integration
- Notifications & announcements
- Integration layer for HRIS/WMS APIs
- Audit trail & compliance
- Reporting & analytics
- Mobile PWA support
- Onboarding & offboarding workflows
- Localization & multi-warehouse
- Advanced scheduling optimization
- Continuous improvement & feedback

## Tech Stack
- Java 17
- Spring Boot 3
- Spring Data JPA
- Spring Security
- Flyway for DB migrations
- PostgreSQL (default, configurable)
- OpenAPI/Swagger
- Maven

## Project Structure
```
SpringBootProject/
âââ pom.xml
âââ README.md
âââ src/
â   âââ main/
â   â   âââ java/com/warehouse/ems/
â   â   â   âââ Application.java
â   â   â   âââ config/
â   â   â   âââ domain/
â   â   â   âââ dto/
â   â   â   âââ repository/
â   â   â   âââ service/
â   â   â   âââ controller/
â   â   â   âââ exception/
â   â   â   âââ util/
â   â   âââ resources/
â   â       âââ application.yml
â   â       âââ db/migration/
â   â       âââ static/
â   âââ test/java/com/warehouse/ems/
```

## Build & Run

1. **Build the project:**
   ```bash
   mvn clean install
   ```
2. **Run the application:**
   ```bash
   mvn spring-boot:run
   ```
   The app will start on [http://localhost:8080](http://localhost:8080)

3. **API Documentation:**
   Visit [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) for OpenAPI docs.

4. **Actuator Health Check:**
   Visit [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)

## Database
- Default: PostgreSQL
- Configure DB in `src/main/resources/application.yml`
- Flyway migration scripts in `src/main/resources/db/migration/`

## Testing
```bash
mvn test
```

## Contributing
- Follow standard Java and Spring Boot best practices
- Use feature branches and pull requests

## License
MIT
