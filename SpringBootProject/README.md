# Warehouse Employee Management System

A production-ready, modular Spring Boot 3.x application for managing warehouse employees, attendance, scheduling, safety, and more.

## Modules

- **warehouse-core**: Domain entities, repositories, and core services
- **warehouse-api**: REST API controllers and DTOs
- **warehouse-security**: JWT-based security and RBAC
- **warehouse-integration**: HRIS/WMS integration, webhooks, SSO
- **warehouse-web**: PWA frontend (Thymeleaf)

## Build & Run

```bash
mvn clean install
cd warehouse-web
mvn spring-boot:run
```

The app runs on [http://localhost:8080](http://localhost:8080).

## Database

- Uses Flyway for migrations (see `warehouse-core/src/main/resources/db/migration`)
- Default: H2 in-memory (override in `application.yml`)

## API Docs

- OpenAPI UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

## Security

- JWT authentication (see `warehouse-security`)
- Roles: ADMIN, HR, SUPERVISOR, WORKER

## Testing

```bash
mvn test
```

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines.

---

Â© 2024 Company Name. All rights reserved.