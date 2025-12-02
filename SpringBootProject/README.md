# Warehouse Employee Management System

## Overview

This is a modular, production-ready Spring Boot application for managing warehouse employees, attendance, scheduling, safety, assets, payroll, and more.

## Features

- Employee CRUD & bulk import
- Role-based access control (RBAC)
- Attendance & shift scheduling
- Leave management
- Training & certification tracking
- Safety incident reporting
- Asset assignment
- Performance reviews
- Payroll export
- Notifications (email/SMS/push)
- Integration with HRIS/WMS
- Audit logging
- Reporting & analytics
- Mobile/PWA support

## Build & Run

1. **Database Setup**: Ensure PostgreSQL is running and create the database:
   ```
   createdb warehouse_ems
   ```

2. **Configure DB Credentials**: Update `src/main/resources/application.properties` if needed.

3. **Build the Project**:
   ```
   mvn clean install
   ```

4. **Run the Application**:
   ```
   mvn spring-boot:run
   ```

5. **API Documentation**: Visit [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

## Testing

- Unit and integration tests can be run with:
  ```
  mvn test
  ```

## Migration

- Flyway migrations run automatically on startup.

## Security

- OAuth2/JWT and API key support.
- RBAC enforced on all endpoints.

## Contact

For issues or contributions, please open a GitHub issue or pull request.