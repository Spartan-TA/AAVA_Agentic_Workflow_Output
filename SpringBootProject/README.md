# Warehouse Employee Management System (WEMS)

## Overview
The Warehouse Employee Management System is a comprehensive Spring Boot application designed to manage all aspects of warehouse employee operations including employee records, time tracking, scheduling, leave management, training certifications, safety incidents, asset management, performance reviews, and multi-warehouse support.

## Technology Stack
- **Framework:** Spring Boot 3.2.5
- **Language:** Java 17+
- **Build Tool:** Maven 3.8+
- **Database:** PostgreSQL 14+
- **Migration Tool:** Flyway
- **Security:** Spring Security with JWT
- **Documentation:** SpringDoc OpenAPI 3
- **Monitoring:** Spring Boot Actuator
- **Testing:** JUnit 5, Mockito

## Prerequisites
- Java Development Kit (JDK) 17 or higher
- Apache Maven 3.8 or higher
- PostgreSQL 14 or higher
- Git

## Database Setup

### 1. Install PostgreSQL
Ensure PostgreSQL is installed and running on your system.

### 2. Create Database
```sql
CREATE DATABASE wems;
CREATE USER wems_user WITH PASSWORD 'wems_password';
GRANT ALL PRIVILEGES ON DATABASE wems TO wems_user;
```

### 3. Configure Database Connection
Update `src/main/resources/application.yml` with your database credentials:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/wems
    username: wems_user
    password: wems_password
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

### Option 1: Using Maven
```bash
mvn spring-boot:run
```

### Option 2: Using Java JAR
```bash
java -jar target/wems-1.0.0.jar
```

### Option 3: With Custom Profile
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

## Application Access

Once the application is running, you can access:

- **Application:** http://localhost:8080
- **Health Check:** http://localhost:8080/actuator/health
- **API Documentation:** http://localhost:8080/swagger-ui.html (if configured)

## Project Structure

```
SpringBootProject/
âââ src/
â   âââ main/
â   â   âââ java/
â   â   â   âââ com/
â   â   â       âââ company/
â   â   â           âââ wems/
â   â   â               âââ WarehouseEmsApplication.java
â   â   â               âââ employee/
â   â   â               â   âââ Employee.java
â   â   â               â   âââ EmployeeRepository.java
â   â   â               â   âââ EmployeeService.java
â   â   â               â   âââ EmployeeController.java
â   â   â               âââ attendance/
â   â   â               âââ scheduling/
â   â   â               âââ leave/
â   â   â               âââ training/
â   â   â               âââ safety/
â   â   â               âââ asset/
â   â   â               âââ performance/
â   â   â               âââ warehouse/
â   â   â               âââ forecast/
â   â   â               âââ portal/
â   â   â               âââ security/
â   â   â               â   âââ SecurityConfig.java
â   â   â               âââ exception/
â   â   â                   âââ ResourceNotFoundException.java
â   â   â                   âââ GlobalExceptionHandler.java
â   â   âââ resources/
â   â       âââ application.yml
â   â       âââ db/
â   â           âââ migration/
â   â               âââ V1__baseline_schema.sql
â   âââ test/
â       âââ java/
âââ pom.xml
âââ README.md
```

## API Endpoints

### Employee Management
- `GET /api/employees` - Get all employees
- `GET /api/employees/{id}` - Get employee by ID
- `POST /api/employees` - Create new employee
- `PUT /api/employees/{id}` - Update employee
- `DELETE /api/employees/{id}` - Delete employee (soft delete)

### Attendance
- `POST /api/attendance/clock-in` - Clock in
- `POST /api/attendance/clock-out` - Clock out
- `GET /api/attendance/history/{employeeId}` - Get attendance history

### Scheduling
- `GET /api/scheduling/shift-templates` - Get all shift templates
- `POST /api/scheduling/shift-templates` - Create shift template
- `PUT /api/scheduling/shift-templates/{id}` - Update shift template
- `DELETE /api/scheduling/shift-templates/{id}` - Delete shift template

### Leave Management
- `POST /api/leave/request` - Submit leave request
- `POST /api/leave/approve/{id}` - Approve leave request
- `POST /api/leave/reject/{id}` - Reject leave request
- `GET /api/leave/history/{employeeId}` - Get leave history

## Security

The application uses Spring Security with role-based access control (RBAC):

### Roles
- **ADMIN:** Full system access
- **HR:** Employee management and reporting
- **SUPERVISOR:** Team management and approvals
- **WORKER:** Self-service portal access

### Authentication
The application uses JWT (JSON Web Token) for stateless authentication.

## Configuration

### Application Properties
Key configuration properties in `application.yml`:

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/wems
    username: wems_user
    password: wems_password
  jpa:
    hibernate:
      ddl-auto: none
    show-sql: true
  flyway:
    enabled: true

security:
  jwt:
    secret: supersecretjwtkey
    expiration: 3600000
```

### Environment-Specific Configuration
- `application-dev.yml` - Development environment
- `application-prod.yml` - Production environment

## Testing

### Run All Tests
```bash
mvn test
```

### Run Specific Test Class
```bash
mvn test -Dtest=EmployeeServiceTest
```

### Generate Test Coverage Report
```bash
mvn clean test jacoco:report
```

## Database Migrations

Flyway automatically runs database migrations on application startup. Migration scripts are located in `src/main/resources/db/migration/`.

### Migration Naming Convention
- `V1__baseline_schema.sql` - Initial schema
- `V2__add_indexes.sql` - Add performance indexes
- `V3__add_audit_fields.sql` - Add audit fields

## Deployment

### Docker Deployment
```bash
# Build Docker image
docker build -t wems:latest .

# Run container
docker run -p 8080:8080 -e SPRING_PROFILES_ACTIVE=prod wems:latest
```

### Production Deployment Checklist
1. Update database credentials
2. Configure JWT secret key
3. Enable HTTPS
4. Configure logging levels
5. Set up monitoring and alerting
6. Configure backup strategy
7. Review security settings

## Monitoring

Spring Boot Actuator endpoints are available at:
- `/actuator/health` - Application health status
- `/actuator/info` - Application information
- `/actuator/metrics` - Application metrics

## Troubleshooting

### Common Issues

**Issue:** Application fails to start
- **Solution:** Check database connection settings and ensure PostgreSQL is running

**Issue:** Port 8080 already in use
- **Solution:** Change port in `application.yml` or stop the process using port 8080

**Issue:** Flyway migration fails
- **Solution:** Check migration scripts for syntax errors and ensure database user has proper permissions

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

This project is proprietary software developed for warehouse management operations.

## Contact

For questions or support, please contact:
- **Email:** support@company.com
- **GitHub:** https://github.com/Spartan-TA/AAVA_Agentic_Workflow_Output

## Version History

### Version 1.0.0 (Current)
- Initial release
- Employee management module
- Time and attendance tracking
- Shift scheduling
- Leave management
- Training and certification tracking
- Safety incident reporting
- Asset management
- Performance reviews
- Multi-warehouse support
- Self-service portal

---

**Built with Spring Boot** | **Powered by Java 17**