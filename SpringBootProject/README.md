# Warehouse Employee Management System (WEMS)

## Overview

WEMS is a production-ready, modular Spring Boot application for managing warehouse employees, attendance, scheduling, safety, assets, and more.

## Features

- Employee CRUD with unique badgeId and soft-delete
- Role-based access control (ADMIN, HR, SUPERVISOR, WORKER)
- JWT authentication (pluggable)
- Flyway database migrations
- OpenAPI documentation
- Actuator monitoring

## Build & Run

1. **Database**: Start a PostgreSQL instance (default: `wems`/`wems_user`/`wems_pass`)
2. **Build**: `mvn clean install`
3. **Run**: `mvn spring-boot:run`
4. **API Docs**: Visit [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
5. **Health**: [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)

## Project Structure

See `/src/main/java/com/company/wems/` for modules:
- config
- employee
- attendance
- scheduling
- leave
- training
- safety
- asset
- performance
- payroll
- notification
- integration
- audit
- reporting
- document
- security
- common

## Extending

Follow the provided patterns to add new entities, services, controllers, and migrations for each epic.

## License

MIT