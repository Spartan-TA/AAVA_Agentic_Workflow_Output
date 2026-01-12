# Warehouse Employee Management System (EMS)

## Overview

A comprehensive, production-ready Spring Boot 3.2.0 application for managing warehouse employee operations, covering 20 user stories from employee master data to deployment automation.

## Features

### Core Modules (E01-E20)

1. **E01: Project Scaffolding & Domain Setup**
   - Maven-based Spring Boot 3.2.0 project
   - Modular package structure
   - Flyway database migrations
   - Spring Boot Actuator for monitoring

2. **E02: Employee Master Data (CRUD)**
   - Complete CRUD operations for employees
   - Unique badge ID enforcement
   - Soft delete support
   - Pagination and filtering
   - OpenAPI documentation

3. **E03: Role-Based Access Control (RBAC)**
   - Spring Security integration
   - Four roles: ADMIN, HR, SUPERVISOR, WORKER
   - Method-level security
   - Row-level data filtering
   - API Key/OAuth2 toggle

4. **E04: Time & Attendance**
   - Clock in/out endpoints
   - Geofence validation
   - Device tracking
   - Shift association
   - Missed punch corrections
   - CSV reporting

5. **E05: Shift & Schedule Management**
   - Shift templates and rotations
   - Overtime rules
   - Employee assignments
   - Blackout dates
   - Operation calendars
   - Conflict detection

6. **E06: Leave & Absence Management**
   - PTO, sick, and unpaid leave requests
   - Approval workflow
   - Accrual balances
   - Leave policies
   - Integration with scheduling

7. **E07: Training & Certification Tracking**
   - Certification management
   - Expiration tracking
   - Renewal workflows
   - Assignment blocking for expired certs
   - Document uploads

8. **E08: Safety Incidents & OSHA Reporting**
   - Incident recording
   - Investigation workflow
   - Corrective actions
   - OSHA 300/300A reports
   - Safety metrics dashboard

9. **E09: Equipment & Asset Assignment**
   - Asset registry
   - Check-in/out tracking
   - Certification validation
   - Asset condition management
   - History logging

10. **E10: Performance Reviews & Goals**
    - Review cycles
    - Goal tracking
    - Competency ratings
    - Supervisor/employee acknowledgements
    - PDF export

11. **E11: Payroll Export Integration**
    - Payroll file generation
    - Provider format mapping
    - SFTP/API delivery
    - Retry logic
    - Audit logging

12. **E12: Notifications & Announcements**
    - In-app notifications
    - Email/SMS delivery
    - Opt-in/out preferences
    - Localized templates
    - Rate limiting
    - Dashboard announcements

13. **E13: Integration Layer (HRIS/WMS APIs)**
    - REST APIs for HRIS sync
    - WMS integration
    - IDP/SSO support
    - Webhooks
    - JWT/OAuth2 security
    - Idempotency

14. **E14: Audit Trail & Compliance**
    - Centralized audit logging
    - Immutable records
    - Actor tracking
    - Before/after snapshots
    - Export capabilities

15. **E15: Reporting & Analytics**
    - Attendance reports
    - Overtime analysis
    - Leave balance reports
    - Certification status
    - Safety KPIs
    - CSV/PDF export

16. **E16: Mobile Access (PWA)**
    - Responsive mobile views
    - Installable PWA
    - Offline queue
    - Conflict resolution
    - Core flows optimized

17. **E17: Onboarding & Offboarding**
    - Automated provisioning
    - Task generation
    - Asset assignment
    - Access revocation
    - Schedule updates

18. **E18: Localization & Multi-Tenant**
    - Multiple warehouse support
    - Data isolation
    - UI localization (en, es)
    - Timezone-aware scheduling
    - Tenant filtering

19. **E19: Observability & Monitoring**
    - Structured JSON logging
    - Micrometer/Prometheus metrics
    - Distributed tracing (Zipkin/Jaeger)
    - Health checks
    - Grafana dashboards

20. **E20: Deployment & CI/CD**
    - Dockerized application
    - Kubernetes manifests
    - CI/CD pipeline
    - Blue-green deployment
    - Automated rollback

## Technology Stack

- **Framework**: Spring Boot 3.2.0
- **Language**: Java 17
- **Build Tool**: Maven
- **Database**: PostgreSQL
- **Migration**: Flyway
- **Security**: Spring Security (OAuth2/JWT)
- **API Documentation**: SpringDoc OpenAPI 3
- **Monitoring**: Spring Boot Actuator, Micrometer, Prometheus
- **Tracing**: Spring Cloud Sleuth, Zipkin
- **Containerization**: Docker
- **Orchestration**: Kubernetes
- **CI/CD**: GitHub Actions

## Project Structure

