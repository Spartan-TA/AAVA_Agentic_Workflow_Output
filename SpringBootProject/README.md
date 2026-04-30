# Warehouse Employee Management System

A production-ready Spring Boot 2.7.14 application for managing warehouse employees, attendance, scheduling, safety, assets, payroll, and more.

## Features

- Employee CRUD with soft delete, pagination, filtering
- Attendance with geofence validation
- Shift & schedule management
- Leave & absence management
- Certification tracking with expiry alerts
- Safety incident logging and OSHA reporting
- Asset assignment and tracking
- Performance reviews & goals
- Payroll export integration
- Notifications (email/SMS/in-app)
- Integration with HRIS/WMS
- Audit logging for all sensitive operations
- Reporting & analytics
- Mobile PWA support
- Localization (English/Spanish)
- Role-based access control (RBAC) with JWT
- Flyway migrations for PostgreSQL
- Actuator health monitoring

## Build & Run

1. **Database**: Ensure PostgreSQL is running and create a database:
    ```
    createdb warehouse_ems
    createuser warehouse --pwprompt
    ```

2. **Configure**: Edit `src/main/resources/application.properties` for DB credentials.

3. **Build**:
    ```
    mvn clean install
    ```

4. **Run**:
    ```
    mvn spring-boot:run
    ```

5. **API Docs**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

6. **Health Check**: [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)

## Testing

Run all tests:
```
mvn test
```

## Localization

- Default: English
- Spanish: Set `Accept-Language: es` header

## Security

- JWT-based authentication
- Roles: ADMIN, HR, SUPERVISOR, WORKER

## License

MIT