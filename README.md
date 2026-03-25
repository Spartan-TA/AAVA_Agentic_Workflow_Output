# Warehouse Employee Management System (EMS)

## Build

```
mvn clean install
```

## Run

```
mvn spring-boot:run
```

## Test

```
mvn test
```

## Endpoints

- Application runs on: `http://localhost:8080`
- OpenAPI/Swagger UI: `http://localhost:8080/swagger-ui.html`
- Actuator: `http://localhost:8080/actuator`

## Database

- Default: PostgreSQL (see `src/main/resources/application.yml` for config)
- Flyway migrations auto-run on startup
