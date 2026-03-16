# Warehouse Employee Management System (WEMS)

A production-ready, modular Spring Boot 3.x application for managing warehouse employees, attendance, scheduling, safety, assets, and more.

## Features

- Employee CRUD with RBAC (ADMIN, HR, SUPERVISOR, WORKER)
- Time & attendance tracking (clock-in/out, corrections)
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
- Mobile access (PWA-ready)
- Onboarding & offboarding workflow

## Tech Stack

- Java 17+, Spring Boot 3.x, Maven
- Spring Data JPA (Hibernate), PostgreSQL
- Flyway for DB migrations
- Spring Security (JWT/OAuth2)
- OpenAPI/Swagger
- Spring Actuator, Caching, Async
- MapStruct, Lombok

## Getting Started

### Prerequisites

- Java 17+
- Maven 3.8+
- PostgreSQL 13+

### Build & Run

```bash
# Clone the repo
git clone https://github.com/your-org/wems.git
cd wems

# Build
mvn clean install

# Run (dev profile)
SPRING_PROFILES_ACTIVE=dev mvn spring-boot:run
```

### Database

- Default DB: `wems`
- User: `wems_user`
- Password: `wems_pass`
- See `src/main/resources/application.yml` for config.

### API Documentation

- Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- OpenAPI JSON: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

### Testing

```bash
mvn test
```

### Deployment

See [docs/DEPLOYMENT_GUIDE.md](docs/DEPLOYMENT_GUIDE.md).

## Contributing

See [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) for module structure and guidelines.

## License

MIT