# Warehouse Employee Management System

This is a Spring Boot application for managing warehouse employees, attendance, shifts, and safety incidents.

## Build & Run

1. **Build the project:**
   ```bash
   mvn clean install
   ```
2. **Run the application:**
   ```bash
   mvn spring-boot:run
   ```
3. **Access the API:**
   - The application runs on [http://localhost:8080](http://localhost:8080)

## Features
- Employee CRUD management
- Attendance clock in/out
- Shift scheduling
- Safety incident reporting
- OAuth2/JWT security
- PostgreSQL database
- Flyway migrations
- Actuator endpoints

## Docker
To build and run with Docker:
```bash
docker build -t warehouse-employee-mgmt .
docker run -p 8080:8080 warehouse-employee-mgmt
```

## CI/CD
See `.github/workflows/ci.yml` for GitHub Actions pipeline.
