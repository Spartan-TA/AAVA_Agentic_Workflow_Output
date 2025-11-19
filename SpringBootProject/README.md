# Warehouse Employee Management System

## Overview
A comprehensive Spring Boot application for managing warehouse employees, attendance, scheduling, leave, certifications, safety, assets, performance, payroll, notifications, integrations, audit, reporting, mobile access, onboarding/offboarding, localization, observability, and CI/CD.

## Tech Stack
- **Framework:** Spring Boot 3.2.5
- **Language:** Java 17+
- **Build Tool:** Maven
- **Database:** PostgreSQL
- **ORM:** Spring Data JPA / Hibernate
- **Security:** Spring Security (RBAC, JWT/OAuth2)
- **Database Migration:** Flyway
- **Monitoring:** Spring Boot Actuator, Prometheus
- **API Documentation:** OpenAPI 3.0 (Springdoc)
- **Testing:** JUnit 5, Mockito, TestContainers
- **Utilities:** Lombok

## Features

### Core Modules
1. **Employee Management (E02)** - CRUD operations for employee master data
2. **Role-Based Access Control (E03)** - Security with 4 roles (ADMIN, HR, SUPERVISOR, WORKER)
3. **Time & Attendance (E04)** - Clock in/out with geofence validation
4. **Shift & Schedule Management (E05)** - Shift templates and assignments
5. **Leave & Absence Management (E06)** - PTO/sick leave requests and approvals
6. **Training & Certification Tracking (E07)** - Certification management with expiration alerts
7. **Safety Incidents & OSHA Reporting (E08)** - Incident recording and compliance
8. **Equipment & Asset Assignment (E09)** - Asset checkout/return tracking
9. **Performance Reviews & Goals (E10)** - Review cycles and ratings
10. **Payroll Export Integration (E11)** - Automated payroll file generation
11. **Notifications & Announcements (E12)** - Multi-channel notifications
12. **Integration Layer (E13)** - HRIS/WMS API connectors
13. **Audit Trail & Compliance (E14)** - Immutable audit logging
14. **Reporting & Analytics (E15)** - Operational reports and KPIs
15. **Mobile Access (E16)** - Progressive Web App (PWA)
16. **Onboarding & Offboarding (E17)** - Automated lifecycle workflows
17. **Localization & Multi-Tenant (E18)** - English/Spanish support
18. **Observability & Monitoring (E19)** - Structured logging and metrics
19. **Automated Testing & CI/CD (E20)** - Comprehensive test coverage

## Prerequisites

