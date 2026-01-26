# Warehouse Employee Management System

A complete, production-ready Spring Boot application for managing warehouse employees, attendance, scheduling, safety, assets, payroll, notifications, integrations, audit, reporting, mobile PWA, onboarding/offboarding, localization, multi-tenant, observability, and CI/CD.

## Features
- Modular package structure for all 20 epics
- Spring Boot 2.7+, Maven, PostgreSQL, JPA, Flyway, Actuator, Security (JWT/OAuth2, RBAC)
- RESTful APIs with Swagger/OpenAPI documentation
- Dockerfile and docker-compose for containerization
- CI/CD pipeline configuration

## Getting Started

### Prerequisites
- Java 11+
- Maven 3.6+
- Docker (optional)
- PostgreSQL

### Build & Run (Local)
1. Clone the repository
2. Configure your database credentials in `src/main/resources/application.yml`
3. Run Flyway migrations automatically on startup
4. Build and run:
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```
5. Access Swagger UI at [http://localhost:8080/swagger-ui/](http://localhost:8080/swagger-ui/)

### Docker
1. Build Docker image:
   ```bash
   docker build -t wms-app .
   ```
2. Run with Docker Compose:
   ```bash
   docker-compose up
   ```

### CI/CD
- See `.github/workflows/ci.yml` for pipeline configuration

## API Documentation
- Swagger/OpenAPI available at `/swagger-ui/`

## Database Migrations
- Flyway migration scripts in `src/main/resources/db/migration/`

## Security
- JWT/OAuth2 authentication
- RBAC for ADMIN, HR, SUPERVISOR, WORKER roles

## Monitoring
- Spring Boot Actuator endpoints at `/actuator`

## Multi-Tenant & Localization
- Multi-language and tenant isolation supported

## Contributing
Pull requests welcome. Please follow code style and add tests.

## License
MIT
