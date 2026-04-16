# Warehouse Employee Management System

A production-ready, modular Spring Boot 3.x application for managing warehouse employees, attendance, scheduling, safety, assets, payroll, and more.

## Features

- Employee CRUD with RBAC
- JWT/OAuth2 Security
- PostgreSQL with Flyway migrations
- OpenAPI 3 documentation
- Prometheus metrics
- Modular architecture (employee, attendance, scheduling, etc.)

## Requirements

- Java 17+
- Maven 3.8+
- PostgreSQL 13+

## Getting Started

1. **Clone the repo**

   ```
   git clone <repo-url>
   cd warehouse-employee-management
   ```

2. **Configure the database**

   Update `src/main/resources/application.yml` with your PostgreSQL credentials.

3. **Build and run**

   ```
   mvn clean package
   java -jar target/warehouse-employee-management-1.0.0.jar
   ```

4. **API Docs**

   Visit [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

5. **Actuator Health**

   Visit [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)

## Docker

Build and run with Docker:

```
docker build -t warehouse-employee-management .
docker run -p 8080:8080 warehouse-employee-management
```

## Modules

- Employee Management
- Scheduling & Attendance
- Leave & Training
- Safety & Assets
- Payroll & Notifications
- Integration & Audit
- Reporting & Mobile

## License

MIT