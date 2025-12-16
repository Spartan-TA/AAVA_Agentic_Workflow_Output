# Warehouse Employee Management System

A comprehensive Spring Boot 3.x application for managing warehouse employees, attendance, scheduling, safety, payroll, and more.

## Features
- Employee CRUD with RBAC and soft-delete
- Time & Attendance with geofencing
- Shift & Schedule management
- Leave, Certification, Safety, Asset, Performance modules
- Payroll export, notifications, integrations
- Audit trail, reporting, mobile PWA, localization
- Observability, Docker/K8s deployment, CI/CD

## Requirements
- Java 17+
- Maven 3.8+
- PostgreSQL 13+

## Build & Run

1. **Clone the repository:**
   ```bash
   git clone <repo-url>
   cd SpringBootProject
   ```
2. **Configure the database:**
   - Update `src/main/resources/application.yml` with your DB credentials.
3. **Build the project:**
   ```bash
   mvn clean install
   ```
4. **Run the application:**
   ```bash
   mvn spring-boot:run
   ```
   The app runs on [http://localhost:8080](http://localhost:8080)

## API Documentation
- Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- OpenAPI JSON: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

## Database Migrations
- Flyway auto-runs migrations from `src/main/resources/db/migration`.

## Testing
- Run all tests:
  ```bash
  mvn test
  ```
- Test containers spin up PostgreSQL for integration tests.

## Docker
- Build image:
  ```bash
  docker build -t warehouse-employee-mgmt .
  ```
- Run container:
  ```bash
  docker run -p 8080:8080 warehouse-employee-mgmt
  ```

## Kubernetes
- Deploy using `deployment.yaml`:
  ```bash
  kubectl apply -f deployment.yaml
  ```

## CI/CD
- GitHub Actions pipeline in `.github/workflows/ci-cd.yml` for build, test, and deploy.

## Security
- Spring Security with JWT/OAuth2 and API key toggle
- Method-level and row-level access control

## Observability
- Actuator endpoints: `/actuator/health`, `/actuator/metrics`, `/actuator/prometheus`
- Structured logging and OpenTelemetry tracing

## Contributing
- Please see [CONTRIBUTING.md](CONTRIBUTING.md) (if available)

## License
- MIT or as specified in repository
