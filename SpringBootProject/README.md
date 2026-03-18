# Warehouse Employee Management System

## Overview

Production-ready Spring Boot application for managing warehouse employees, attendance, scheduling, payroll, and more.

## Features

- Modular monolith architecture
- Employee CRUD, attendance tracking, shift scheduling
- JWT-based security & RBAC
- PostgreSQL database
- Flyway migrations
- OpenAPI/Swagger documentation
- Caching, auditing, exception handling

## Setup

### Prerequisites

- Java 17+
- Maven 3.8+
- PostgreSQL

### Build & Run

```bash
# Build
mvn clean install

# Run
mvn spring-boot:run
```

### Database

- Default DB: `warehouse_employee_management`
- User: `warehouse_admin`
- Password: `warehouse_password`

### Docker

```bash
docker-compose up --build
```

### API Documentation

- Swagger UI: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

## Modules

- Employee
- Attendance
- Scheduling
- Security & RBAC
- ...and more

## License

MIT