```
SpringBootProject/
âââ pom.xml
âââ Dockerfile
âââ README.md
âââ .gitignore
âââ k8s/
â   âââ deployment.yaml
â   âââ service.yaml
â   âââ ingress.yaml
â   âââ configmap.yaml
âââ .github/
â   âââ workflows/
â       âââ ci-cd-pipeline.yml
âââ src/
    âââ main/
    â   âââ java/
    â   â   âââ com/
    â   â       âââ warehouse/
    â   â           âââ ems/
    â   â               âââ WarehouseEmsApplication.java
    â   â               âââ config/
    â   â               âââ security/
    â   â               âââ common/
    â   â               âââ employee/
    â   â               âââ attendance/
    â   â               âââ schedule/
    â   â               âââ leave/
    â   â               âââ training/
    â   â               âââ safety/
    â   â               âââ asset/
    â   â               âââ performance/
    â   â               âââ payroll/
    â   â               âââ notification/
    â   â               âââ integration/
    â   â               âââ audit/
    â   â               âââ reporting/
    â   â               âââ mobile/
    â   â               âââ lifecycle/
    â   â               âââ tenant/
    â   âââ resources/
    â       âââ application.yml
    â       âââ db/
    â       â   âââ migration/
    â       âââ static/
    â       â   âââ manifest.json
    â       âââ templates/
    â       âââ messages.properties
    â       âââ logback-spring.xml
    âââ test/
        âââ java/
            âââ com/
                âââ warehouse/
                    âââ ems/
```

## Prerequisites

- Java 17 or higher
- Maven 3.8+
- PostgreSQL 14+
- Docker (optional, for containerization)
- Kubernetes cluster (optional, for deployment)

## Configuration

### Database Setup

1. Create PostgreSQL database:
```sql
CREATE DATABASE warehouse_ems;
CREATE USER warehouse WITH PASSWORD 'ems123';
GRANT ALL PRIVILEGES ON DATABASE warehouse_ems TO warehouse;
```

2. Update `application.yml` with your database credentials:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/warehouse_ems
    username: warehouse
    password: ems123
```

### Environment Variables

- `DB_USERNAME`: Database username (default: warehouse)
- `DB_PASSWORD`: Database password (default: ems123)
- `JWT_ISSUER_URI`: JWT issuer URI for OAuth2
- `MAIL_PASSWORD`: Email server password

## Build & Run

### Local Development

```bash
# Clone the repository
git clone https://github.com/your-org/warehouse-ems.git
cd warehouse-ems

# Build the project
mvn clean package

# Run the application
java -jar target/ems-1.0.0.jar

# Or use Maven Spring Boot plugin
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

### Docker

```bash
# Build Docker image
docker build -t warehouse-ems:1.0.0 .

# Run container
docker run -p 8080:8080   -e DB_USERNAME=warehouse   -e DB_PASSWORD=ems123   warehouse-ems:1.0.0
```

### Kubernetes

```bash
# Apply Kubernetes manifests
kubectl apply -f k8s/

# Check deployment status
kubectl get pods -l app=warehouse-ems

# Access the service
kubectl port-forward svc/warehouse-ems 8080:8080
```

## API Documentation

Once the application is running, access the API documentation:

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/api-docs

## Health Checks

- **Health**: http://localhost:8080/actuator/health
- **Info**: http://localhost:8080/actuator/info
- **Metrics**: http://localhost:8080/actuator/metrics
- **Prometheus**: http://localhost:8080/actuator/prometheus

## Testing

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=EmployeeControllerTest

# Run with coverage
mvn clean test jacoco:report
```

## Database Migrations

Flyway migrations are automatically applied on startup. Migration files are located in `src/main/resources/db/migration/`.

To manually run migrations:
```bash
mvn flyway:migrate
```

## Security

### Default Roles

- **ADMIN**: Full system access
- **HR**: Employee and leave management
- **SUPERVISOR**: Team management
- **WORKER**: Self-service operations

### Authentication

The system supports two authentication modes (configurable in `application.yml`):

1. **API Key**: Set `warehouse.security.api-key-enabled=true`
2. **OAuth2/JWT**: Set `warehouse.security.oauth2-enabled=true`

## Monitoring & Observability

### Logging

- Structured JSON logs with trace IDs
- Log level configuration in `logback-spring.xml`
- Logs include: timestamp, level, logger, message, MDC context

### Metrics

- Exposed via `/actuator/prometheus`
- Includes: HTTP requests, JVM metrics, database connections, custom business metrics

### Tracing

- Distributed tracing with Zipkin/Jaeger
- Trace IDs propagated across services
- Configure Zipkin URL in `application.yml`

## CI/CD Pipeline

The project includes a GitHub Actions workflow (`.github/workflows/ci-cd-pipeline.yml`) that:

1. Builds the application
2. Runs tests
3. Scans for vulnerabilities
4. Builds Docker image
5. Deploys to Kubernetes
6. Runs smoke tests
7. Rolls back on failure

## Multi-Tenant Support

The system supports multiple warehouses (tenants) with:

- Data isolation via `tenant_id` column
- Tenant-specific configurations
- Locale and timezone settings per tenant

## Localization

Supported locales:
- English (en_US)
- Spanish (es_ES)

Translation files: `messages.properties`, `messages_es.properties`

## PWA Support

The application includes Progressive Web App capabilities:

- Installable on mobile devices
- Offline support for core operations
- Service worker for caching
- Manifest file: `static/manifest.json`

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Support

For issues and questions:
- GitHub Issues: https://github.com/your-org/warehouse-ems/issues
- Email: support@warehouse-ems.com

## Authors

- Warehouse EMS Team

## Acknowledgments

- Spring Boot team for the excellent framework
- All contributors and maintainers

---

**Version**: 1.0.0  
**Last Updated**: 2024  
**Status**: Production Ready