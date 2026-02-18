# Warehouse Employee Management Platform (WEM)

A comprehensive Spring Boot platform for warehouse employee management, covering employee master data, scheduling, attendance, safety, RBAC, notifications, reporting, and more.

## Features
- Modular architecture: employee, scheduling, attendance, safety, config, audit, integration, notification, reporting, asset, leave, training, performance, payroll, portal, localization, mobile
- JPA entities, DTOs, MapStruct mappers, REST controllers, and service layers
- RBAC with OAuth2/API key support
- Flyway migrations, Actuator endpoints, OpenAPI docs

## Build & Run

### Prerequisites
- Java 17+
- Maven 3.8+
- PostgreSQL (or use H2 for dev/testing)

### Clone & Build
```bash
git clone <repo-url>
cd SpringBootProject
mvn clean install
```

### Run
```bash
mvn spring-boot:run
```

The app will start on [http://localhost:8080](http://localhost:8080)

### API Docs
- Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- OpenAPI JSON: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

### Health Check
- [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)

### Test
```bash
mvn test
```

## Configuration
- Edit `src/main/resources/application.yml` for DB, Flyway, and security settings.

## Modules
- `com.companyname.wem.employee` - Employee master data
- `com.companyname.wem.scheduling` - Shift & schedule management
- `com.companyname.wem.attendance` - Time & attendance
- `com.companyname.wem.safety` - Safety incidents
- ...and more as per technical design

## Contributing
Pull requests welcome! Please follow code style and add tests.

## License
MIT
