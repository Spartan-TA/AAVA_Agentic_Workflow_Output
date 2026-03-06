# Warehouse Employee Management System

## Build & Run

1. **Prerequisites**: Java 17+, Maven, PostgreSQL
2. **Database**: Create database `warehouse_ems` and user as per `application.yml`
3. **Build**:  
   ```
   mvn clean install
   ```
4. **Run**:  
   ```
   mvn spring-boot:run
   ```
5. **API Docs**:  
   Visit [http://localhost:8080/api/swagger-ui.html](http://localhost:8080/api/swagger-ui.html)
6. **Health Check**:  
   ```
   curl http://localhost:8080/api/actuator/health
   ```

## Profiles

- `dev`: Local development
- `prod`: Production

## Flyway

- DB migrations auto-run on startup.

## Structure

- `com.warehouse.ems`: Root package
  - `employee`, `security`, `attendance`, `shift`, `leave`, `certification`, `safety`, `asset`, `review`, `payroll`, `notification`, `integration`, `audit`, `reporting`, `mobile`, `onboarding`