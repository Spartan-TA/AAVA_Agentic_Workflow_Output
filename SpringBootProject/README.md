# Warehouse Employee Management System

This is a Spring Boot 3.x application for managing warehouse employees, attendance, shifts, leave requests, certifications, safety incidents, and assets.

## Features
- Employee CRUD
- Attendance tracking (clock-in/out, geofence validation)
- Shift scheduling
- Leave management
- Certification tracking
- Safety incident reporting
- Asset management
- JWT-based authentication & role-based access control
- RESTful APIs with OpenAPI documentation
- Flyway database migrations

## Requirements
- Java 17+
- Maven
- PostgreSQL

## Getting Started
1. Clone the repository
2. Configure `src/main/resources/application.yml`
3. Run database migrations
4. Start the application: `mvn spring-boot:run`

## Docker
See `Dockerfile` for containerization instructions.