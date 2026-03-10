# Warehouse Employee Management System

A production-ready Spring Boot application for managing warehouse employees, attendance, shifts, leave requests, certifications, safety incidents, assets, and performance reviews.

## Features

- Employee CRUD with soft delete
- Attendance clock-in/out and history
- Shift management
- Leave request workflow
- Certification tracking
- Safety incident reporting
- Asset assignment and tracking
- Performance reviews
- JWT-based security with role-based access
- OpenAPI (Swagger) documentation
- Flyway database migrations
- PostgreSQL support
- JUnit tests

## Getting Started

### Prerequisites

- Java 11+
- Maven 3.6+
- PostgreSQL

### Setup

1. Clone the repository.
2. Configure your PostgreSQL credentials in `src/main/resources/application.yml`.
3. Run database migrations:
   ```
   mvn flyway:migrate
   ```
4. Build and run the application:
   ```
   mvn spring-boot:run
   ```

### API Documentation

Visit [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) for interactive API docs.

### Running Tests

```
mvn test
```

## Project Structure

See the `/src/main/java/com/warehouse/ems/` directory for all source code.

## License

MIT