# Warehouse Employee Management System

## Overview

Production-ready Spring Boot 3.x application for managing warehouse employees, attendance, scheduling, safety, assets, and more.

## Features

- Employee CRUD
- Role-based access control
- Attendance & scheduling
- Leave management
- Training & certifications
- Safety & OSHA reporting
- Asset assignment
- Performance reviews
- Payroll export
- Notifications
- HRIS/WMS integrations
- Audit trail
- Reporting & analytics
- Mobile PWA
- Onboarding/offboarding
- Localization & multi-site
- Scheduling optimization
- Self-service portal & chatbot

## Build & Run

### Prerequisites

- Java 17+
- Maven 3.8+
- PostgreSQL (default config: localhost:5432/wmsdb, user: wmsuser, password: wmspassword)

### Steps

```bash
mvn clean install
mvn spring-boot:run
```

App runs on [http://localhost:8080](http://localhost:8080)

### Database Migration

Flyway runs automatically on startup. Migration scripts are in `src/main/resources/db/migration`.

### API Documentation

Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

### Health Check

Actuator: [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)