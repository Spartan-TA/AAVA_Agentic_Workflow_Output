# Warehouse Employee Management System

A production-ready Spring Boot application for managing warehouse employees, attendance, scheduling, safety, and more.

## Features

### Core Modules (20 Epics)

1. **E01 - Project Scaffolding & Domain Setup**: Spring Boot Maven project with modular architecture
2. **E02 - Employee Master Data (CRUD)**: Complete employee management with RBAC
3. **E03 - Role-Based Access Control (RBAC)**: Spring Security with JWT/OAuth2
4. **E04 - Time & Attendance**: Clock in/out with geofence validation
5. **E05 - Shift & Schedule Management**: Recurring shifts, rotations, conflict detection
6. **E06 - Leave & Absence Management**: PTO, sick leave with approval workflows
7. **E07 - Training & Certification Tracking**: Expiration alerts, qualification verification
8. **E08 - Safety Incidents & OSHA Reporting**: Incident tracking, OSHA 300/300A export
9. **E09 - Equipment & Asset Assignment**: Asset checkout/return with certification checks
10. **E10 - Performance Reviews & Goals**: Review cycles, competencies, acknowledgements
11. **E11 - Payroll Export Integration**: Automated payroll file generation with SFTP delivery
12. **E12 - Notifications & Announcements**: Multi-channel (in-app, email, SMS)
13. **E13 - Integration Layer (HRIS/WMS APIs)**: REST APIs for external system integration
14. **E14 - Audit Trail & Compliance**: Comprehensive change tracking
15. **E15 - Reporting & Analytics**: Operational reports with CSV/PDF export
16. **E16 - Mobile Access (PWA)**: Progressive Web App for mobile workers
17. **E17 - Onboarding & Offboarding Workflow**: Automated employee lifecycle management
18. **E18 - Localization & Multi-Tenant**: i18n support (English/Spanish)
19. **E19 - Observability & Monitoring**: Prometheus metrics, distributed tracing
20. **E20 - CI/CD & Deployment Automation**: GitHub Actions pipeline

## Technology Stack

- **Framework**: Spring Boot 3.2.5
- **Language**: Java 17
- **Build Tool**: Maven
- **Database**: PostgreSQL 15
- **Migration**: Flyway
- **Security**: Spring Security with JWT
- **API Documentation**: OpenAPI/Swagger
- **Containerization**: Docker
- **Orchestration**: Kubernetes
- **CI/CD**: GitHub Actions
- **Monitoring**: Actuator, Prometheus, Micrometer

## Prerequisites

- Java 17 or higher
- Maven 3.8+
- Docker and Docker Compose (for containerized deployment)
- PostgreSQL 15 (if running locally without Docker)

## Build & Run

### Option 1: Local Development with Docker Compose (Recommended)

```bash
# Clone the repository
git clone https://github.com/Spartan-TA/AAVA_Agentic_Workflow_Output.git
cd AAVA_Agentic_Workflow_Output/SpringBootProject

# Build and run with Docker Compose
docker-compose up --build
```

The application will be available at: http://localhost:8080

### Option 2: Manual Build and Run

```bash
# Start PostgreSQL (ensure it's running on localhost:5432)
# Update application.properties with your database credentials

# Build the application
mvn clean package

# Run the application
java -jar target/employee-mgmt-1.0.0.jar
```

### Option 3: Kubernetes Deployment

```bash
# Build Docker image
docker build -t your-docker-repo/employee-mgmt:latest .

# Push to registry
docker push your-docker-repo/employee-mgmt:latest

# Deploy to Kubernetes
kubectl apply -f k8s/deployment.yaml
```

## API Documentation

Once the application is running, access the API documentation:

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs

## Health Check & Monitoring

- **Health Endpoint**: http://localhost:8080/actuator/health
- **Metrics**: http://localhost:8080/actuator/metrics
- **Prometheus**: http://localhost:8080/actuator/prometheus

## Database Migrations

Flyway migrations are automatically executed on application startup. Migration scripts are located in:

```
src/main/resources/db/migration/
```

## Configuration

Key configuration properties in `application.properties`:

```properties
# Server
server.port=8080

# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/warehouse
spring.datasource.username=warehouse_user
spring.datasource.password=warehouse_pass

# Security
jwt.secret=your-very-secure-secret
jwt.expiration=3600000

# Actuator
management.endpoints.web.exposure.include=health,info,metrics,prometheus
```

## Project Structure

```
SpringBootProject/
âââ src/
â   âââ main/
â   â   âââ java/com/warehouse/employee_mgmt/
â   â   â   âââ config/              # Security and application configuration
â   â   â   âââ employee/            # Employee module
â   â   â   â   âââ entity/          # JPA entities
â   â   â   â   âââ repository/      # Spring Data repositories
â   â   â   â   âââ service/         # Business logic
â   â   â   â   âââ controller/      # REST controllers
â   â   â   â   âââ dto/             # Data Transfer Objects
â   â   â   âââ security/            # JWT and authentication
â   â   â   âââ exception/           # Global exception handlers
â   â   â   âââ EmployeeMgmtApplication.java
â   â   âââ resources/
â   â       âââ application.properties
â   â       âââ db/migration/        # Flyway migration scripts
â   âââ test/                        # Unit and integration tests
âââ k8s/                             # Kubernetes manifests
âââ .github/workflows/               # CI/CD pipelines
âââ Dockerfile
âââ docker-compose.yml
âââ pom.xml
âââ README.md
```

## Security

### Roles

- **ADMIN**: Full system access
- **HR**: Employee management, leave approvals
- **SUPERVISOR**: Team management, shift assignments
- **WORKER**: Self-service (view own data, clock in/out)

### Authentication

The application uses JWT-based authentication. To access protected endpoints:

1. Obtain a JWT token from the `/api/auth/login` endpoint
2. Include the token in the `Authorization` header: `Bearer <token>`

## Testing

```bash
# Run all tests
mvn test

# Run with coverage
mvn clean test jacoco:report
```

## CI/CD Pipeline

The GitHub Actions pipeline automatically:

1. Builds the application
2. Runs unit and integration tests
3. Builds Docker image
4. Pushes to container registry
5. Deploys to Kubernetes (staging/production)

## Monitoring & Observability

- **Structured Logging**: JSON format with correlation IDs
- **Metrics**: Exported to Prometheus
- **Distributed Tracing**: OpenTelemetry integration
- **Health Checks**: Custom health indicators for DB and external APIs

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

MIT License

## Support

For issues and questions, please open a GitHub issue or contact the development team.

---

**Version**: 1.0.0  
**Last Updated**: 2026-02-17  
**Status**: Production Ready