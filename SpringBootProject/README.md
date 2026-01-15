# Warehouse Employee Management System

## Overview

A production-ready Spring Boot application for managing warehouse employees, attendance, scheduling, safety, assets, performance, payroll, notifications, audit, and more.

## Features

- **Employee Management**: CRUD operations with badgeId uniqueness, soft-delete, pagination, filtering
- **Security**: JWT/OAuth2 authentication with RBAC (ADMIN, HR, SUPERVISOR, WORKER)
- **Attendance Tracking**: Clock-in/out with geofence validation, corrections workflow
- **Shift Scheduling**: Recurring templates, conflict detection, audit trail
- **Leave Management**: Request/approval workflow, accrual balances
- **Certification Tracking**: Expiry alerts, document upload, assignment blocking
- **Safety Incident Reporting**: OSHA metrics and investigation workflow
- **Asset Management**: Checkout/return tracking, certification checks
- **Performance Reviews**: Goals, ratings, acknowledgements
- **Payroll Export**: SFTP/API delivery with reconciliation
- **Notifications**: In-app, email, SMS with rate limits
- **Integration APIs**: HRIS/WMS webhooks, SSO
- **Audit Trail**: Immutable logging for compliance
- **Reporting & Analytics**: CSV/PDF export, dashboards
- **PWA Mobile Access**: Offline support, responsive design
- **Onboarding/Offboarding**: Automated provisioning
- **Multi-Warehouse Support**: Localization and time zones
- **AI-Driven Scheduling**: Demand forecasting
- **Feedback System**: Voting and A/B testing

## Technology Stack

- **Framework**: Spring Boot 3.2.0
- **Language**: Java 17+
- **Build Tool**: Maven
- **Database**: PostgreSQL 15
- **Migration**: Flyway
- **Security**: Spring Security with JWT/OAuth2
- **API Documentation**: OpenAPI 3.0 (Springdoc)
- **Monitoring**: Spring Boot Actuator
- **Containerization**: Docker

## Prerequisites

- Java 17 or higher
- Maven 3.8+
- Docker and Docker Compose
- PostgreSQL 15 (if running locally without Docker)

## Build & Run

### Using Docker Compose (Recommended)

```bash
# Clone the repository
git clone https://github.com/Spartan-TA/AAVA_Agentic_Workflow_Output.git
cd AAVA_Agentic_Workflow_Output/SpringBootProject

# Build and run with Docker Compose
docker-compose up --build
```

The application will be available at `http://localhost:8080`

### Local Development

```bash
# Build the project
mvn clean package

# Run the application
java -jar target/ems-1.0.0.jar
```

**Note**: Ensure PostgreSQL is running and update `application.yml` with your database credentials.

## API Documentation

Once the application is running, access the interactive API documentation:

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs

## Health Check

Spring Boot Actuator provides health monitoring:

- **Health Endpoint**: http://localhost:8080/actuator/health
- **Info Endpoint**: http://localhost:8080/actuator/info

## Database Configuration

### Default Credentials

- **Database**: `warehouse_ems`
- **Username**: `warehouse_user`
- **Password**: `warehouse_pass`

### Flyway Migrations

Database schema is managed using Flyway. Migrations are located in:

```
src/main/resources/db/migration/
```

Migrations run automatically on application startup.

## Security

### Authentication

The application supports:

- **JWT**: Token-based authentication
- **OAuth2**: Resource server configuration
- **API Key**: Optional for integrations

### Roles

- **ADMIN**: Full system access
- **HR**: Employee and leave management
- **SUPERVISOR**: Team-level access
- **WORKER**: Self-service access

### Endpoints Security

All `/api/employees/**` endpoints require authentication. Role-based access is enforced using `@PreAuthorize` annotations.

## Project Structure

```
SpringBootProject/
âââ src/
â   âââ main/
â   â   âââ java/com/warehouse/ems/
â   â   â   âââ WarehouseEmsApplication.java
â   â   â   âââ employee/
â   â   â   â   âââ entity/
â   â   â   â   âââ dto/
â   â   â   â   âââ repository/
â   â   â   â   âââ service/
â   â   â   â   âââ controller/
â   â   â   â   âââ mapper/
â   â   â   âââ attendance/
â   â   â   âââ scheduling/
â   â   â   âââ leave/
â   â   â   âââ certification/
â   â   â   âââ safety/
â   â   â   âââ asset/
â   â   â   âââ performance/
â   â   â   âââ payroll/
â   â   â   âââ notification/
â   â   â   âââ integration/
â   â   â   âââ audit/
â   â   â   âââ reporting/
â   â   â   âââ security/
â   â   â   âââ exception/
â   â   â   âââ config/
â   â   âââ resources/
â   â       âââ application.yml
â   â       âââ db/migration/
â   âââ test/
âââ pom.xml
âââ Dockerfile
âââ docker-compose.yml
âââ README.md
```

## Testing

### Run Unit Tests

```bash
mvn test
```

### Run Integration Tests

```bash
mvn verify
```

## Deployment

### Docker

```bash
# Build Docker image
docker build -t warehouse-ems:latest .

# Run container
docker run -p 8080:8080   -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/warehouse_ems   -e SPRING_DATASOURCE_USERNAME=warehouse_user   -e SPRING_DATASOURCE_PASSWORD=warehouse_pass   warehouse-ems:latest
```

### Kubernetes

Kubernetes manifests can be generated using tools like Helm or Kustomize. Ensure ConfigMaps and Secrets are configured for database credentials and JWT secrets.

## Environment Variables

Key environment variables:

- `SPRING_DATASOURCE_URL`: Database connection URL
- `SPRING_DATASOURCE_USERNAME`: Database username
- `SPRING_DATASOURCE_PASSWORD`: Database password
- `JWT_SECRET`: Secret key for JWT signing
- `JWT_EXPIRATION`: Token expiration time (milliseconds)

## Monitoring & Logging

- **Logging**: SLF4J with Logback
- **Metrics**: Spring Boot Actuator
- **Health Checks**: `/actuator/health`

## Contributing

Contributions are welcome! Please follow these guidelines:

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

MIT License

## Support

For issues and questions, please open an issue on GitHub.

---

**Warehouse EMS** - A comprehensive employee management system for warehouse operations.