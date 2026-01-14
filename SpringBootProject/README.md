# Warehouse Employee Management System (EMS)

A Spring Boot application for managing warehouse employees, with audit logging, security, and integration capabilities.

## Features
- Employee CRUD operations
- JWT and API Key security
- Multi-tenancy support
- Audit logging (AOP)
- Integration with HRIS and WMS
- OpenAPI/Swagger documentation

## Getting Started

### Prerequisites
- Java 11+
- Maven
- PostgreSQL

### Setup
1. Clone the repository
2. Configure `application.yml` for your environment
3. Run database migrations (`V1__initial_schema.sql`)
4. Build and run the application:
   ```bash
   mvn spring-boot:run
   ```

### API Documentation
Visit `/swagger-ui.html` after starting the application.

## Directory Structure
- `src/main/java/com/warehouse/ems/employee` - Employee module
- `src/main/java/com/warehouse/ems/security` - Security configuration
- `src/main/java/com/warehouse/ems/audit` - Audit logging
- `src/main/java/com/warehouse/ems/integration` - Integration services
- `src/main/java/com/warehouse/ems/common` - Common utilities
- `src/main/java/com/warehouse/ems/config` - Configuration (OpenAPI)

## License
MIT
