# Warehouse Employee Management System

A comprehensive Spring Boot application for managing warehouse employees, attendance, scheduling, certifications, safety incidents, and more.

## Features

- **Employee Management**: CRUD operations for employee records with role-based access control
- **Time & Attendance**: Clock-in/out functionality with geofence validation
- **Shift Scheduling**: Create shift templates and assign to employees with conflict detection
- **Leave Management**: Request and approve leave with accrual tracking
- **Certification Tracking**: Track employee certifications with expiry alerts
- **Safety Management**: Record and track safety incidents with OSHA reporting
- **Asset Management**: Assign equipment and PPE to employees
- **Performance Reviews**: Structured review workflows
- **Payroll Integration**: Generate payroll-ready export files
- **Notifications**: Multi-channel notifications for important events

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- PostgreSQL 12+

## Build

```bash
mvn clean install
```

## Database Setup

1. Create PostgreSQL database:
```sql
CREATE DATABASE wms;
CREATE USER wms_user WITH PASSWORD 'wms_password';
GRANT ALL PRIVILEGES ON DATABASE wms TO wms_user;
```

2. Flyway will automatically run migrations on application startup

## Run

```bash
java -jar target/warehouse-employee-management-1.0.0.jar
```

Or using Maven:
```bash
mvn spring-boot:run
```

## Health Check

```bash
curl http://localhost:8080/actuator/health
```

Expected response:
```json
{"status":"UP"}
```

## API Documentation

Once the application is running, access the OpenAPI documentation at:
```
http://localhost:8080/swagger-ui.html
```

## Default Credentials

- Username: `admin`
- Password: `admin`
- Role: `ADMIN`

## Project Structure

```
src/
âââ main/
â   âââ java/com/wms/
â   â   âââ WmsApplication.java
â   â   âââ config/
â   â   â   âââ SecurityConfig.java
â   â   â   âââ ActuatorConfig.java
â   â   âââ employee/
â   â   â   âââ entity/
â   â   â   âââ repository/
â   â   â   âââ service/
â   â   â   âââ controller/
â   â   â   âââ dto/
â   â   âââ attendance/
â   â   âââ scheduling/
â   â   âââ leave/
â   â   âââ certification/
â   â   âââ safety/
â   âââ resources/
â       âââ application.properties
â       âââ db/migration/
â           âââ V1__baseline.sql
â           âââ V2__attendance.sql
â           âââ V3__scheduling.sql
âââ test/
```

## Testing

Run unit tests:
```bash
mvn test
```

## Troubleshooting

### Database Connection Issues
- Ensure PostgreSQL is running on port 5432
- Verify database credentials in `application.properties`
- Check that the `wms` database exists

### Port Already in Use
- Change the port in `application.properties`: `server.port=8081`

### Flyway Migration Errors
- Check migration scripts in `src/main/resources/db/migration`
- Verify database user has sufficient privileges

## Contributing

1. Create a feature branch
2. Make your changes
3. Write/update tests
4. Submit a pull request

## License

Copyright Â© 2024 Warehouse Management System