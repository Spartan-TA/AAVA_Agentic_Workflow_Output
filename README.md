# Warehouse Employee Management System (EMS)

## Overview
A Spring Boot application for managing warehouse employees, attendance, scheduling, leave, certifications, safety, assets, performance, payroll, notifications, integrations, audit, and reporting.

## Requirements
- Java 11+
- Maven 3.6+
- PostgreSQL

## Setup
1. Clone the repository:
   ```bash
   git clone <repo-url>
   cd <repo-dir>
   ```
2. Configure PostgreSQL:
   - Create database `warehouse_ems`
   - Create user `wms_user` with password `wms_password`
   - Grant all privileges to user
3. Update `application.yml` if needed.

## Build
```bash
mvn clean install
```

## Run
```bash
mvn spring-boot:run
```

## API Documentation
- Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- OpenAPI Docs: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

## Database Migration
- Flyway runs automatically on startup.

## Actuator Endpoints
- Health: `/actuator/health`
- Info: `/actuator/info`
- Metrics: `/actuator/metrics`
- Prometheus: `/actuator/prometheus`

## Security
- JWT authentication
- OAuth2 login
- Role-based access control

## License
MIT
