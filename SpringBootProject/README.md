# Warehouse Employee Management System (WMS)

A production-ready, modular Spring Boot application for managing warehouse employees, attendance, scheduling, compliance, and more.

## Features
- Employee CRUD operations
- Role-based access control (RBAC)
- Time & Attendance (Clock In/Out)
- Shift & Schedule Management
- Leave & Absence Management
- Training & Certification Tracking
- Safety Incidents & OSHA Reporting
- Equipment & Asset Assignment
- Performance Reviews & Goals
- Payroll Export Integration
- Notifications & Announcements
- Integration Layer (HRIS/WMS APIs)
- Audit Trail & Compliance
- Reporting & Analytics
- Mobile Access (PWA)
- Onboarding & Offboarding Workflow
- Localization & Multi-Tenant
- Advanced Scheduling Optimization
- Continuous Improvement & Feedback

## Technology Stack
- Java 17+
- Spring Boot 3.2.x
- PostgreSQL 15+
- Maven
- Spring Security
- Spring Data JPA
- MapStruct
- Flyway
- OpenAPI/Swagger

## Build & Run

1. **Clone the repository**
   ```bash
   git clone <repo-url>
   cd SpringBootProject
   ```
2. **Configure Database**
   - Update `src/main/resources/application.yml` with your PostgreSQL credentials.
3. **Build the project**
   ```bash
   mvn clean install
   ```
4. **Run the application**
   ```bash
   mvn spring-boot:run
   ```
5. **Access API Documentation**
   - Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

## Database Migration
- Flyway will automatically run migration scripts in `src/main/resources/db/migration`.

## Security
- JWT-based authentication (see `SecurityConfig.java`)
- RBAC enforced on all endpoints

## Contributing
- Fork the repo, create a feature branch, and submit a pull request.

## License
- Apache 2.0
