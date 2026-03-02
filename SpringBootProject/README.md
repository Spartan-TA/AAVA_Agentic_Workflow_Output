# Warehouse Employee Management System

A production-ready Spring Boot 3.x application for managing warehouse employees, attendance, scheduling, safety, and more.

## Features
- Employee master data CRUD with soft-delete, pagination, filtering
- Role-based access control (ADMIN, HR, SUPERVISOR, WORKER) with OAuth2/JWT
- Time & attendance with geofence validation
- Shift & schedule management with conflict detection
- Leave & absence management with accruals
- Training & certification tracking
- Safety incident reporting (OSHA)
- Equipment & asset assignment
- Performance reviews & goals
- Payroll export integration
- Multi-channel notifications
- Integration layer (HRIS/WMS APIs, SSO)
- Audit trail for compliance
- Reporting & analytics
- Mobile PWA support
- Multi-tenant, i18n
- Observability (Prometheus, structured logging)
- CI/CD ready (GitHub Actions)

## Tech Stack
- Java 17+, Spring Boot 3.x, Maven
- PostgreSQL, Spring Data JPA, Flyway
- Spring Security, OAuth2/JWT
- Lombok, MapStruct
- OpenAPI/Swagger
- Docker, Docker Compose
- JUnit 5, Mockito

## Getting Started

### Prerequisites
- Java 17+
- Maven 3.8+
- Docker (for local DB)

### Build & Run

1. **Clone the repo:**
   ```sh
   git clone <repo-url>
   cd SpringBootProject
   ```
2. **Start PostgreSQL:**
   ```sh
   docker-compose up -d
   ```
3. **Run migrations:**
   Flyway runs automatically on app startup.
4. **Build and run:**
   ```sh
   mvn clean package
   java -jar target/employee-management-1.0.0.jar
   ```
   or
   ```sh
   mvn spring-boot:run
   ```
5. **Access API docs:**
   - Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
   - Actuator: [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)

### Configuration
- See `src/main/resources/application.yml` for environment settings.
- Profiles: `dev`, `prod`.

### Running Tests
```sh
mvn test
```

### Docker
- Build image:
  ```sh
  docker build -t warehouse-employee-mgmt .
  ```
- Run:
  ```sh
  docker run --rm -p 8080:8080 --env SPRING_PROFILES_ACTIVE=prod warehouse-employee-mgmt
  ```

## Directory Structure
```
SpringBootProject/
âââ pom.xml
âââ README.md
âââ .gitignore
âââ Dockerfile
âââ docker-compose.yml
âââ src/main/java/com/warehouse/employee/
â   âââ WarehouseEmployeeApplication.java
â   âââ config/
â   âââ domain/
â   âââ repository/
â   âââ service/
â   âââ controller/
â   âââ dto/
â   âââ exception/
â   âââ security/
â   âââ integration/
â   âââ util/
âââ src/main/resources/
â   âââ application.yml
â   âââ application-dev.yml
â   âââ application-prod.yml
â   âââ db/migration/
â   âââ messages.properties
âââ src/test/java/
```

## License
MIT
