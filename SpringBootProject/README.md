# Warehouse Employee Management System

## Overview

A production-ready, enterprise-grade warehouse employee management system built with Spring Boot 3.x, Java 17+, and PostgreSQL. This system provides comprehensive employee lifecycle management, time & attendance tracking, shift scheduling, leave management, certification tracking, safety incident reporting, and much more.

## Features

### Core Modules (20 Epics)

1. **Project Scaffolding & Domain Setup** - Spring Boot 3.x initialization with modular architecture
2. **Employee Master Data (CRUD)** - Complete employee management with RBAC
3. **Role-Based Access Control (RBAC)** - JWT/OAuth2 authentication with role-based permissions
4. **Time & Attendance** - Clock-in/out tracking with geofence validation
5. **Shift & Schedule Management** - Recurring shift templates and conflict detection
6. **Leave & Absence Management** - PTO, sick, and unpaid leave workflows
7. **Training & Certification Tracking** - Certification expiry alerts and renewal management
8. **Safety Incidents & OSHA Reporting** - Incident tracking and regulatory compliance
9. **Equipment & Asset Assignment** - Asset checkout/return with certification validation
10. **Performance Reviews & Goals** - Quarterly/annual review cycles
11. **Payroll Export Integration** - Automated payroll file generation
12. **Notifications & Announcements** - Multi-channel notifications (email, SMS, in-app)
13. **Integration Layer** - HRIS/WMS/IDP API integration
14. **Audit Trail & Compliance** - Immutable audit logging
15. **Reporting & Analytics** - Operational reports and KPI dashboards
16. **Mobile Access (PWA)** - Progressive Web App for mobile access
17. **Onboarding & Offboarding** - Automated employee lifecycle workflows
18. **Localization & Multi-Tenant** - Multi-region support with tenant isolation
19. **Observability & Monitoring** - Structured logging, tracing, and metrics
20. **CI/CD & Deployment Automation** - Automated build, test, and deployment pipelines

## Technology Stack

- **Framework:** Spring Boot 3.2.5
- **Language:** Java 17+
- **Build Tool:** Maven
- **Database:** PostgreSQL
- **Migration:** Flyway
- **Security:** Spring Security with JWT/OAuth2
- **API Documentation:** OpenAPI/Swagger (springdoc-openapi)
- **Monitoring:** Spring Boot Actuator, Prometheus
- **Containerization:** Docker
- **CI/CD:** GitHub Actions

## Prerequisites

- Java 17 or higher
- Maven 3.9+
- PostgreSQL 14+
- Docker (optional)

## Setup Instructions

### 1. Clone the Repository

```bash
git clone https://github.com/your-org/warehouse-employee-mgmt.git
cd warehouse-employee-mgmt
```

### 2. Configure Database

Create a PostgreSQL database:

```sql
CREATE DATABASE warehouse;
CREATE USER warehouse_user WITH PASSWORD 'warehouse_pass';
GRANT ALL PRIVILEGES ON DATABASE warehouse TO warehouse_user;
```

Update `src/main/resources/application.yml` with your database credentials.

### 3. Build the Application

```bash
mvn clean package
```

### 4. Run the Application

```bash
java -jar target/employee-mgmt-1.0.0.jar
```

Or using Maven:

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

### 5. Access API Documentation

Once the application is running, access the Swagger UI at:

```
http://localhost:8080/swagger-ui.html
```

### 6. Health Check

Verify the application is running:

```
http://localhost:8080/actuator/health
```

## Docker Deployment

### Build Docker Image

```bash
docker build -t warehouse-employee-mgmt .
```

### Run with Docker

```bash
docker run -p 8080:8080   -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/warehouse   -e SPRING_DATASOURCE_USERNAME=warehouse_user   -e SPRING_DATASOURCE_PASSWORD=warehouse_pass   warehouse-employee-mgmt
```

### Docker Compose (Recommended)

Create a `docker-compose.yml`:

```yaml
version: '3.8'
services:
  postgres:
    image: postgres:14
    environment:
      POSTGRES_DB: warehouse
      POSTGRES_USER: warehouse_user
      POSTGRES_PASSWORD: warehouse_pass
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/warehouse
      SPRING_DATASOURCE_USERNAME: warehouse_user
      SPRING_DATASOURCE_PASSWORD: warehouse_pass
    depends_on:
      - postgres

volumes:
  postgres_data:
```

Run with:

```bash
docker-compose up
```

## API Endpoints

### Employee Management

- `GET /api/v1/employees` - Get all employees (paginated)
- `GET /api/v1/employees/{id}` - Get employee by ID
- `GET /api/v1/employees/badge/{badgeId}` - Get employee by badge ID
- `POST /api/v1/employees` - Create new employee
- `PUT /api/v1/employees/{id}` - Update employee
- `DELETE /api/v1/employees/{id}` - Soft delete employee
- `GET /api/v1/employees/department/{department}` - Find by department
- `GET /api/v1/employees/role/{role}` - Find by role
- `GET /api/v1/employees/search?name={name}` - Search by name

### Attendance

- `POST /api/v1/attendance/clock-in` - Clock in
- `POST /api/v1/attendance/clock-out` - Clock out
- `GET /api/v1/attendance/daily-totals` - Get daily totals
- `POST /api/v1/attendance/corrections` - Submit correction request

### Additional endpoints available for all 20 modules (see Swagger UI for complete documentation)

## Security

### Authentication

The system uses JWT-based authentication. To access protected endpoints:

1. Obtain a JWT token from the authentication endpoint
2. Include the token in the `Authorization` header: `Bearer <token>`

### Roles

- **ADMIN** - Full system access
- **HR** - Employee management, leave approval, reporting
- **SUPERVISOR** - Team management, attendance, shift assignment
- **WORKER** - Self-service (clock-in/out, leave requests, view schedule)

## Database Migrations

Flyway migrations are located in `src/main/resources/db/migration/`

Migrations run automatically on application startup.

To manually run migrations:

```bash
mvn flyway:migrate
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

### Test Coverage

```bash
mvn jacoco:report
```

View coverage report at `target/site/jacoco/index.html`

## Monitoring

### Actuator Endpoints

- `/actuator/health` - Health check
- `/actuator/info` - Application info
- `/actuator/metrics` - Application metrics
- `/actuator/prometheus` - Prometheus metrics

### Logging

Structured JSON logging is enabled by default. Logs include:

- Timestamp
- Log level
- Logger name
- Message
- Trace ID (for distributed tracing)

## CI/CD Pipeline

The project includes a GitHub Actions workflow (`.github/workflows/ci.yml`) that:

1. Builds the application
2. Runs tests
3. Generates test coverage reports
4. Builds Docker image
5. Runs security scans
6. Deploys to staging (on develop branch)
7. Deploys to production (on main branch, with approval)

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

MIT License - see LICENSE file for details

## Support

For issues and questions:

- Create an issue in the GitHub repository
- Contact the development team at dev@warehouse-mgmt.com

## Roadmap

- [ ] Mobile native apps (iOS/Android)
- [ ] Advanced analytics and ML-based insights
- [ ] Integration with additional HRIS systems
- [ ] Real-time notifications via WebSocket
- [ ] Enhanced reporting with custom dashboards

## Acknowledgments

- Spring Boot team for the excellent framework
- PostgreSQL community
- All contributors to this project

---

**Version:** 1.0.0  
**Last Updated:** 2024  
**Maintained by:** Warehouse Management Team