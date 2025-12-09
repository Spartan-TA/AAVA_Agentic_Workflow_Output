# Warehouse Employee Management System (EMS)

## Overview
Comprehensive Spring Boot application for managing warehouse employees, scheduling, attendance, safety, certifications, and more.

## Prerequisites
- Java 11+
- Maven 3.6+
- PostgreSQL 12+

## Build & Run

### 1. Database Setup
Create PostgreSQL database:
```sql
CREATE DATABASE warehouse_ems;
CREATE USER ems_user WITH PASSWORD 'ems_password';
GRANT ALL PRIVILEGES ON DATABASE warehouse_ems TO ems_user;
```

### 2. Build
```bash
mvn clean install
```

### 3. Run
```bash
mvn spring-boot:run
```

Application will start on http://localhost:8080

### 4. API Documentation
Access Swagger UI at: http://localhost:8080/swagger-ui.html

### 5. Health Check
http://localhost:8080/actuator/health

## Project Structure
```
src/main/java/com/warehouse/ems/
âââ WarehouseEmsApplication.java (Main entry point)
âââ config/ (Security, Localization configs)
âââ exception/ (Global exception handlers)
âââ employee/ (Employee module)
â   âââ entity/
â   âââ dto/
â   âââ repository/
â   âââ service/
â   âââ controller/
âââ attendance/ (Attendance module)
âââ scheduling/ (Scheduling module)
âââ leave/ (Leave management module)
âââ certification/ (Certification tracking module)
âââ safety/ (Safety incidents module)
âââ asset/ (Asset management module)
âââ review/ (Performance reviews module)
âââ notification/ (Notifications module)
âââ audit/ (Audit logging module)
âââ document/ (Document management module)
```

## Key Features
- Employee CRUD with RBAC
- Clock-in/out with geofence
- Shift scheduling and rotations
- Leave request/approval workflow
- Certification tracking with expiration alerts
- Safety incident reporting and OSHA compliance
- Asset assignment with certification validation
- Performance review management
- Multi-channel notifications
- Comprehensive audit logging
- Payroll export integration
- Mobile PWA support
- Localization (English/Spanish)

## Security
- Spring Security with JWT/OAuth2
- Role-based access control (ADMIN, HR, SUPERVISOR, WORKER)
- Method-level security
- Row-level security for supervisors

## Testing
Run unit tests:
```bash
mvn test
```

## Deployment
Package for deployment:
```bash
mvn clean package
java -jar target/ems-1.0.0.jar
```

## Configuration
Edit `src/main/resources/application.properties` for:
- Database connection
- Security settings
- Notification channels
- Localization
- PWA settings

## Support
For issues or questions, contact the development team.