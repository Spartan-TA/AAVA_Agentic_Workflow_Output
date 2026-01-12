# Warehouse Employee Management System

A Spring Boot 3.x application for managing warehouse employees, attendance, scheduling, safety, and more.

## Features
- Employee CRUD (with soft-delete)
- Role-based access control (RBAC)
- Time & attendance tracking
- Shift & schedule management
- Leave & absence management
- Training & certification tracking
- Safety incidents & OSHA reporting
- Equipment & asset assignment
- Performance reviews & goals
- Payroll export integration
- Notifications & announcements
- Integration layer (HRIS/WMS APIs)
- Audit trail & compliance
- Reporting & analytics
- Mobile access (PWA)
- Onboarding & offboarding workflow
- Localization & multi-tenant support
- Observability & monitoring
- CI/CD ready

## Tech Stack
- Java 17+
- Spring Boot 3.x
- Spring Data JPA
- Spring Security (OAuth2/JWT)
- PostgreSQL
- Flyway
- Maven
- Spring Boot Actuator

## Build & Run

### Prerequisites
- Java 17+
- Maven 3.8+
- PostgreSQL 13+

### Setup
1. Clone the repository
2. Configure `src/main/resources/application.yml` for your DB credentials
3. Run migrations: `mvn flyway:migrate`
4. Build: `mvn clean package`
5. Run: `java -jar target/warehouse-employee-mgmt-1.0.0.jar`

### API Docs
- Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- Actuator Health: [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)

## License
MIT
