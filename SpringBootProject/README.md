# Warehouse Employee Management System (EMS)

A production-ready, multi-module Spring Boot application for warehouse employee management, including RBAC, attendance, scheduling, compliance, and integrations.

## Features
- Employee CRUD, soft-delete, and RBAC
- Shift, attendance, leave, and asset management
- Training, certification, and safety incident tracking
- Payroll export, notifications, and announcements
- Multi-tenant, localization, and audit trail
- OpenAPI docs, Flyway migrations, Actuator health
- JWT/OAuth2 security, CSRF, CORS, rate limiting

## Requirements
- Java 17+
- Maven 3.8+
- PostgreSQL 13+
- Docker (optional)

## Build & Run

```bash
# Clone repository
$ git clone <repo-url>
$ cd SpringBootProject

# Build
$ mvn clean install

# Run (local)
$ mvn spring-boot:run

# Or run with Docker
$ docker build -t warehouse-ems .
$ docker run -p 8080:8080 warehouse-ems
```

## Database Setup
- Configure PostgreSQL in `src/main/resources/application.yml`
- Flyway will auto-migrate schema on startup

## API Documentation
- Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

## Health Checks
- Actuator: [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)

## Testing
- Unit tests: `mvn test`
- Integration tests: `mvn verify`
- Test data setup examples in code comments

## Security
- JWT-based authentication
- RBAC for ADMIN, HR, SUPERVISOR, WORKER
- CSRF, CORS, rate limiting enabled

## Project Structure
```
com.wms.ems
âââ config
âââ controller
âââ dto
âââ entity
âââ exception
âââ repository
âââ security
âââ service
âââ util
âââ integration
```

## Contributing
- Follow Java/Spring best practices
- Add/modify tests for new features
- See code comments for test scenarios

## License
MIT
