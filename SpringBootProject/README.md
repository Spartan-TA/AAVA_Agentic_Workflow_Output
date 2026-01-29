# Warehouse Employee Management System (Warehouse EMS)

## Overview
Warehouse EMS is a production-ready Spring Boot application for managing warehouse employees, attendance, scheduling, safety, assets, and compliance. It implements 20 epics covering all major HR, operational, and regulatory workflows for warehouse environments.

## Features
- Employee master data CRUD with soft-delete, filtering, pagination
- Role-based access control (RBAC) with JWT/OAuth2
- Time & attendance (clock in/out, corrections, geofence)
- Shift & schedule management
- Leave & absence management
- Training & certification tracking
- Safety incidents & OSHA reporting
- Equipment & asset assignment
- Performance reviews & goals
- Payroll export integration
- Notifications & announcements
- Integration layer (HRIS/WMS APIs, webhooks)
- Audit trail & compliance
- Reporting & analytics
- Mobile access (PWA, offline support)
- Onboarding & offboarding workflows
- Localization & multi-warehouse support
- Disaster recovery & backup
- Performance & scalability (Redis caching)

## Tech Stack
- Spring Boot 2.7+
- Maven
- PostgreSQL
- Flyway
- Spring Security (JWT/OAuth2)
- Spring Data JPA
- Redis
- Springdoc OpenAPI
- Actuator
- Lombok

## Setup Instructions
1. **Clone the repository**
   ```bash
   git clone https://github.com/Spartan-TA/AAVA_Agentic_Workflow_Output.git
   cd AAVA_Agentic_Workflow_Output/SpringBootProject
   ```
2. **Configure Database**
   - Create a PostgreSQL database named `warehouse_ems`.
   - Create a user `ems_user` with password and grant privileges.
   - Set environment variable `DB_PASSWORD` for database password.
3. **Configure Redis**
   - Install and start Redis on localhost:6379.
4. **Build the project**
   ```bash
   mvn clean install
   ```
5. **Run the application**
   ```bash
   mvn spring-boot:run
   ```
6. **Access endpoints**
   - API: http://localhost:8080/api/
   - Actuator: http://localhost:8080/actuator/health
   - OpenAPI docs: http://localhost:8080/swagger-ui.html

## Module Structure
- `com.warehouseems.config` - Security, cache, Flyway, localization configs
- `com.warehouseems.employee` - Employee entity, repository, service, controller, DTOs
- `com.warehouseems.scheduling` - Shift, schedule entities and services
- `com.warehouseems.attendance` - Clock events, reporting
- `com.warehouseems.safety` - Incidents, OSHA reporting
- `com.warehouseems.leave` - Leave requests, approvals
- `com.warehouseems.certification` - Training, cert tracking
- `com.warehouseems.asset` - Equipment assignment
- `com.warehouseems.review` - Performance reviews
- `com.warehouseems.payroll` - Payroll export integration
- `com.warehouseems.notification` - Multi-channel notifications
- `com.warehouseems.integration` - HRIS/WMS APIs
- `com.warehouseems.audit` - Audit logging
- `com.warehouseems.reporting` - Analytics, exports
- `com.warehouseems.common` - Shared DTOs, utilities, exceptions

## Database Migration
- Flyway migration scripts are located in `src/main/resources/db/migration`.
- On first run, baseline schema is created automatically.

## Testing
- Unit and integration tests are in `src/test/java`.
- Run tests with:
   ```bash
   mvn test
   ```

## License
MIT
