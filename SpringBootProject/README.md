# Warehouse Employee Management System

## Overview

The Warehouse Employee Management System is a comprehensive Spring Boot application designed to manage warehouse employees, their schedules, attendance, certifications, safety incidents, and more. The system implements role-based access control (RBAC) and provides RESTful APIs for all operations.

## Features

### Core Modules

- **Employee Management (E02)**: Complete CRUD operations for employee master data with unique badge ID enforcement and soft-delete support
- **Security & RBAC (E03)**: Role-based access control with support for ADMIN, HR, SUPERVISOR, and WORKER roles
- **Time & Attendance (E04)**: Clock in/out tracking with geofencing and device capture
- **Shift Scheduling (E05)**: Recurring shift templates, rotations, and conflict detection
- **Leave Management (E06)**: PTO, sick leave, and unpaid leave request/approval workflow
- **Certifications (E07)**: Track employee certifications, expirations, and renewals
- **Safety Incidents (E08)**: Record and manage safety incidents with OSHA reporting
- **Asset Management (E09)**: Track equipment assignments and check-in/out
- **Performance Reviews (E10)**: Manage employee performance reviews and goals
- **Payroll Integration (E11)**: Generate payroll-ready export files
- **Notifications (E12)**: In-app, email, and SMS notifications
- **Integration APIs (E13)**: REST APIs for HRIS, WMS, and SSO integration
- **Audit Trail (E14)**: Comprehensive audit logging for compliance
- **Reporting & Analytics (E15)**: Operational reports and dashboards
- **Mobile PWA (E16)**: Progressive Web App for mobile access
- **Onboarding/Offboarding (E17)**: Automated employee lifecycle management

## Technology Stack

- **Java**: 17+
- **Spring Boot**: 3.2.5
- **Spring Data JPA**: Database access layer
- **Spring Security**: Authentication and authorization
- **PostgreSQL**: Primary database
- **Flyway**: Database migration management
- **Maven**: Build and dependency management
- **OpenAPI/Swagger**: API documentation
- **Spring Boot Actuator**: Health monitoring and metrics

## Prerequisites

- Java Development Kit (JDK) 17 or higher
- Maven 3.8+
- PostgreSQL 14+ (or Docker for containerized database)
- Git

## Database Setup

### Using Docker (Recommended)

```bash
docker run --name warehouse-postgres   -e POSTGRES_DB=warehouse   -e POSTGRES_USER=warehouse_user   -e POSTGRES_PASSWORD=warehouse_pass   -p 5432:5432   -d postgres:14
```

### Manual PostgreSQL Setup

1. Install PostgreSQL 14+
2. Create database:
   ```sql
   CREATE DATABASE warehouse;
   CREATE USER warehouse_user WITH PASSWORD 'warehouse_pass';
   GRANT ALL PRIVILEGES ON DATABASE warehouse TO warehouse_user;
   ```

## Build Instructions

### Clone the Repository

```bash
git clone https://github.com/Spartan-TA/AAVA_Agentic_Workflow_Output.git
cd AAVA_Agentic_Workflow_Output/SpringBootProject
```

### Build the Project

```bash
mvn clean install
```

This command will:
- Download all dependencies
- Compile the source code
- Run unit tests
- Package the application as a JAR file

### Skip Tests (Optional)

```bash
mvn clean install -DskipTests
```

## Run Instructions

### Using Maven

```bash
mvn spring-boot:run
```

### Using Java JAR

```bash
java -jar target/employee-mgmt-1.0.0.jar
```

### With Custom Configuration

```bash
java -jar target/employee-mgmt-1.0.0.jar   --spring.datasource.url=jdbc:postgresql://localhost:5432/warehouse   --spring.datasource.username=warehouse_user   --spring.datasource.password=warehouse_pass
```

## Configuration

### Application Properties

Edit `src/main/resources/application.yml` to configure:

- **Server Port**: Default is 8080
- **Database Connection**: PostgreSQL URL, username, password
- **Security Mode**: `basic` (default) or `oauth2`
- **Flyway Migrations**: Enabled by default
- **Actuator Endpoints**: Health and info exposed

### Environment Variables

You can override configuration using environment variables:

```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/warehouse
export SPRING_DATASOURCE_USERNAME=warehouse_user
export SPRING_DATASOURCE_PASSWORD=warehouse_pass
export SERVER_PORT=8080
```

## API Documentation

### Swagger UI

Once the application is running, access the interactive API documentation at:

```
http://localhost:8080/swagger-ui.html
```

### OpenAPI Specification

The OpenAPI 3.0 specification is available at:

```
http://localhost:8080/v3/api-docs
```

## Authentication

### Default Users (Development)

The application comes with pre-configured users for testing:

| Username   | Password      | Role       | Access Level                          |
|------------|---------------|------------|---------------------------------------|
| admin      | admin123      | ADMIN      | Full system access                    |
| hr         | hr123         | HR         | Employee management and reporting     |
| supervisor | supervisor123 | SUPERVISOR | Team management and scheduling        |
| worker     | worker123     | WORKER     | Self-service (schedules, attendance)  |

### Basic Authentication

Include credentials in the Authorization header:

```bash
curl -u admin:admin123 http://localhost:8080/api/v1/employees
```

### OAuth2 (Production)

