# Warehouse Employee Management System (EMS)

## Overview

This is a comprehensive Spring Boot application for managing warehouse employees, attendance, scheduling, safety, assets, and more. It implements robust security, audit, reporting, and integration features.

## Tech Stack

- Java 17
- Spring Boot 3.2.x
- Maven
- PostgreSQL
- Spring Security (JWT)
- Flyway
- Spring Data JPA
- Spring Boot Actuator
- OpenAPI/Swagger

## Build & Run

1. **Database Setup**:  
   Create a PostgreSQL database named `warehouse_ems` and a user with appropriate privileges.

2. **Configure DB Credentials**:  
   Update `src/main/resources/application.yml` with your DB username and password.

3. **Build Project**:  
   ```
   mvn clean install
   ```

4. **Run Application**:  
   ```
   mvn spring-boot:run
   ```

5. **API Documentation**:  
   Visit [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) for OpenAPI docs.

6. **Health Check**:  
   Visit [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)

## Modules

- Employee Management (CRUD, RBAC)
- Attendance & Scheduling
- Leave & Absence
- Training & Certification
- Safety & OSHA
- Equipment & Asset Assignment
- Performance Reviews
- Payroll Export
- Notifications
- Integration Layer
- Audit Trail
- Reporting & Analytics
- Mobile PWA
- Onboarding/Offboarding

## Testing

Unit and integration tests are located under `src/test/java`.

## Migration

Flyway migration scripts are in `src/main/resources/db/migration`.

## Contact

For issues or contributions, please open a GitHub issue or pull request.