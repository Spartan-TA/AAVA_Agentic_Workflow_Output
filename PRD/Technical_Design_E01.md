# Technical Design Document - Epic E01
## Project Scaffolding & Domain Setup

### User Story
As a developer, I want to initialize a Spring Boot (Maven) project with base packages and core modules so that I can accelerate delivery and enforce consistency.

### 1. Architecture Overview

**Framework Stack:**
- Spring Boot 3.2.x
- Java 17 LTS
- Maven 3.9.x
- PostgreSQL 15.x
- Flyway 9.x

**Layered Architecture:**
- Controller Layer (REST APIs)
- Service Layer (Business Logic)
- Repository Layer (Data Access)
- Domain Layer (Entities)

### 2. Package Structure

```
com.warehouse.ems
âââ WarehouseEmsApplication.java
âââ config/
âââ common/
âââ employee/
âââ scheduling/
âââ attendance/
âââ safety/
```

### 3. Maven Configuration

**Key Dependencies:**
```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>
    <dependency>
        <groupId>org.flywaydb</groupId>
        <artifactId>flyway-core</artifactId>
    </dependency>
</dependencies>
```

### 4. Application Configuration

```yaml
server:
  port: 8080

spring:
  application:
    name: warehouse-ems
  datasource:
    url: jdbc:postgresql://localhost:5432/warehouse_ems
  flyway:
    enabled: true
    baseline-on-migrate: true

management:
  endpoints:
    web:
      exposure:
        include: health,info
```

### 5. Main Application Class

```java
@SpringBootApplication
@EnableJpaAuditing
public class WarehouseEmsApplication {
    public static void main(String[] args) {
        SpringApplication.run(WarehouseEmsApplication.class, args);
    }
}
```

### 6. Database Migration

**Flyway Baseline Migration:**
```sql
-- V1__baseline_schema.sql
CREATE TABLE employees (
    id BIGSERIAL PRIMARY KEY,
    badge_id VARCHAR(50) UNIQUE NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### 7. Actuator Health Check

**Endpoint:** `/actuator/health`
**Expected Response:** `{"status":"UP"}`

### 8. README

```markdown
# Warehouse EMS

## Build
mvn clean install

## Run
mvn spring-boot:run

## Health Check
curl http://localhost:8080/actuator/health
```

### Acceptance Criteria Met
â Project builds and runs on port 8080
â Actuator health endpoint returns UP
â Flyway baseline migration applied
â README with build/run steps
â Base package structure created