For production deployments, configure OAuth2 in `application.yml`:

```yaml
security:
  mode: oauth2
  oauth2:
    resourceserver:
      jwt:
        issuer-uri: https://your-idp.example.com
```

## API Endpoints

### Employee Management

- `GET /api/v1/employees` - List all employees (paginated)
- `GET /api/v1/employees/{id}` - Get employee by ID
- `GET /api/v1/employees/badge/{badgeId}` - Get employee by badge ID
- `POST /api/v1/employees` - Create new employee
- `PUT /api/v1/employees/{id}` - Update employee
- `DELETE /api/v1/employees/{id}` - Soft-delete employee
- `GET /api/v1/employees/department/{department}` - Get employees by department
- `GET /api/v1/employees/department/{department}/count` - Count employees by department

### Attendance (Planned)

- `POST /api/v1/attendance/clock-in` - Clock in
- `POST /api/v1/attendance/clock-out` - Clock out
- `GET /api/v1/attendance/employee/{employeeId}` - Get attendance records

### Health Check

- `GET /actuator/health` - Application health status
- `GET /actuator/info` - Application information

## Testing

### Run All Tests

```bash
mvn test
```

### Run Specific Test Class

```bash
mvn test -Dtest=EmployeeServiceTest
```

### Integration Tests

```bash
mvn verify
```

## Database Migrations

Flyway manages database schema versions automatically. Migration scripts are located in:

```
src/main/resources/db/migration/
```

### Migration Naming Convention

```
V{version}__{description}.sql
```

Example:
```
V1__create_employees_table.sql
V2__add_attendance_table.sql
```

## Deployment

### Docker Deployment

Create a `Dockerfile`:

```dockerfile
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/employee-mgmt-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

Build and run:

```bash
docker build -t warehouse-employee-mgmt .
docker run -p 8080:8080 warehouse-employee-mgmt
```

### Production Considerations

1. **Database**: Use managed PostgreSQL service (AWS RDS, Azure Database, etc.)
2. **Security**: Replace in-memory users with database-backed authentication
3. **Secrets Management**: Use environment variables or secret management services
4. **Monitoring**: Configure application metrics and logging
5. **Load Balancing**: Deploy multiple instances behind a load balancer
6. **SSL/TLS**: Enable HTTPS for all endpoints

## Troubleshooting

### Application Won't Start

1. Check database connectivity:
   ```bash
   psql -h localhost -U warehouse_user -d warehouse
   ```

2. Verify Java version:
   ```bash
   java -version
   ```

3. Check application logs:
   ```bash
   tail -f logs/application.log
   ```

### Database Connection Issues

- Ensure PostgreSQL is running
- Verify credentials in `application.yml`
- Check firewall rules for port 5432

### Authentication Failures

- Verify username and password
- Check role assignments
- Review security configuration

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is proprietary software developed for warehouse management operations.

## Support

For issues, questions, or contributions, please contact the development team.

## Roadmap

### Phase 1 (Current)
- â Project scaffolding
- â Employee CRUD operations
- â Security and RBAC
- â API documentation

### Phase 2 (In Progress)
- â³ Attendance tracking
- â³ Shift scheduling
- â³ Leave management

### Phase 3 (Planned)
- ð Certifications tracking
- ð Safety incidents
- ð Asset management
- ð Performance reviews

### Phase 4 (Future)
- ð Payroll integration
- ð Notifications
- ð Mobile PWA
- ð Advanced reporting

## Architecture

### Layered Architecture

```
âââââââââââââââââââââââââââââââââââââââ
â         Controller Layer            â  (REST APIs, Request/Response)
âââââââââââââââââââââââââââââââââââââââ¤
â          Service Layer              â  (Business Logic, Validation)
âââââââââââââââââââââââââââââââââââââââ¤
â        Repository Layer             â  (Data Access, JPA)
âââââââââââââââââââââââââââââââââââââââ¤
â         Database Layer              â  (PostgreSQL)
âââââââââââââââââââââââââââââââââââââââ
```

### Package Structure

```
com.warehouse
âââ EmployeeMgmtApplication.java
âââ employee
â   âââ Employee.java (Entity)
â   âââ EmployeeDTO.java (Data Transfer Object)
â   âââ EmployeeRepository.java (Data Access)
â   âââ EmployeeService.java (Business Logic)
â   âââ EmployeeController.java (REST API)
âââ attendance
â   âââ ... (Attendance module)
âââ security
â   âââ WebSecurityConfig.java (Security Configuration)
âââ config
    âââ ... (Application Configuration)
```

## Performance Considerations

- **Pagination**: All list endpoints support pagination to handle large datasets
- **Indexing**: Database indexes on frequently queried fields (badge_id, department, status)
- **Caching**: Consider adding Redis for frequently accessed data
- **Connection Pooling**: HikariCP configured for optimal database connections

## Security Best Practices

- â Role-based access control (RBAC)
- â Password encryption (BCrypt)
- â Soft-delete for data retention
- â Audit logging for sensitive operations
- â ï¸ Replace in-memory users with database authentication in production
- â ï¸ Enable HTTPS/TLS for all endpoints
- â ï¸ Implement rate limiting for API endpoints

---

**Version**: 1.0.0  
**Last Updated**: December 2024  
**Status**: Active Development