# Warehouse EMS (Employee Management System)

## Project Overview
Warehouse EMS is a comprehensive Employee Management System designed for warehouse operations. It provides robust features for managing employees, attendance, shifts, leave requests, certifications, safety incidents, assets, performance reviews, notifications, audit logs, and multi-tenancy. The application is built using Spring Boot, follows best practices, and is ready for production deployment.

## Prerequisites
- **Java 17**
- **Maven 3.8+**
- **PostgreSQL 13+**

## Build Instructions
1. Clone the repository:
   ```bash
   git clone <your-repo-url>
   cd warehouse-ems
   ```
2. Build the project:
   ```bash
   mvn clean install
   ```

## Run Instructions
- Using Maven:
  ```bash
  mvn spring-boot:run
  ```
- Using the packaged JAR:
  ```bash
  java -jar target/ems-1.0.0.jar
  ```

## Test Instructions
Run unit and integration tests:
```bash
mvn test
```

## API Documentation
- Swagger UI is available at: `http://localhost:8080/swagger-ui.html`
- All REST endpoints are documented and support CRUD operations for each entity.

## Configuration Details
- Main configuration file: `src/main/resources/application.properties`
- Key properties:
  - `server.port=8080`
  - `spring.datasource.url=jdbc:postgresql://localhost:5432/warehouse_ems`
  - `spring.datasource.username=your_db_user`
  - `spring.datasource.password=your_db_password`
  - `spring.jpa.hibernate.ddl-auto=validate`
  - `spring.flyway.enabled=true`
  - `management.endpoints.web.exposure.include=health,metrics,prometheus`
  - `spring.cache.type=simple`

## Database Setup
1. Ensure PostgreSQL is running.
2. Create the database:
   ```sql
   CREATE DATABASE warehouse_ems;
   ```
3. Flyway will automatically run migration scripts on startup (`src/main/resources/db/migration/V1__initial_schema.sql`).

## Deployment Instructions
### Docker
1. Build the JAR:
   ```bash
   mvn clean package
   ```
2. Build the Docker image:
   ```bash
   docker build -t warehouse-ems .
   ```
3. Run the container:
   ```bash
   docker run -p 8080:8080 --env SPRING_DATASOURCE_URL=jdbc:postgresql://<db-host>:5432/warehouse_ems --env SPRING_DATASOURCE_USERNAME=<user> --env SPRING_DATASOURCE_PASSWORD=<password> warehouse-ems
   ```

### Kubernetes
- Use the provided `kubernetes-deployment.yaml` for deployment.
- Includes Deployment, Service, ConfigMap, and health checks.

## Additional Notes
- Caching is enabled for key entities.
- Security is configured with OAuth2 and API keys.
- Multi-tenancy is supported via the Tenant entity.
- Audit logging is enabled for all critical operations.
- PWA manifest is included for enhanced user experience.

## Contact
For support or contributions, please open an issue or submit a pull request.