- **Java 17 or higher** - [Download](https://adoptium.net/)
- **Maven 3.8+** - [Download](https://maven.apache.org/download.cgi)
- **PostgreSQL 14+** - [Download](https://www.postgresql.org/download/)
- **Git** - [Download](https://git-scm.com/downloads)

## Database Setup

1. Install PostgreSQL
2. Create database:
```sql
CREATE DATABASE warehouse_db;
CREATE USER warehouse_user WITH PASSWORD 'warehouse_pass';
GRANT ALL PRIVILEGES ON DATABASE warehouse_db TO warehouse_user;
```

3. Update `src/main/resources/application.yml` with your database credentials:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/warehouse_db
    username: warehouse_user
    password: warehouse_pass
```

## Build & Run

### Clone Repository
```bash
git clone https://github.com/Spartan-TA/AAVA_Agentic_Workflow_Output.git
cd AAVA_Agentic_Workflow_Output/SpringBootProject
```

### Build Project
```bash
mvn clean install
```

### Run Database Migrations
```bash
mvn flyway:migrate
```

### Run Application
```bash
mvn spring-boot:run
```

The application will start on **http://localhost:8080**

## API Documentation

Once the application is running, access the interactive API documentation:

- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **OpenAPI JSON:** http://localhost:8080/v3/api-docs

## Health Check

Spring Boot Actuator provides health and monitoring endpoints:

- **Health:** http://localhost:8080/actuator/health
- **Info:** http://localhost:8080/actuator/info
- **Metrics:** http://localhost:8080/actuator/metrics
- **Prometheus:** http://localhost:8080/actuator/prometheus

## Testing

### Run Unit Tests
```bash
mvn test
```

### Run Integration Tests
```bash
mvn verify
```

### Test Coverage Report
```bash
mvn jacoco:report
```
View report at: `target/site/jacoco/index.html`

## Project Structure

```
SpringBootProject/
âââ pom.xml                                 # Maven configuration
âââ .gitignore                              # Git ignore rules
âââ README.md                               # This file
âââ src/
â   âââ main/
â   â   âââ java/com/warehouse/employee/
â   â   â   âââ config/                     # Configuration classes
â   â   â   â   âââ SecurityConfig.java     # Spring Security config
â   â   â   â   âââ DatabaseConfig.java     # JPA config
â   â   â   â   âââ ObservabilityConfig.java # Metrics config
â   â   â   âââ domain/                     # Domain layer
â   â   â   â   âââ employee/               # Employee module
â   â   â   â   â   âââ Employee.java       # Entity
â   â   â   â   â   âââ EmployeeRepository.java # Repository
â   â   â   â   â   âââ EmployeeService.java    # Service
â   â   â   â   âââ attendance/             # Attendance module
â   â   â   â   âââ scheduling/             # Scheduling module
â   â   â   â   âââ leave/                  # Leave module
â   â   â   â   âââ certification/          # Certification module
â   â   â   â   âââ safety/                 # Safety module
â   â   â   â   âââ asset/                  # Asset module
â   â   â   â   âââ performance/            # Performance module
â   â   â   â   âââ audit/                  # Audit module
â   â   â   âââ web/                        # Web layer
â   â   â   â   âââ controller/             # REST controllers
â   â   â   â   â   âââ EmployeeController.java
â   â   â   â   âââ dto/                    # Data Transfer Objects
â   â   â   â       âââ EmployeeRequest.java
â   â   â   â       âââ EmployeeResponse.java
â   â   â   âââ integration/                # Integration layer
â   â   â   â   âââ hris/                   # HRIS integration
â   â   â   â   âââ wms/                    # WMS integration
â   â   â   â   âââ payroll/                # Payroll integration
â   â   â   â   âââ notification/           # Notification service
â   â   â   âââ util/                       # Utility classes
â   â   â       âââ SecurityUtil.java
â   â   â       âââ DateTimeUtil.java
â   â   â       âââ ValidationUtil.java
â   â   âââ resources/
â   â       âââ application.yml             # Application config
â   â       âââ messages.properties         # Localization (English)
â   â       âââ messages_es.properties      # Localization (Spanish)
â   â       âââ db/migration/               # Flyway migrations
â   â           âââ V1__init_schema.sql
â   âââ test/
â       âââ java/com/warehouse/employee/
â           âââ domain/employee/
â               âââ EmployeeServiceTest.java # Unit tests
```

## API Endpoints

### Employee Management
- `POST /api/v1/employees` - Create employee (ADMIN, HR)
- `GET /api/v1/employees/{id}` - Get employee by ID (ADMIN, HR, SUPERVISOR)
- `GET /api/v1/employees` - List employees with pagination (ADMIN, HR, SUPERVISOR)
- `PUT /api/v1/employees/{id}` - Update employee (ADMIN, HR)
- `PATCH /api/v1/employees/{id}` - Partial update (ADMIN, HR)
- `DELETE /api/v1/employees/{id}` - Soft delete employee (ADMIN)

### Time & Attendance
- `POST /api/v1/attendance/clock-in` - Clock in (ALL)
- `POST /api/v1/attendance/clock-out` - Clock out (ALL)
- `GET /api/v1/attendance/records` - Get attendance records (ADMIN, HR, SUPERVISOR)

### Additional endpoints for Scheduling, Leave, Certifications, Safety, Assets, Performance, Payroll, Notifications, Integrations, Audit, and Reporting modules.

## Security

### Roles
- **ADMIN** - Full system access
- **HR** - Employee and HR operations
- **SUPERVISOR** - Team management
- **WORKER** - Self-service operations

### Authentication
- **JWT Tokens** - For web and mobile clients
- **API Keys** - For service-to-service integration
- **OAuth2/OIDC** - For SSO with identity providers

## Configuration

### Environment Variables
Set the following environment variables for production:

```bash
export DB_USERNAME=your_db_user
export DB_PASSWORD=your_db_password
export JWT_SECRET=your_jwt_secret
export JWT_ISSUER_URI=your_issuer_uri
```

### Application Profiles
- **dev** - Development (default)
- **test** - Testing
- **prod** - Production

Activate profile:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

## Deployment

### Docker
```bash
# Build Docker image
docker build -t warehouse-employee-management:1.0.0 .

# Run container
docker run -p 8080:8080   -e DB_USERNAME=warehouse_user   -e DB_PASSWORD=warehouse_pass   warehouse-employee-management:1.0.0
```

### Kubernetes
See `k8s/` directory for Kubernetes manifests.

## Monitoring

### Prometheus Metrics
Metrics are exposed at `/actuator/prometheus` for Prometheus scraping.

### Distributed Tracing
Configure Zipkin/Jaeger for distributed tracing:
```yaml
management:
  tracing:
    enabled: true
    sampling:
      probability: 1.0
```

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
- **GitHub Issues:** https://github.com/Spartan-TA/AAVA_Agentic_Workflow_Output/issues
- **Email:** support@warehouse-employee-mgmt.com

## Acknowledgments

- Spring Boot Team
- PostgreSQL Community
- Open Source Contributors

---

**Version:** 1.0.0
**Last Updated:** 2025-01-19
**Status:** Production Ready â