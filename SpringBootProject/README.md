# Warehouse Employee Management Platform

This is a comprehensive Spring Boot application for managing warehouse employees, attendance, scheduling, safety, assets, payroll, notifications, integrations, audit, reporting, mobile access, onboarding/offboarding, multi-tenancy, and observability.

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
- Onboarding & offboarding workflow
- Localization & multi-tenant support
- Observability & monitoring
- CI/CD pipeline

## Tech Stack
- Java 17
- Spring Boot 3.2+
- Maven
- PostgreSQL
- Flyway
- Spring Security (OAuth2/JWT)
- Spring Data JPA
- Spring Boot Actuator
- Prometheus
- OpenAPI/Swagger

## Getting Started

### Prerequisites
- Java 17+
- Maven 3.8+
- PostgreSQL

### Build & Run
1. Clone the repository:
   ```bash
   git clone https://github.com/Spartan-TA/AAVA_Agentic_Workflow_Output.git
   cd AAVA_Agentic_Workflow_Output/SpringBootProject
   ```
2. Update `src/main/resources/application.yml` with your PostgreSQL credentials.
3. Run Flyway migrations:
   ```bash
   mvn flyway:migrate
   ```
4. Build and start the application:
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```
5. Access Actuator health endpoint:
   - [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)

### API Documentation
- Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

### Testing
- Run unit and integration tests:
   ```bash
   mvn test
   ```

## Project Structure
```
SpringBootProject/
âââ pom.xml
âââ README.md
âââ src/main/java/com/warehousemgmt/
â   âââ WarehouseEmployeeMgmtApplication.java
â   âââ employee/
â   âââ scheduling/
â   âââ attendance/
â   âââ safety/
â   âââ certification/
â   âââ asset/
â   âââ performance/
â   âââ payroll/
â   âââ notification/
â   âââ integration/
â   âââ audit/
â   âââ reporting/
â   âââ mobile/
â   âââ lifecycle/
â   âââ tenant/
â   âââ security/
â   âââ config/
âââ src/main/resources/
â   âââ application.yml
â   âââ db/migration/
```

## Contributing
Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

## License
MIT
