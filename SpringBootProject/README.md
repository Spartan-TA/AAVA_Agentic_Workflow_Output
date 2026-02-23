# Warehouse Employee Management System

## Overview

A production-ready Spring Boot 3.x application for managing warehouse employees, shifts, attendance, safety, assets, and more. Implements RBAC, audit trail, reporting, integrations, and mobile PWA access.

## Build & Run

1. **Build:**  
   `./mvnw clean install`

2. **Run:**  
   `./mvnw spring-boot:run`

3. **Configuration:**  
   See `src/main/resources/application.yml` for environment settings.

4. **Database Migration:**  
   Flyway runs automatically on startup. Migration scripts in `src/main/resources/db/migration`.

5. **API Documentation:**  
   Swagger/OpenAPI available at `/swagger-ui.html`.

## Package Structure

- `com.warehouse.management.entity` - JPA entities
- `com.warehouse.management.repository` - Spring Data repositories
- `com.warehouse.management.service` - Business logic
- `com.warehouse.management.controller` - REST controllers
- `com.warehouse.management.dto` - DTOs for API requests/responses
- `com.warehouse.management.config` - Configuration classes
- `com.warehouse.management.security` - Security (RBAC, OAuth2)
- `com.warehouse.management.exception` - Exception handling
- `com.warehouse.management.audit` - Audit trail
- `com.warehouse.management.integration` - External API connectors

## Testing

- Unit tests in `src/test/java/com/warehouse/management`
- Run: `./mvnw test`

## CI/CD

- Example GitHub Actions workflow in `.github/workflows/ci.yml`