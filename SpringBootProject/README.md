# Warehouse Employee Management System (WMS)

A production-ready Spring Boot application for managing warehouse employees, including employee CRUD, RBAC security, time & attendance, scheduling, leave, training, safety, equipment, performance, payroll, notifications, integrations, audit, reporting, mobile PWA, onboarding/offboarding, localization, advanced scheduling, and self-service portal.

## Build & Run

1. **Database**: Ensure PostgreSQL is running and a database named `wmsdb` exists. Update credentials in `src/main/resources/application.yml` if needed.

2. **Build**:
   ```bash
   mvn clean install
   ```

3. **Run**:
   ```bash
   mvn spring-boot:run
   ```
   The app will start on [http://localhost:8080](http://localhost:8080)

4. **API Docs**: (If OpenAPI enabled) Visit `/swagger-ui.html`.

## Test

```bash
mvn test
```

## Directory Structure

- `src/main/java/com/wms/` - Java source code
- `src/main/resources/` - Configuration and migration scripts
- `src/main/resources/db/migration/` - Flyway SQL migrations

## Security

- RBAC with roles: ADMIN, HR, SUPERVISOR, WORKER
- HTTP Basic Auth (default), OAuth2 ready

## Features

- Employee CRUD: `/employees`
- More endpoints: See controllers in source code

## Contribution

- Fork, branch, and submit PRs.

## License

MIT
