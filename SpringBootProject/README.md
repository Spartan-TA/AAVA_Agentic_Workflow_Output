# Warehouse Employee Management System (EMS)

Production-ready Spring Boot application for managing warehouse employees, scheduling, attendance, safety, assets, payroll, notifications, integrations, audit, reporting, and more.

## Features
- Employee CRUD
- RBAC Security
- Time & Attendance
- Shift Scheduling
- Leave Management
- Certification Tracking
- Safety Incidents
- Asset Management
- Performance Reviews
- Payroll Integration
- Notifications
- HRIS/WMS Integration
- Audit Trail
- Reporting & Analytics
- Mobile PWA
- Onboarding/Offboarding
- Localization
- AI/ML Scheduling
- CI/CD

## Build & Run

### Prerequisites
- Java 17+
- Maven 3.8+
- PostgreSQL (or compatible DB)

### Setup
1. Clone the repository:
   ```bash
   git clone <repo-url>
   cd SpringBootProject
   ```
2. Configure database in `src/main/resources/application.yml`.
3. Run Flyway migrations:
   ```bash
   mvn flyway:migrate
   ```

### Build
```bash
mvn clean install
```

### Run
```bash
mvn spring-boot:run
```

Application runs on [http://localhost:8080/api](http://localhost:8080/api)

### Health Check
```bash
curl http://localhost:8080/api/actuator/health
```

### API Documentation
- Swagger UI: [http://localhost:8080/api/swagger-ui.html](http://localhost:8080/api/swagger-ui.html)

### Testing
```bash
mvn test
```

## Package Structure
- `com.wms.ems.employee` - Employee CRUD
- `com.wms.ems.scheduling` - Shift & Schedule
- `com.wms.ems.attendance` - Time & Attendance
- `com.wms.ems.safety` - Safety Incidents
- `com.wms.ems.leave` - Leave Management
- `com.wms.ems.certification` - Certification Tracking
- `com.wms.ems.asset` - Asset Management
- `com.wms.ems.performance` - Performance Reviews
- `com.wms.ems.payroll` - Payroll Integration
- `com.wms.ems.notification` - Notifications
- `com.wms.ems.integration` - HRIS/WMS Integration
- `com.wms.ems.audit` - Audit Trail
- `com.wms.ems.reporting` - Reporting & Analytics
- `com.wms.ems.config` - Security, App Config
- `com.wms.ems.common` - Shared Utilities

## License
MIT
