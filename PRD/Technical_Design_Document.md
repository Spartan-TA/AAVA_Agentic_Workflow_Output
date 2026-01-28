# WAREHOUSE EMPLOYEE MANAGEMENT SYSTEM
## LOW-LEVEL TECHNICAL DESIGN DOCUMENT

---

## TABLE OF CONTENTS

1. [System Overview](#system-overview)
2. [Architecture & Technology Stack](#architecture--technology-stack)
3. [Package Structure](#package-structure)
4. [Epic E01: Project Scaffolding & Domain Setup](#epic-e01-project-scaffolding--domain-setup)
5. [Epic E02: Employee Master Data (CRUD)](#epic-e02-employee-master-data-crud)
6. [Epic E03: Role-Based Access Control (RBAC)](#epic-e03-role-based-access-control-rbac)
7. [Epic E04: Time & Attendance (Clock In/Out)](#epic-e04-time--attendance-clock-inout)
8. [Epic E05: Shift & Schedule Management](#epic-e05-shift--schedule-management)
9. [Epic E06: Leave & Absence Management](#epic-e06-leave--absence-management)
10. [Epic E07: Training & Certification Tracking](#epic-e07-training--certification-tracking)
11. [Epic E08: Safety Incidents & OSHA Reporting](#epic-e08-safety-incidents--osha-reporting)
12. [Epic E09: Equipment & Asset Assignment](#epic-e09-equipment--asset-assignment)
13. [Epic E10: Performance Reviews & Goals](#epic-e10-performance-reviews--goals)
14. [Epic E11: Payroll Export Integration](#epic-e11-payroll-export-integration)
15. [Epic E12: Notifications & Announcements](#epic-e12-notifications--announcements)
16. [Epic E13: Integration Layer (HRIS/WMS APIs)](#epic-e13-integration-layer-hriswms-apis)
17. [Epic E14: Audit Trail & Compliance](#epic-e14-audit-trail--compliance)
18. [Epic E15: Reporting & Analytics](#epic-e15-reporting--analytics)
19. [Epic E16: Mobile Access (PWA)](#epic-e16-mobile-access-pwa)
20. [Epic E17: Onboarding & Offboarding Workflow](#epic-e17-onboarding--offboarding-workflow)
21. [Epic E18: Localization & Multi-Tenant](#epic-e18-localization--multi-tenant)
22. [Epic E19: Advanced Scheduling (AI/Optimization)](#epic-e19-advanced-scheduling-aioptimization)
23. [Epic E20: Continuous Deployment & Observability](#epic-e20-continuous-deployment--observability)
24. [Cross-Cutting Concerns](#cross-cutting-concerns)
25. [Database Schema](#database-schema)
26. [API Documentation Standards](#api-documentation-standards)

---

## SYSTEM OVERVIEW

### Purpose
The Warehouse Employee Management System is a comprehensive Spring Boot application designed to manage all aspects of warehouse employee lifecycle, including attendance tracking, scheduling, safety compliance, training, and performance management.

### Key Objectives
- Centralized employee data management
- Automated time and attendance tracking
- Intelligent shift scheduling with conflict detection
- Safety incident tracking and OSHA compliance
- Integration with external HRIS and WMS systems
- Mobile-first user experience
- Comprehensive audit trail and reporting

---

## ARCHITECTURE & TECHNOLOGY STACK

### Section: System Architecture

**Description:**
The system follows a layered architecture pattern with clear separation of concerns:

1. **Presentation Layer**: REST APIs with OpenAPI documentation
2. **Service Layer**: Business logic and orchestration
3. **Repository Layer**: Data access using Spring Data JPA
4. **Domain Layer**: Entity models and value objects
5. **Infrastructure Layer**: Cross-cutting concerns (security, logging, caching)

**Design Specification:**

- **Architecture Pattern**: Layered Architecture with Domain-Driven Design principles
- **Communication**: Synchronous REST APIs, Asynchronous event-driven for notifications
- **Data Storage**: PostgreSQL for primary data, Redis for caching
- **Security**: Spring Security with JWT/OAuth2
- **API Documentation**: OpenAPI 3.0 (Springdoc)

### Technology Stack

```yaml
Core Framework:
  - Spring Boot: 3.2.x
  - Java: 17 (LTS)
  - Maven: 3.9.x

Data Layer:
  - Spring Data JPA: 3.2.x
  - Hibernate: 6.4.x
  - PostgreSQL: 15.x
  - Flyway: 9.x (Database Migrations)
  - Redis: 7.x (Caching)

Security:
  - Spring Security: 6.2.x
  - JWT: jjwt 0.12.x
  - OAuth2 Resource Server

API & Documentation:
  - Spring Web MVC
  - Springdoc OpenAPI: 2.3.x
  - Jackson: 2.16.x

Monitoring & Observability:
  - Spring Boot Actuator
  - Micrometer: 1.12.x
  - Prometheus
  - Grafana

Testing:
  - JUnit 5
  - Mockito
  - TestContainers
  - REST Assured

Build & Deployment:
  - Maven
  - Docker
  - GitHub Actions
```

**Sample Implementation:**

```xml
<!-- pom.xml -->
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.1</version>
        <relativePath/>
    </parent>
    
    <groupId>com.warehouse</groupId>
    <artifactId>employee-management</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <name>Warehouse Employee Management System</name>
    
    <properties>
        <java.version>17</java.version>
        <springdoc.version>2.3.0</springdoc.version>
        <jjwt.version>0.12.3</jjwt.version>
    </properties>
    
    <dependencies>
        <!-- Spring Boot Starters -->
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
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-cache</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis</artifactId>
        </dependency>
        
        <!-- Database -->
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-core</artifactId>
        </dependency>
        
        <!-- API Documentation -->
        <dependency>
            <groupId>org.springdoc</groupId>
            <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
            <version>${springdoc.version}</version>
        </dependency>
        
        <!-- Security -->
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-api</artifactId>
            <version>${jjwt.version}</version>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-impl</artifactId>
            <version>${jjwt.version}</version>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-jackson</artifactId>
            <version>${jjwt.version}</version>
            <scope>runtime</scope>
        </dependency>
        
        <!-- Utilities -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.mapstruct</groupId>
            <artifactId>mapstruct</artifactId>
            <version>1.5.5.Final</version>
        </dependency>
        
        <!-- Testing -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.security</groupId>
            <artifactId>spring-security-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>postgresql</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
    
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

---

## PACKAGE STRUCTURE

### Section: Package Organization

**Description:**
The application follows a feature-based package structure organized by domain modules, promoting high cohesion and loose coupling.

**Design Specification:**

```
com.warehouse.employeemanagement/
âââ WarehouseEmployeeManagementApplication.java
âââ config/
â   âââ SecurityConfig.java
â   âââ CacheConfig.java
â   âââ OpenApiConfig.java
â   âââ AsyncConfig.java
â   âââ JpaAuditingConfig.java
âââ common/
â   âââ dto/
â   â   âââ PageResponse.java
â   â   âââ ApiResponse.java
â   â   âââ ErrorResponse.java
â   âââ exception/
â   â   âââ GlobalExceptionHandler.java
â   â   âââ ResourceNotFoundException.java
â   â   âââ BusinessException.java
â   â   âââ ValidationException.java
â   âââ util/
â   â   âââ DateTimeUtil.java
â   â   âââ ValidationUtil.java
â   âââ constants/
â       âââ AppConstants.java
â       âââ ErrorCodes.java
âââ security/
â   âââ jwt/
â   â   âââ JwtTokenProvider.java
â   â   âââ JwtAuthenticationFilter.java
â   â   âââ JwtAuthenticationEntryPoint.java
â   âââ oauth2/
â   â   âââ OAuth2ResourceServerConfig.java
â   âââ UserPrincipal.java
â   âââ SecurityUtils.java
âââ employee/
â   âââ domain/
â   â   âââ Employee.java
â   â   âââ EmployeeStatus.java
â   â   âââ Role.java
â   âââ dto/
â   â   âââ EmployeeRequest.java
â   â   âââ EmployeeResponse.java
â   â   âââ EmployeeMapper.java
â   âââ repository/
â   â   âââ EmployeeRepository.java
â   â   âââ EmployeeRepositoryCustom.java
â   âââ service/
â   â   âââ EmployeeService.java
â   â   âââ EmployeeServiceImpl.java
â   âââ controller/
â       âââ EmployeeController.java
âââ attendance/
â   âââ domain/
â   â   âââ AttendanceEvent.java
â   â   âââ EventType.java
â   â   âââ AttendanceCorrection.java
â   âââ dto/
â   â   âââ ClockInRequest.java
â   â   âââ ClockOutRequest.java
â   â   âââ AttendanceResponse.java
â   â   âââ AttendanceMapper.java
â   âââ repository/
â   â   âââ AttendanceEventRepository.java
â   â   âââ AttendanceCorrectionRepository.java
â   âââ service/
â   â   âââ AttendanceService.java
â   â   âââ AttendanceServiceImpl.java
â   â   âââ AttendanceCalculationService.java
â   âââ controller/
â       âââ AttendanceController.java
âââ scheduling/
â   âââ domain/
â   â   âââ Shift.java
â   â   âââ ShiftTemplate.java
â   â   âââ ShiftAssignment.java
â   â   âââ ShiftStatus.java
â   âââ dto/
â   â   âââ ShiftRequest.java
â   â   âââ ShiftResponse.java
â   â   âââ ShiftAssignmentRequest.java
â   â   âââ ShiftMapper.java
â   âââ repository/
â   â   âââ ShiftRepository.java
â   â   âââ ShiftTemplateRepository.java
â   â   âââ ShiftAssignmentRepository.java
â   âââ service/
â   â   âââ ShiftService.java
â   â   âââ ShiftServiceImpl.java
â   â   âââ ConflictDetectionService.java
â   â   âââ SchedulingOptimizationService.java
â   âââ controller/
â       âââ ShiftController.java
âââ leave/
â   âââ domain/
â   â   âââ LeaveRequest.java
â   â   âââ LeaveType.java
â   â   âââ LeaveStatus.java
â   â   âââ LeaveBalance.java
â   âââ dto/
â   â   âââ LeaveRequestDto.java
â   â   âââ LeaveApprovalDto.java
â   â   âââ LeaveMapper.java
â   âââ repository/
â   â   âââ LeaveRequestRepository.java
â   â   âââ LeaveBalanceRepository.java
â   âââ service/
â   â   âââ LeaveService.java
â   â   âââ LeaveServiceImpl.java
â   â   âââ LeaveAccrualService.java
â   âââ controller/
â       âââ LeaveController.java
âââ training/
â   âââ domain/
â   â   âââ Certification.java
â   â   âââ CertificationType.java
â   â   âââ CertificationStatus.java
â   âââ dto/
â   â   âââ CertificationRequest.java
â   â   âââ CertificationResponse.java
â   â   âââ CertificationMapper.java
â   âââ repository/
â   â   âââ CertificationRepository.java
â   âââ service/
â   â   âââ CertificationService.java
â   â   âââ CertificationServiceImpl.java
â   â   âââ CertificationExpiryService.java
â   âââ controller/
â       âââ CertificationController.java
âââ safety/
â   âââ domain/
â   â   âââ SafetyIncident.java
â   â   âââ IncidentSeverity.java
â   â   âââ IncidentStatus.java
â   â   âââ CorrectiveAction.java
â   âââ dto/
â   â   âââ IncidentRequest.java
â   â   âââ IncidentResponse.java
â   â   âââ OshaReportDto.java
â   â   âââ SafetyMapper.java
â   âââ repository/
â   â   âââ SafetyIncidentRepository.java
â   â   âââ CorrectiveActionRepository.java
â   âââ service/
â   â   âââ SafetyIncidentService.java
â   â   âââ SafetyIncidentServiceImpl.java
â   â   âââ OshaReportingService.java
â   âââ controller/
â       âââ SafetyIncidentController.java
âââ asset/
â   âââ domain/
â   â   âââ Asset.java
â   â   âââ AssetType.java
â   â   âââ AssetCondition.java
â   â   âââ AssetAssignment.java
â   âââ dto/
â   â   âââ AssetRequest.java
â   â   âââ AssetResponse.java
â   â   âââ CheckoutRequest.java
â   â   âââ AssetMapper.java
â   âââ repository/
â   â   âââ AssetRepository.java
â   â   âââ AssetAssignmentRepository.java
â   âââ service/
â   â   âââ AssetService.java
â   â   âââ AssetServiceImpl.java
â   âââ controller/
â       âââ AssetController.java
âââ performance/
â   âââ domain/
â   â   âââ PerformanceReview.java
â   â   âââ ReviewCycle.java
â   â   âââ ReviewTemplate.java
â   â   âââ ReviewStatus.java
â   âââ dto/
â   â   âââ ReviewRequest.java
â   â   âââ ReviewResponse.java
â   â   âââ PerformanceMapper.java
â   âââ repository/
â   â   âââ PerformanceReviewRepository.java
â   â   âââ ReviewCycleRepository.java
â   âââ service/
â   â   âââ PerformanceReviewService.java
â   â   âââ PerformanceReviewServiceImpl.java
â   âââ controller/
â       âââ PerformanceReviewController.java
âââ payroll/
â   âââ domain/
â   â   âââ PayrollExport.java
â   â   âââ PayrollRecord.java
â   âââ dto/
â   â   âââ PayrollExportRequest.java
â   â   âââ PayrollExportResponse.java
â   âââ repository/
â   â   âââ PayrollExportRepository.java
â   âââ service/
â   â   âââ PayrollExportService.java
â   â   âââ PayrollExportServiceImpl.java
â   â   âââ PayrollReconciliationService.java
â   âââ controller/
â       âââ PayrollExportController.java
âââ notification/
â   âââ domain/
â   â   âââ Notification.java
â   â   âââ NotificationChannel.java
â   â   âââ NotificationStatus.java
â   â   âââ Announcement.java
â   âââ dto/
â   â   âââ NotificationRequest.java
â   â   âââ NotificationResponse.java
â   âââ repository/
â   â   âââ NotificationRepository.java
â   â   âââ AnnouncementRepository.java
â   âââ service/
â   â   âââ NotificationService.java
â   â   âââ NotificationServiceImpl.java
â   â   âââ EmailNotificationService.java
â   â   âââ SmsNotificationService.java
â   â   âââ InAppNotificationService.java
â   âââ controller/
â       âââ NotificationController.java
âââ integration/
â   âââ hris/
â   â   âââ HrisClient.java
â   â   âââ HrisSyncService.java
â   â   âââ dto/
â   âââ wms/
â   â   âââ WmsClient.java
â   â   âââ WmsSyncService.java
â   â   âââ dto/
â   âââ webhook/
â   â   âââ WebhookService.java
â   â   âââ WebhookEvent.java
â   â   âââ WebhookRepository.java
â   âââ controller/
â       âââ IntegrationController.java
âââ audit/
â   âââ domain/
â   â   âââ AuditLog.java
â   â   âââ AuditAction.java
â   âââ repository/
â   â   âââ AuditLogRepository.java
â   âââ service/
â   â   âââ AuditService.java
â   â   âââ AuditServiceImpl.java
â   âââ aspect/
â   â   âââ AuditAspect.java
â   âââ controller/
â       âââ AuditController.java
âââ reporting/
â   âââ dto/
â   â   âââ ReportRequest.java
â   â   âââ ReportResponse.java
â   â   âââ DashboardMetrics.java
â   âââ service/
â   â   âââ ReportingService.java
â   â   âââ ReportingServiceImpl.java
â   â   âââ AttendanceReportService.java
â   â   âââ SafetyReportService.java
â   â   âââ ExportService.java
â   âââ controller/
â       âââ ReportingController.java
âââ onboarding/
â   âââ domain/
â   â   âââ OnboardingTask.java
â   â   âââ OffboardingTask.java
â   â   âââ TaskStatus.java
â   âââ dto/
â   â   âââ OnboardingRequest.java
â   â   âââ TaskResponse.java
â   âââ repository/
â   â   âââ OnboardingTaskRepository.java
â   â   âââ OffboardingTaskRepository.java
â   âââ service/
â   â   âââ OnboardingService.java
â   â   âââ OnboardingServiceImpl.java
â   â   âââ OffboardingService.java
â   â   âââ OffboardingServiceImpl.java
â   âââ controller/
â       âââ OnboardingController.java
âââ localization/
    âââ service/
    â   âââ LocalizationService.java
    â   âââ LocalizationServiceImpl.java
    âââ config/
        âââ LocaleConfig.java
```

**Sample Implementation:**

```java
// Main Application Class
package com.warehouse.employeemanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaAuditing
@EnableCaching
@EnableAsync
@EnableScheduling
public class WarehouseEmployeeManagementApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(WarehouseEmployeeManagementApplication.class, args);
    }
}
```

---

## EPIC E01: PROJECT SCAFFOLDING & DOMAIN SETUP

### Section: Application Configuration

**Description:**
Initialize Spring Boot project with Maven, configure base packages, set up database migrations with Flyway, and enable Spring Boot Actuator for health monitoring.

**Design Specification:**

- **Build Tool**: Maven 3.9.x
- **Java Version**: 17 (LTS)
- **Spring Boot Version**: 3.2.1
- **Database Migration**: Flyway
- **Health Monitoring**: Spring Boot Actuator
- **Default Port**: 8080

**Sample Implementation:**

```yaml
# application.yml
spring:
  application:
    name: warehouse-employee-management
  
  datasource:
    url: jdbc:postgresql://localhost:5432/warehouse_db
    username: ${DB_USERNAME:warehouse_user}
    password: ${DB_PASSWORD:warehouse_pass}
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
  
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
        use_sql_comments: true
        jdbc:
          batch_size: 20
        order_inserts: true
        order_updates: true
    open-in-view: false
  
  flyway:
    enabled: true
    baseline-on-migrate: true
    locations: classpath:db/migration
    validate-on-migrate: true
  
  cache:
    type: redis
    redis:
      time-to-live: 3600000
  
  data:
    redis:
      host: localhost
      port: 6379
      password: ${REDIS_PASSWORD:}
      timeout: 60000
  
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: ${JWT_ISSUER_URI:http://localhost:8080}

server:
  port: 8080
  compression:
    enabled: true
  error:
    include-message: always
    include-binding-errors: always

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
      base-path: /actuator
  endpoint:
    health:
      show-details: when-authorized
      probes:
        enabled: true
  metrics:
    export:
      prometheus:
        enabled: true
  health:
    livenessstate:
      enabled: true
    readinessstate:
      enabled: true

logging:
  level:
    root: INFO
    com.warehouse.employeemanagement: DEBUG
    org.springframework.security: DEBUG
    org.hibernate.SQL: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
  file:
    name: logs/application.log
    max-size: 10MB
    max-history: 30

app:
  jwt:
    secret: ${JWT_SECRET:your-256-bit-secret-key-change-in-production}
    expiration: 86400000
  security:
    auth-mode: ${AUTH_MODE:jwt}
  cors:
    allowed-origins: ${CORS_ORIGINS:http://localhost:3000,http://localhost:4200}
    allowed-methods: GET,POST,PUT,PATCH,DELETE,OPTIONS
    allowed-headers: "*"
    allow-credentials: true
```

```sql
-- db/migration/V1__initial_schema.sql
-- Flyway Migration Script

-- Create extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Create base tables
CREATE TABLE IF NOT EXISTS employees (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    badge_id VARCHAR(50) UNIQUE NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    phone VARCHAR(20),
    role VARCHAR(50) NOT NULL,
    department VARCHAR(100),
    shift_group VARCHAR(50),
    hire_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    version INTEGER NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_employees_badge_id ON employees(badge_id);
CREATE INDEX idx_employees_status ON employees(status);
CREATE INDEX idx_employees_department ON employees(department);

COMMENT ON TABLE employees IS 'Master employee data table';
COMMENT ON COLUMN employees.badge_id IS 'Unique employee badge identifier';
COMMENT ON COLUMN employees.status IS 'Employee status: ACTIVE, INACTIVE, TERMINATED';
```

```java
// Config: OpenAPI Configuration
package com.warehouse.employeemanagement.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {
    
    @Value("${app.api.version:1.0.0}")
    private String apiVersion;
    
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Warehouse Employee Management API")
                .version(apiVersion)
                .description("Comprehensive API for managing warehouse employee lifecycle, attendance, scheduling, safety, and compliance")
                .contact(new Contact()
                    .name("API Support")
                    .email("api-support@warehouse.com")
                    .url("https://warehouse.com/support"))
                .license(new License()
                    .name("Apache 2.0")
                    .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
            .servers(List.of(
                new Server().url("http://localhost:8080").description("Development Server"),
                new Server().url("https://api-staging.warehouse.com").description("Staging Server"),
                new Server().url("https://api.warehouse.com").description("Production Server")))
            .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
            .components(new io.swagger.v3.oas.models.Components()
                .addSecuritySchemes("bearerAuth", new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
                    .description("JWT authentication token")));
    }
}
```

---

## EPIC E02: EMPLOYEE MASTER DATA (CRUD)

### Section: Domain Model - Employee Entity

**Description:**
The Employee entity represents the core domain model for warehouse employees, containing all essential employee information including personal details, role, department, and employment status.

**Design Specification:**

- **Entity Name**: Employee
- **Table Name**: employees
- **Primary Key**: UUID
- **Unique Constraints**: badgeId, email
- **Soft Delete**: Supported via 'deleted' flag
- **Auditing**: Created/Updated timestamps and user tracking
- **Optimistic Locking**: Version field

**Fields:**
- id (UUID): Primary key
- badgeId (String): Unique employee badge identifier
- firstName (String): Employee first name
- lastName (String): Employee last name
- email (String): Employee email address
- phone (String): Contact phone number
- role (Role enum): Employee role (ADMIN, HR, SUPERVISOR, WORKER)
- department (String): Department assignment
- shiftGroup (String): Shift group assignment
- hireDate (LocalDate): Date of hire
- status (EmployeeStatus enum): Employment status
- createdAt, updatedAt: Audit timestamps
- createdBy, updatedBy: Audit user tracking
- version: Optimistic locking
- deleted: Soft delete flag

**Sample Implementation:**

```java
// Domain: Employee Entity
package com.warehouse.employeemanagement.employee.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "employees", indexes = {
    @Index(name = "idx_employees_badge_id", columnList = "badge_id"),
    @Index(name = "idx_employees_status", columnList = "status"),
    @Index(name = "idx_employees_department", columnList = "department")
})
@EntityListeners(AuditingEntityListener.class)
@SQLDelete(sql = "UPDATE employees SET deleted = true WHERE id = ? AND version = ?")
@Where(clause = "deleted = false")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "badge_id", unique = true, nullable = false, length = 50)
    @NotBlank(message = "Badge ID is required")
    @Size(max = 50, message = "Badge ID must not exceed 50 characters")
    private String badgeId;
    
    @Column(name = "first_name", nullable = false, length = 100)
    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name must not exceed 100 characters")
    private String firstName;
    
    @Column(name = "last_name", nullable = false, length = 100)
    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name must not exceed 100 characters")
    private String lastName;
    
    @Column(unique = true, nullable = false)
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;
    
    @Column(length = 20)
    @Pattern(regexp = "^[+]?[0-9]{10,20}$", message = "Phone number must be valid")
    private String phone;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    @NotNull(message = "Role is required")
    private Role role;
    
    @Column(length = 100)
    private String department;
    
    @Column(name = "shift_group", length = 50)
    private String shiftGroup;
    
    @Column(name = "hire_date", nullable = false)
    @NotNull(message = "Hire date is required")
    @PastOrPresent(message = "Hire date cannot be in the future")
    private LocalDate hireDate;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @NotNull(message = "Status is required")
    @Builder.Default
    private EmployeeStatus status = EmployeeStatus.ACTIVE;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @CreatedBy
    @Column(name = "created_by", length = 100)
    private String createdBy;
    
    @LastModifiedBy
    @Column(name = "updated_by", length = 100)
    private String updatedBy;
    
    @Version
    @Column(nullable = false)
    @Builder.Default
    private Integer version = 0;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean deleted = false;
    
    // Helper methods
    public String getFullName() {
        return firstName + " " + lastName;
    }
    
    public boolean isActive() {
        return status == EmployeeStatus.ACTIVE;
    }
    
    public void terminate() {
        this.status = EmployeeStatus.TERMINATED;
    }
    
    public void activate() {
        this.status = EmployeeStatus.ACTIVE;
    }
}
```

```java
// Domain: Role Enum
package com.warehouse.employeemanagement.employee.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Role {
    ADMIN("Administrator", "Full system access"),
    HR("Human Resources", "Employee management and reporting"),
    SUPERVISOR("Supervisor", "Team management and scheduling"),
    WORKER("Worker", "Basic employee access");
    
    private final String displayName;
    private final String description;
    
    public boolean hasAdminPrivileges() {
        return this == ADMIN;
    }
    
    public boolean canManageEmployees() {
        return this == ADMIN || this == HR;
    }
    
    public boolean canManageTeam() {
        return this == ADMIN || this == HR || this == SUPERVISOR;
    }
}
```

```java
// Domain: EmployeeStatus Enum
package com.warehouse.employeemanagement.employee.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EmployeeStatus {
    ACTIVE("Active", "Currently employed and working"),
    INACTIVE("Inactive", "Temporarily not working"),
    ON_LEAVE("On Leave", "Currently on approved leave"),
    TERMINATED("Terminated", "Employment ended");
    
    private final String displayName;
    private final String description;
    
    public boolean canClockIn() {
        return this == ACTIVE;
    }
    
    public boolean canBeScheduled() {
        return this == ACTIVE || this == ON_LEAVE;
    }
}
```

### Section: Data Transfer Objects (DTOs)

**Description:**
DTOs provide a clean separation between the domain model and API layer, allowing for controlled data exposure and validation.

**Sample Implementation:**

```java
// DTO: Employee Request
package com.warehouse.employeemanagement.employee.dto;

import com.warehouse.employeemanagement.employee.domain.EmployeeStatus;
import com.warehouse.employeemanagement.employee.domain.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Employee creation/update request")
public class EmployeeRequest {
    
    @Schema(description = "Unique employee badge identifier", example = "EMP001", required = true)
    @NotBlank(message = "Badge ID is required")
    @Size(max = 50, message = "Badge ID must not exceed 50 characters")
    private String badgeId;
    
    @Schema(description = "Employee first name", example = "John", required = true)
    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name must not exceed 100 characters")
    private String firstName;
    
    @Schema(description = "Employee last name", example = "Doe", required = true)
    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name must not exceed 100 characters")
    private String lastName;
    
    @Schema(description = "Employee email address", example = "john.doe@warehouse.com", required = true)
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;
    
    @Schema(description = "Contact phone number", example = "+1234567890")
    @Pattern(regexp = "^[+]?[0-9]{10,20}$", message = "Phone number must be valid")
    private String phone;
    
    @Schema(description = "Employee role", example = "WORKER", required = true)
    @NotNull(message = "Role is required")
    private Role role;
    
    @Schema(description = "Department assignment", example = "Warehouse Operations")
    private String department;
    
    @Schema(description = "Shift group assignment", example = "A")
    private String shiftGroup;
    
    @Schema(description = "Date of hire", example = "2024-01-15", required = true)
    @NotNull(message = "Hire date is required")
    @PastOrPresent(message = "Hire date cannot be in the future")
    private LocalDate hireDate;
    
    @Schema(description = "Employment status", example = "ACTIVE")
    private EmployeeStatus status;
}
```

```java
// DTO: Employee Response
package com.warehouse.employeemanagement.employee.dto;

import com.warehouse.employeemanagement.employee.domain.EmployeeStatus;
import com.warehouse.employeemanagement.employee.domain.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Employee response data")
public class EmployeeResponse {
    
    @Schema(description = "Employee unique identifier", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;
    
    @Schema(description = "Unique employee badge identifier", example = "EMP001")
    private String badgeId;
    
    @Schema(description = "Employee first name", example = "John")
    private String firstName;
    
    @Schema(description = "Employee last name", example = "Doe")
    private String lastName;
    
    @Schema(description = "Employee full name", example = "John Doe")
    private String fullName;
    
    @Schema(description = "Employee email address", example = "john.doe@warehouse.com")
    private String email;
    
    @Schema(description = "Contact phone number", example = "+1234567890")
    private String phone;
    
    @Schema(description = "Employee role", example = "WORKER")
    private Role role;
    
    @Schema(description = "Department assignment", example = "Warehouse Operations")
    private String department;
    
    @Schema(description = "Shift group assignment", example = "A")
    private String shiftGroup;
    
    @Schema(description = "Date of hire", example = "2024-01-15")
    private LocalDate hireDate;
    
    @Schema(description = "Employment status", example = "ACTIVE")
    private EmployeeStatus status;
    
    @Schema(description = "Record creation timestamp")
    private LocalDateTime createdAt;
    
    @Schema(description = "Record last update timestamp")
    private LocalDateTime updatedAt;
    
    @Schema(description = "Record version for optimistic locking")
    private Integer version;
}
```

```java
// DTO: Employee Mapper (MapStruct)
package com.warehouse.employeemanagement.employee.dto;

import com.warehouse.employeemanagement.employee.domain.Employee;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EmployeeMapper {
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    Employee toEntity(EmployeeRequest request);
    
    @Mapping(target = "fullName", expression = "java(employee.getFullName())")
    EmployeeResponse toResponse(Employee employee);
    
    List<EmployeeResponse> toResponseList(List<Employee> employees);
    
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    void updateEntityFromRequest(EmployeeRequest request, @MappingTarget Employee employee);
}
```

### Section: Repository Layer

**Description:**
Repository layer provides data access abstraction using Spring Data JPA with custom query methods for complex filtering and pagination.

**Sample Implementation:**

```java
// Repository: Employee Repository
package com.warehouse.employeemanagement.employee.repository;

import com.warehouse.employeemanagement.employee.domain.Employee;
import com.warehouse.employeemanagement.employee.domain.EmployeeStatus;
import com.warehouse.employeemanagement.employee.domain.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, UUID> {
    
    Optional<Employee> findByBadgeId(String badgeId);
    
    Optional<Employee> findByEmail(String email);
    
    boolean existsByBadgeId(String badgeId);
    
    boolean existsByEmail(String email);
    
    List<Employee> findByStatus(EmployeeStatus status);
    
    List<Employee> findByDepartment(String department);
    
    List<Employee> findByRole(Role role);
    
    Page<Employee> findByStatus(EmployeeStatus status, Pageable pageable);
    
    Page<Employee> findByDepartment(String department, Pageable pageable);
    
    Page<Employee> findByRole(Role role, Pageable pageable);
    
    @Query("SELECT e FROM Employee e WHERE " +
           "(:status IS NULL OR e.status = :status) AND " +
           "(:department IS NULL OR e.department = :department) AND " +
           "(:role IS NULL OR e.role = :role) AND " +
           "(:searchTerm IS NULL OR " +
           "LOWER(e.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(e.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(e.badgeId) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(e.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Employee> findByFilters(
        @Param("status") EmployeeStatus status,
        @Param("department") String department,
        @Param("role") Role role,
        @Param("searchTerm") String searchTerm,
        Pageable pageable
    );
    
    @Query("SELECT COUNT(e) FROM Employee e WHERE e.status = :status")
    long countByStatus(@Param("status") EmployeeStatus status);
    
    @Query("SELECT COUNT(e) FROM Employee e WHERE e.department = :department")
    long countByDepartment(@Param("department") String department);
    
    @Query("SELECT DISTINCT e.department FROM Employee e WHERE e.department IS NOT NULL ORDER BY e.department")
    List<String> findAllDepartments();
    
    @Query("SELECT DISTINCT e.shiftGroup FROM Employee e WHERE e.shiftGroup IS NOT NULL ORDER BY e.shiftGroup")
    List<String> findAllShiftGroups();
}
```

### Section: Service Layer

**Description:**
Service layer implements business logic, validation, and orchestration of repository operations with transaction management.

**Sample Implementation:**

```java
// Service: Employee Service Interface
package com.warehouse.employeemanagement.employee.service;

import com.warehouse.employeemanagement.employee.domain.EmployeeStatus;
import com.warehouse.employeemanagement.employee.domain.Role;
import com.warehouse.employeemanagement.employee.dto.EmployeeRequest;
import com.warehouse.employeemanagement.employee.dto.EmployeeResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface EmployeeService {
    
    EmployeeResponse createEmployee(EmployeeRequest request);
    
    EmployeeResponse getEmployeeById(UUID id);
    
    EmployeeResponse getEmployeeByBadgeId(String badgeId);
    
    Page<EmployeeResponse> getAllEmployees(Pageable pageable);
    
    Page<EmployeeResponse> searchEmployees(
        EmployeeStatus status,
        String department,
        Role role,
        String searchTerm,
        Pageable pageable
    );
    
    EmployeeResponse updateEmployee(UUID id, EmployeeRequest request);
    
    EmployeeResponse patchEmployee(UUID id, EmployeeRequest request);
    
    void deleteEmployee(UUID id);
    
    void terminateEmployee(UUID id);
    
    void activateEmployee(UUID id);
    
    List<String> getAllDepartments();
    
    List<String> getAllShiftGroups();
    
    long countByStatus(EmployeeStatus status);
    
    long countByDepartment(String department);
}
```

```java
// Service: Employee Service Implementation
package com.warehouse.employeemanagement.employee.service;

import com.warehouse.employeemanagement.common.exception.ResourceNotFoundException;
import com.warehouse.employeemanagement.common.exception.BusinessException;
import com.warehouse.employeemanagement.employee.domain.Employee;
import com.warehouse.employeemanagement.employee.domain.EmployeeStatus;
import com.warehouse.employeemanagement.employee.domain.Role;
import com.warehouse.employeemanagement.employee.dto.EmployeeMapper;
import com.warehouse.employeemanagement.employee.dto.EmployeeRequest;
import com.warehouse.employeemanagement.employee.dto.EmployeeResponse;
import com.warehouse.employeemanagement.employee.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class EmployeeServiceImpl implements EmployeeService {
    
    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper;
    
    @Override
    @Transactional
    @CacheEvict(value = "employees", allEntries = true)
    public EmployeeResponse createEmployee(EmployeeRequest request) {
        log.info("Creating new employee with badge ID: {}", request.getBadgeId());
        
        // Validate unique constraints
        if (employeeRepository.existsByBadgeId(request.getBadgeId())) {
            throw new BusinessException("Employee with badge ID " + request.getBadgeId() + " already exists");
        }
        
        if (employeeRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Employee with email " + request.getEmail() + " already exists");
        }
        
        Employee employee = employeeMapper.toEntity(request);
        Employee savedEmployee = employeeRepository.save(employee);
        
        log.info("Successfully created employee with ID: {}", savedEmployee.getId());
        return employeeMapper.toResponse(savedEmployee);
    }
    
    @Override
    @Cacheable(value = "employees", key = "#id")
    public EmployeeResponse getEmployeeById(UUID id) {
        log.debug("Fetching employee by ID: {}", id);
        Employee employee = findEmployeeById(id);
        return employeeMapper.toResponse(employee);
    }
    
    @Override
    @Cacheable(value = "employees", key = "#badgeId")
    public EmployeeResponse getEmployeeByBadgeId(String badgeId) {
        log.debug("Fetching employee by badge ID: {}", badgeId);
        Employee employee = employeeRepository.findByBadgeId(badgeId)
            .orElseThrow(() -> new ResourceNotFoundException("Employee not found with badge ID: " + badgeId));
        return employeeMapper.toResponse(employee);
    }
    
    @Override
    public Page<EmployeeResponse> getAllEmployees(Pageable pageable) {
        log.debug("Fetching all employees with pagination: {}", pageable);
        Page<Employee> employees = employeeRepository.findAll(pageable);
        return employees.map(employeeMapper::toResponse);
    }
    
    @Override
    public Page<EmployeeResponse> searchEmployees(
            EmployeeStatus status,
            String department,
            Role role,
            String searchTerm,
            Pageable pageable) {
        log.debug("Searching employees with filters - status: {}, department: {}, role: {}, searchTerm: {}",
                  status, department, role, searchTerm);
        
        Page<Employee> employees = employeeRepository.findByFilters(
            status, department, role, searchTerm, pageable
        );
        
        return employees.map(employeeMapper::toResponse);
    }
    
    @Override
    @Transactional
    @CacheEvict(value = "employees", key = "#id")
    public EmployeeResponse updateEmployee(UUID id, EmployeeRequest request) {
        log.info("Updating employee with ID: {}", id);
        
        Employee employee = findEmployeeById(id);
        
        // Validate unique constraints if changed
        if (!employee.getBadgeId().equals(request.getBadgeId()) &&
            employeeRepository.existsByBadgeId(request.getBadgeId())) {
            throw new BusinessException("Employee with badge ID " + request.getBadgeId() + " already exists");
        }
        
        if (!employee.getEmail().equals(request.getEmail()) &&
            employeeRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Employee with email " + request.getEmail() + " already exists");
        }
        
        employeeMapper.updateEntityFromRequest(request, employee);
        Employee updatedEmployee = employeeRepository.save(employee);
        
        log.info("Successfully updated employee with ID: {}", id);
        return employeeMapper.toResponse(updatedEmployee);
    }
    
    @Override
    @Transactional
    @CacheEvict(value = "employees", key = "#id")
    public EmployeeResponse patchEmployee(UUID id, EmployeeRequest request) {
        log.info("Patching employee with ID: {}", id);
        return updateEmployee(id, request);
    }
    
    @Override
    @Transactional
    @CacheEvict(value = "employees", key = "#id")
    public void deleteEmployee(UUID id) {
        log.info("Soft deleting employee with ID: {}", id);
        Employee employee = findEmployeeById(id);
        employeeRepository.delete(employee);
        log.info("Successfully deleted employee with ID: {}", id);
    }
    
    @Override
    @Transactional
    @CacheEvict(value = "employees", key = "#id")
    public void terminateEmployee(UUID id) {
        log.info("Terminating employee with ID: {}", id);
        Employee employee = findEmployeeById(id);
        employee.terminate();
        employeeRepository.save(employee);
        log.info("Successfully terminated employee with ID: {}", id);
    }
    
    @Override
    @Transactional
    @CacheEvict(value = "employees", key = "#id")
    public void activateEmployee(UUID id) {
        log.info("Activating employee with ID: {}", id);
        Employee employee = findEmployeeById(id);
        employee.activate();
        employeeRepository.save(employee);
        log.info("Successfully activated employee with ID: {}", id);
    }
    
    @Override
    public List<String> getAllDepartments() {
        return employeeRepository.findAllDepartments();
    }
    
    @Override
    public List<String> getAllShiftGroups() {
        return employeeRepository.findAllShiftGroups();
    }
    
    @Override
    public long countByStatus(EmployeeStatus status) {
        return employeeRepository.countByStatus(status);
    }
    
    @Override
    public long countByDepartment(String department) {
        return employeeRepository.countByDepartment(department);
    }
    
    private Employee findEmployeeById(UUID id) {
        return employeeRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + id));
    }
}
```

### Section: Controller Layer

**Description:**
REST controller exposing employee management endpoints with comprehensive OpenAPI documentation, validation, and error handling.

**Sample Implementation:**

```java
// Controller: Employee Controller
package com.warehouse.employeemanagement.employee.controller;

import com.warehouse.employeemanagement.common.dto.ApiResponse;
import com.warehouse.employeemanagement.employee.domain.EmployeeStatus;
import com.warehouse.employeemanagement.employee.domain.Role;
import com.warehouse.employeemanagement.employee.dto.EmployeeRequest;
import com.warehouse.employeemanagement.employee.dto.EmployeeResponse;
import com.warehouse.employeemanagement.employee.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
@Tag(name = "Employee Management", description = "APIs for managing warehouse employees")
@SecurityRequirement(name = "bearerAuth")
public class EmployeeController {
    
    private final EmployeeService employeeService;
    
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @Operation(
        summary = "Create new employee",
        description = "Creates a new employee record. Requires ADMIN or HR role."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "Employee created successfully",
            content = @Content(schema = @Schema(implementation = EmployeeResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid input data"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "409",
            description = "Employee with badge ID or email already exists"
        )
    })
    public ResponseEntity<ApiResponse<EmployeeResponse>> createEmployee(
            @Valid @RequestBody EmployeeRequest request) {
        EmployeeResponse response = employeeService.createEmployee(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(response, "Employee created successfully"));
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'SUPERVISOR', 'WORKER')")
    @Operation(
        summary = "Get employee by ID",
        description = "Retrieves employee details by unique identifier"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Employee found",
            content = @Content(schema = @Schema(implementation = EmployeeResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Employee not found"
        )
    })
    public ResponseEntity<ApiResponse<EmployeeResponse>> getEmployeeById(
            @Parameter(description = "Employee unique identifier") @PathVariable UUID id) {
        EmployeeResponse response = employeeService.getEmployeeById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping("/badge/{badgeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'SUPERVISOR', 'WORKER')")
    @Operation(
        summary = "Get employee by badge ID",
        description = "Retrieves employee details by badge identifier"
    )
    public ResponseEntity<ApiResponse<EmployeeResponse>> getEmployeeByBadgeId(
            @Parameter(description = "Employee badge identifier") @PathVariable String badgeId) {
        EmployeeResponse response = employeeService.getEmployeeByBadgeId(badgeId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'SUPERVISOR')")
    @Operation(
        summary = "Get all employees",
        description = "Retrieves paginated list of all employees with optional filtering"
    )
    public ResponseEntity<ApiResponse<Page<EmployeeResponse>>> getAllEmployees(
            @Parameter(description = "Filter by employment status") @RequestParam(required = false) EmployeeStatus status,
            @Parameter(description = "Filter by department") @RequestParam(required = false) String department,
            @Parameter(description = "Filter by role") @RequestParam(required = false) Role role,
            @Parameter(description = "Search term for name, badge ID, or email") @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "lastName", direction = Sort.Direction.ASC) Pageable pageable) {
        
        Page<EmployeeResponse> response = employeeService.searchEmployees(
            status, department, role, search, pageable
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @Operation(
        summary = "Update employee",
        description = "Updates all fields of an existing employee. Requires ADMIN or HR role."
    )
    public ResponseEntity<ApiResponse<EmployeeResponse>> updateEmployee(
            @Parameter(description = "Employee unique identifier") @PathVariable UUID id,
            @Valid @RequestBody EmployeeRequest request) {
        EmployeeResponse response = employeeService.updateEmployee(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Employee updated successfully"));
    }
    
    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @Operation(
        summary = "Partially update employee",
        description = "Updates specific fields of an existing employee. Requires ADMIN or HR role."
    )
    public ResponseEntity<ApiResponse<EmployeeResponse>> patchEmployee(
            @Parameter(description = "Employee unique identifier") @PathVariable UUID id,
            @RequestBody EmployeeRequest request) {
        EmployeeResponse response = employeeService.patchEmployee(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Employee updated successfully"));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Delete employee",
        description = "Soft deletes an employee record. Requires ADMIN role."
    )
    public ResponseEntity<ApiResponse<Void>> deleteEmployee(
            @Parameter(description = "Employee unique identifier") @PathVariable UUID id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Employee deleted successfully"));
    }
    
    @PostMapping("/{id}/terminate")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @Operation(
        summary = "Terminate employee",
        description = "Changes employee status to TERMINATED. Requires ADMIN or HR role."
    )
    public ResponseEntity<ApiResponse<Void>> terminateEmployee(
            @Parameter(description = "Employee unique identifier") @PathVariable UUID id) {
        employeeService.terminateEmployee(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Employee terminated successfully"));
    }
    
    @PostMapping("/{id}/activate")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @Operation(
        summary = "Activate employee",
        description = "Changes employee status to ACTIVE. Requires ADMIN or HR role."
    )
    public ResponseEntity<ApiResponse<Void>> activateEmployee(
            @Parameter(description = "Employee unique identifier") @PathVariable UUID id) {
        employeeService.activateEmployee(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Employee activated successfully"));
    }
    
    @GetMapping("/departments")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'SUPERVISOR')")
    @Operation(
        summary = "Get all departments",
        description = "Retrieves list of all unique departments"
    )
    public ResponseEntity<ApiResponse<List<String>>> getAllDepartments() {
        List<String> departments = employeeService.getAllDepartments();
        return ResponseEntity.ok(ApiResponse.success(departments));
    }
    
    @GetMapping("/shift-groups")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'SUPERVISOR')")
    @Operation(
        summary = "Get all shift groups",
        description = "Retrieves list of all unique shift groups"
    )
    public ResponseEntity<ApiResponse<List<String>>> getAllShiftGroups() {
        List<String> shiftGroups = employeeService.getAllShiftGroups();
        return ResponseEntity.ok(ApiResponse.success(shiftGroups));
    }
}
```

---

## EPIC E03: ROLE-BASED ACCESS CONTROL (RBAC)

### Section: Security Configuration

**Description:**
Implement comprehensive Spring Security configuration with JWT authentication, role-based access control, method-level security, and OAuth2 resource server support.

**Design Specification:**

- **Authentication**: JWT tokens with configurable expiration
- **Authorization**: Role-based (ADMIN, HR, SUPERVISOR, WORKER)
- **Method Security**: @PreAuthorize annotations
- **Row-Level Security**: Custom security expressions
- **OAuth2 Support**: Configurable via application properties
- **API Key Support**: Alternative authentication mechanism

**Sample Implementation:**

```java
// Security: Security Configuration
package com.warehouse.employeemanagement.config;

import com.warehouse.employeemanagement.security.jwt.JwtAuthenticationEntryPoint;
import com.warehouse.employeemanagement.security.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
@RequiredArgsConstructor
public class SecurityConfig {
    
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final UserDetailsService userDetailsService;
    
    @Value("${app.cors.allowed-origins}")
    private String[] allowedOrigins;
    
    @Value("${app.cors.allowed-methods}")
    private String[] allowedMethods;
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers(
                    "/api/v1/auth/**",
                    "/actuator/health",
                    "/actuator/health/**",
                    "/actuator/info",
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html"
                ).permitAll()
                
                // Actuator endpoints - restricted to ADMIN
                .requestMatchers("/actuator/**").hasRole("ADMIN")
                
                // Employee endpoints
                .requestMatchers(HttpMethod.POST, "/api/v1/employees").hasAnyRole("ADMIN", "HR")
                .requestMatchers(HttpMethod.PUT, "/api/v1/employees/**").hasAnyRole("ADMIN", "HR")
                .requestMatchers(HttpMethod.PATCH, "/api/v1/employees/**").hasAnyRole("ADMIN", "HR")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/employees/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/v1/employees/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR", "WORKER")
                
                // All other requests require authentication
                .anyRequest().authenticated()
            )
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(allowedOrigins));
        configuration.setAllowedMethods(Arrays.asList(allowedMethods));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
    
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }
    
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
```

```java
// Security: JWT Token Provider
package com.warehouse.employeemanagement.security.jwt;

import com.warehouse.employeemanagement.security.UserPrincipal;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.stream.Collectors;

@Component
@Slf4j
public class JwtTokenProvider {
    
    @Value("${app.jwt.secret}")
    private String jwtSecret;
    
    @Value("${app.jwt.expiration}")
    private long jwtExpiration;
    
    public String generateToken(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);
        
        String roles = userPrincipal.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.joining(","));
        
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        
        return Jwts.builder()
            .setSubject(userPrincipal.getUsername())
            .claim("userId", userPrincipal.getId().toString())
            .claim("roles", roles)
            .claim("badgeId", userPrincipal.getBadgeId())
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(key, SignatureAlgorithm.HS512)
            .compact();
    }
    
    public String getUsernameFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.getSubject();
    }
    
    public boolean validateToken(String token) {
        try {
            getClaimsFromToken(token);
            return true;
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            log.error("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty");
        }
        return false;
    }
    
    private Claims getClaimsFromToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        return Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .getBody();
    }
}
```

```java
// Security: JWT Authentication Filter
package com.warehouse.employeemanagement.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtTokenProvider tokenProvider;
    private final UserDetailsService userDetailsService;
    
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        
        try {
            String jwt = getJwtFromRequest(request);
            
            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
                String username = tokenProvider.getUsernameFromToken(jwt);
                
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                    );
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("Set authentication for user: {}", username);
            }
        } catch (Exception ex) {
            log.error("Could not set user authentication in security context", ex);
        }
        
        filterChain.doFilter(request, response);
    }
    
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
```

```java
// Security: User Principal
package com.warehouse.employeemanagement.security;

import com.warehouse.employeemanagement.employee.domain.Employee;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class UserPrincipal implements UserDetails {
    
    private UUID id;
    private String badgeId;
    private String email;
    private String password;
    private Collection<? extends GrantedAuthority> authorities;
    
    public static UserPrincipal create(Employee employee) {
        Collection<GrantedAuthority> authorities = Collections.singletonList(
            new SimpleGrantedAuthority("ROLE_" + employee.getRole().name())
        );
        
        return new UserPrincipal(
            employee.getId(),
            employee.getBadgeId(),
            employee.getEmail(),
            "", // Password should be fetched from separate user credentials table
            authorities
        );
    }
    
    @Override
    public String getUsername() {
        return email;
    }
    
    @Override
    public String getPassword() {
        return password;
    }
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    
    @Override
    public boolean isEnabled() {
        return true;
    }
}
```

### Section: Row-Level Security

**Description:**
Implement row-level security to restrict supervisors to viewing only their team members' data.

**Sample Implementation:**

```java
// Security: Custom Security Expressions
package com.warehouse.employeemanagement.security;

import com.warehouse.employeemanagement.employee.domain.Employee;
import com.warehouse.employeemanagement.employee.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("employeeSecurity")
@RequiredArgsConstructor
public class EmployeeSecurityExpression {
    
    private final EmployeeRepository employeeRepository;
    
    public boolean canAccessEmployee(Authentication authentication, UUID employeeId) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        
        // ADMIN and HR can access all employees
        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")) ||
            authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_HR"))) {
            return true;
        }
        
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        
        // Users can access their own data
        if (userPrincipal.getId().equals(employeeId)) {
            return true;
        }
        
        // SUPERVISOR can access their team members
        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_SUPERVISOR"))) {
            return isTeamMember(userPrincipal.getId(), employeeId);
        }
        
        return false;
    }
    
    private boolean isTeamMember(UUID supervisorId, UUID employeeId) {
        // Implementation to check if employee is in supervisor's team
        // This would typically involve checking a team assignment table
        Employee supervisor = employeeRepository.findById(supervisorId).orElse(null);
        Employee employee = employeeRepository.findById(employeeId).orElse(null);
        
        if (supervisor == null || employee == null) {
            return false;
        }
        
        // Simple implementation: same department
        return supervisor.getDepartment() != null &&
               supervisor.getDepartment().equals(employee.getDepartment());
    }
}
```

---

## EPIC E04: TIME & ATTENDANCE (CLOCK IN/OUT)

### Section: Domain Model - Attendance Event

**Description:**
Attendance tracking system for recording clock-in/out events with geofence validation, device capture, and automatic hours calculation.

**Design Specification:**

- **Entity Name**: AttendanceEvent
- **Table Name**: attendance_events
- **Event Types**: CLOCK_IN, CLOCK_OUT, BREAK_START, BREAK_END
- **Geofence Validation**: Optional location-based validation
- **Device Tracking**: Capture device information
- **Hours Calculation**: Automatic calculation per shift

**Sample Implementation:**

```java
// Domain: Attendance Event Entity
package com.warehouse.employeemanagement.attendance.domain;

import com.warehouse.employeemanagement.employee.domain.Employee;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "attendance_events", indexes = {
    @Index(name = "idx_attendance_employee_id", columnList = "employee_id"),
    @Index(name = "idx_attendance_event_time", columnList = "event_time"),
    @Index(name = "idx_attendance_event_type", columnList = "event_type")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceEvent {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    @NotNull(message = "Employee is required")
    private Employee employee;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 20)
    @NotNull(message = "Event type is required")
    private EventType eventType;
    
    @Column(name = "event_time", nullable = false)
    @NotNull(message = "Event time is required")
    private LocalDateTime eventTime;
    
    @Column(name = "shift_date", nullable = false)
    private java.time.LocalDate shiftDate;
    
    @Column(name = "latitude", precision = 10, scale = 7)
    private BigDecimal latitude;
    
    @Column(name = "longitude", precision = 10, scale = 7)
    private BigDecimal longitude;
    
    @Column(name = "device_id", length = 100)
    private String deviceId;
    
    @Column(name = "device_type", length = 50)
    private String deviceType;
    
    @Column(name = "ip_address", length = 45)
    private String ipAddress;
    
    @Column(name = "is_geofence_valid")
    @Builder.Default
    private Boolean isGeofenceValid = true;
    
    @Column(name = "notes", length = 500)
    private String notes;
    
    @Column(name = "is_correction")
    @Builder.Default
    private Boolean isCorrection = false;
    
    @Column(name = "corrected_by")
    private UUID correctedBy;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    // Helper methods
    public boolean isClockIn() {
        return eventType == EventType.CLOCK_IN;
    }
    
    public boolean isClockOut() {
        return eventType == EventType.CLOCK_OUT;
    }
    
    public Duration calculateDuration(AttendanceEvent endEvent) {
        if (endEvent == null || endEvent.getEventTime().isBefore(this.eventTime)) {
            return Duration.ZERO;
        }
        return Duration.between(this.eventTime, endEvent.getEventTime());
    }
}
```

```java
// Domain: Event Type Enum
package com.warehouse.employeemanagement.attendance.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EventType {
    CLOCK_IN("Clock In", "Employee clocked in"),
    CLOCK_OUT("Clock Out", "Employee clocked out"),
    BREAK_START("Break Start", "Employee started break"),
    BREAK_END("Break End", "Employee ended break");
    
    private final String displayName;
    private final String description;
    
    public boolean isPairWith(EventType other) {
        return (this == CLOCK_IN && other == CLOCK_OUT) ||
               (this == BREAK_START && other == BREAK_END);
    }
}
```

```sql
-- db/migration/V2__attendance_schema.sql

CREATE TABLE IF NOT EXISTS attendance_events (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    employee_id UUID NOT NULL REFERENCES employees(id),
    event_type VARCHAR(20) NOT NULL,
    event_time TIMESTAMP NOT NULL,
    shift_date DATE NOT NULL,
    latitude DECIMAL(10, 7),
    longitude DECIMAL(10, 7),
    device_id VARCHAR(100),
    device_type VARCHAR(50),
    ip_address VARCHAR(45),
    is_geofence_valid BOOLEAN DEFAULT TRUE,
    notes VARCHAR(500),
    is_correction BOOLEAN DEFAULT FALSE,
    corrected_by UUID,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_attendance_employee_id ON attendance_events(employee_id);
CREATE INDEX idx_attendance_event_time ON attendance_events(event_time);
CREATE INDEX idx_attendance_event_type ON attendance_events(event_type);
CREATE INDEX idx_attendance_shift_date ON attendance_events(shift_date);

CREATE TABLE IF NOT EXISTS attendance_corrections (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    employee_id UUID NOT NULL REFERENCES employees(id),
    original_event_id UUID REFERENCES attendance_events(id),
    requested_event_type VARCHAR(20) NOT NULL,
    requested_event_time TIMESTAMP NOT NULL,
    reason VARCHAR(1000) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    requested_by UUID NOT NULL,
    requested_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reviewed_by UUID,
    reviewed_at TIMESTAMP,
    review_notes VARCHAR(1000),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_corrections_employee_id ON attendance_corrections(employee_id);
CREATE INDEX idx_corrections_status ON attendance_corrections(status);

COMMENT ON TABLE attendance_events IS 'Employee clock in/out and break events';
COMMENT ON TABLE attendance_corrections IS 'Requests for attendance event corrections';
```

### Section: Service Layer - Attendance Service

**Description:**
Business logic for attendance tracking including validation, hours calculation, and correction workflow.

**Sample Implementation:**

```java
// Service: Attendance Service Implementation
package com.warehouse.employeemanagement.attendance.service;

import com.warehouse.employeemanagement.attendance.domain.AttendanceEvent;
import com.warehouse.employeemanagement.attendance.domain.EventType;
import com.warehouse.employeemanagement.attendance.dto.ClockInRequest;
import com.warehouse.employeemanagement.attendance.dto.ClockOutRequest;
import com.warehouse.employeemanagement.attendance.dto.AttendanceResponse;
import com.warehouse.employeemanagement.attendance.dto.AttendanceMapper;
import com.warehouse.employeemanagement.attendance.repository.AttendanceEventRepository;
import com.warehouse.employeemanagement.common.exception.BusinessException;
import com.warehouse.employeemanagement.employee.domain.Employee;
import com.warehouse.employeemanagement.employee.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AttendanceServiceImpl implements AttendanceService {
    
    private final AttendanceEventRepository attendanceEventRepository;
    private final EmployeeRepository employeeRepository;
    private final AttendanceMapper attendanceMapper;
    private final AttendanceCalculationService calculationService;
    
    @Override
    @Transactional
    public AttendanceResponse clockIn(ClockInRequest request) {
        log.info("Processing clock-in for employee: {}", request.getEmployeeId());
        
        Employee employee = findEmployeeById(request.getEmployeeId());
        
        // Validate employee can clock in
        if (!employee.isActive()) {
            throw new BusinessException("Employee is not active and cannot clock in");
        }
        
        // Check for existing clock-in without clock-out
        Optional<AttendanceEvent> lastEvent = attendanceEventRepository
            .findLastEventByEmployeeId(employee.getId());
        
        if (lastEvent.isPresent() && lastEvent.get().isClockIn()) {
            throw new BusinessException("Employee already clocked in. Please clock out first.");
        }
        
        // Validate geofence if coordinates provided
        boolean isGeofenceValid = true;
        if (request.getLatitude() != null && request.getLongitude() != null) {
            isGeofenceValid = validateGeofence(request.getLatitude(), request.getLongitude());
        }
        
        AttendanceEvent event = AttendanceEvent.builder()
            .employee(employee)
            .eventType(EventType.CLOCK_IN)
            .eventTime(LocalDateTime.now())
            .shiftDate(LocalDate.now())
            .latitude(request.getLatitude())
            .longitude(request.getLongitude())
            .deviceId(request.getDeviceId())
            .deviceType(request.getDeviceType())
            .ipAddress(request.getIpAddress())
            .isGeofenceValid(isGeofenceValid)
            .notes(request.getNotes())
            .build();
        
        AttendanceEvent savedEvent = attendanceEventRepository.save(event);
        log.info("Clock-in successful for employee: {}", employee.getBadgeId());
        
        return attendanceMapper.toResponse(savedEvent);
    }
    
    @Override
    @Transactional
    public AttendanceResponse clockOut(ClockOutRequest request) {
        log.info("Processing clock-out for employee: {}", request.getEmployeeId());
        
        Employee employee = findEmployeeById(request.getEmployeeId());
        
        // Find matching clock-in event
        Optional<AttendanceEvent> clockInEvent = attendanceEventRepository
            .findLastClockInByEmployeeId(employee.getId());
        
        if (clockInEvent.isEmpty()) {
            throw new BusinessException("No matching clock-in found. Please clock in first.");
        }
        
        // Validate geofence if coordinates provided
        boolean isGeofenceValid = true;
        if (request.getLatitude() != null && request.getLongitude() != null) {
            isGeofenceValid = validateGeofence(request.getLatitude(), request.getLongitude());
        }
        
        AttendanceEvent event = AttendanceEvent.builder()
            .employee(employee)
            .eventType(EventType.CLOCK_OUT)
            .eventTime(LocalDateTime.now())
            .shiftDate(clockInEvent.get().getShiftDate())
            .latitude(request.getLatitude())
            .longitude(request.getLongitude())
            .deviceId(request.getDeviceId())
            .deviceType(request.getDeviceType())
            .ipAddress(request.getIpAddress())
            .isGeofenceValid(isGeofenceValid)
            .notes(request.getNotes())
            .build();
        
        AttendanceEvent savedEvent = attendanceEventRepository.save(event);
        
        // Calculate hours worked
        Duration hoursWorked = clockInEvent.get().calculateDuration(savedEvent);
        log.info("Clock-out successful for employee: {}. Hours worked: {}",
                 employee.getBadgeId(), hoursWorked.toHours());
        
        return attendanceMapper.toResponse(savedEvent);
    }
    
    @Override
    public List<AttendanceResponse> getEmployeeAttendance(
            UUID employeeId,
            LocalDate startDate,
            LocalDate endDate) {
        log.debug("Fetching attendance for employee: {} from {} to {}",
                  employeeId, startDate, endDate);
        
        List<AttendanceEvent> events = attendanceEventRepository
            .findByEmployeeIdAndShiftDateBetween(employeeId, startDate, endDate);
        
        return attendanceMapper.toResponseList(events);
    }
    
    @Override
    public Duration calculateHoursWorked(UUID employeeId, LocalDate date) {
        List<AttendanceEvent> events = attendanceEventRepository
            .findByEmployeeIdAndShiftDate(employeeId, date);
        
        return calculationService.calculateTotalHours(events);
    }
    
    private Employee findEmployeeById(UUID id) {
        return employeeRepository.findById(id)
            .orElseThrow(() -> new BusinessException("Employee not found with ID: " + id));
    }
    
    private boolean validateGeofence(java.math.BigDecimal latitude, java.math.BigDecimal longitude) {
        // Implementation for geofence validation
        // This would typically check if coordinates are within allowed warehouse locations
        // For now, return true as a placeholder
        return true;
    }
}
```

```java
// Service: Attendance Calculation Service
package com.warehouse.employeemanagement.attendance.service;

import com.warehouse.employeemanagement.attendance.domain.AttendanceEvent;
import com.warehouse.employeemanagement.attendance.domain.EventType;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
public class AttendanceCalculationService {
    
    public Duration calculateTotalHours(List<AttendanceEvent> events) {
        Duration totalDuration = Duration.ZERO;
        AttendanceEvent lastClockIn = null;
        
        for (AttendanceEvent event : events) {
            if (event.getEventType() == EventType.CLOCK_IN) {
                lastClockIn = event;
            } else if (event.getEventType() == EventType.CLOCK_OUT && lastClockIn != null) {
                Duration duration = lastClockIn.calculateDuration(event);
                totalDuration = totalDuration.plus(duration);
                lastClockIn = null;
            }
        }
        
        return totalDuration;
    }
    
    public double calculateOvertimeHours(Duration totalHours, double regularHoursPerDay) {
        double totalHoursDecimal = totalHours.toMinutes() / 60.0;
        return Math.max(0, totalHoursDecimal - regularHoursPerDay);
    }
}
```

---

## EPIC E05: SHIFT & SCHEDULE MANAGEMENT

### Section: Domain Model - Shift Management

**Description:**
Comprehensive shift scheduling system with templates, assignments, conflict detection, and rotation support.

**Design Specification:**

- **Shift Templates**: Reusable shift definitions
- **Shift Assignments**: Employee-specific shift assignments
- **Conflict Detection**: Automatic detection of scheduling conflicts
- **Rotation Support**: Recurring shift patterns
- **Blackout Dates**: Dates when scheduling is not allowed

**Sample Implementation:**

```java
// Domain: Shift Template Entity
package com.warehouse.employeemanagement.scheduling.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "shift_templates")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShiftTemplate {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(nullable = false, length = 100)
    @NotBlank(message = "Shift name is required")
    private String name;
    
    @Column(length = 500)
    private String description;
    
    @Column(name = "start_time", nullable = false)
    @NotNull(message = "Start time is required")
    private LocalTime startTime;
    
    @Column(name = "end_time", nullable = false)
    @NotNull(message = "End time is required")
    private LocalTime endTime;
    
    @Column(name = "duration_hours", nullable = false)
    private Double durationHours;
    
    @Column(name = "is_overnight")
    @Builder.Default
    private Boolean isOvernight = false;
    
    @Column(name = "shift_code", unique = true, length = 20)
    private String shiftCode;
    
    @Column(name = "department", length = 100)
    private String department;
    
    @Column(name = "required_certifications", length = 500)
    private String requiredCertifications;
    
    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @CreatedBy
    @Column(name = "created_by")
    private String createdBy;
    
    @LastModifiedBy
    @Column(name = "updated_by")
    private String updatedBy;
    
    // Helper methods
    public boolean isOvernightShift() {
        return endTime.isBefore(startTime);
    }
    
    public void calculateDuration() {
        if (isOvernightShift()) {
            this.durationHours = (24.0 - startTime.getHour() + endTime.getHour()) +
                                (endTime.getMinute() - startTime.getMinute()) / 60.0;
        } else {
            this.durationHours = (endTime.getHour() - startTime.getHour()) +
                                (endTime.getMinute() - startTime.getMinute()) / 60.0;
        }
    }
}
```

```java
// Domain: Shift Assignment Entity
package com.warehouse.employeemanagement.scheduling.domain;

import com.warehouse.employeemanagement.employee.domain.Employee;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "shift_assignments", indexes = {
    @Index(name = "idx_shift_assignments_employee", columnList = "employee_id"),
    @Index(name = "idx_shift_assignments_date", columnList = "shift_date"),
    @Index(name = "idx_shift_assignments_status", columnList = "status")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShiftAssignment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    @NotNull(message = "Employee is required")
    private Employee employee;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shift_template_id", nullable = false)
    @NotNull(message = "Shift template is required")
    private ShiftTemplate shiftTemplate;
    
    @Column(name = "shift_date", nullable = false)
    @NotNull(message = "Shift date is required")
    private LocalDate shiftDate;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ShiftStatus status = ShiftStatus.SCHEDULED;
    
    @Column(name = "notes", length = 1000)
    private String notes;
    
    @Column(name = "is_overtime")
    @Builder.Default
    private Boolean isOvertime = false;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @CreatedBy
    @Column(name = "created_by")
    private String createdBy;
    
    @LastModifiedBy
    @Column(name = "updated_by")
    private String updatedBy;
    
    @Version
    private Integer version;
    
    // Helper methods
    public boolean isScheduled() {
        return status == ShiftStatus.SCHEDULED;
    }
    
    public boolean isCompleted() {
        return status == ShiftStatus.COMPLETED;
    }
    
    public void complete() {
        this.status = ShiftStatus.COMPLETED;
    }
    
    public void cancel() {
        this.status = ShiftStatus.CANCELLED;
    }
}
```

```java
// Domain: Shift Status Enum
package com.warehouse.employeemanagement.scheduling.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ShiftStatus {
    SCHEDULED("Scheduled", "Shift is scheduled"),
    IN_PROGRESS("In Progress", "Shift is currently in progress"),
    COMPLETED("Completed", "Shift has been completed"),
    CANCELLED("Cancelled", "Shift has been cancelled"),
    NO_SHOW("No Show", "Employee did not show up for shift");
    
    private final String displayName;
    private final String description;
}
```

### Section: Service Layer - Conflict Detection

**Description:**
Intelligent conflict detection service to prevent double-booking and ensure scheduling compliance.

**Sample Implementation:**

```java
// Service: Conflict Detection Service
package com.warehouse.employeemanagement.scheduling.service;

import com.warehouse.employeemanagement.scheduling.domain.ShiftAssignment;
import com.warehouse.employeemanagement.scheduling.domain.ShiftTemplate;
import com.warehouse.employeemanagement.scheduling.repository.ShiftAssignmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConflictDetectionService {
    
    private final ShiftAssignmentRepository shiftAssignmentRepository;
    
    public List<String> detectConflicts(UUID employeeId, LocalDate shiftDate, ShiftTemplate newShift) {
        List<String> conflicts = new ArrayList<>();
        
        // Get existing assignments for the employee on the same date
        List<ShiftAssignment> existingAssignments = shiftAssignmentRepository
            .findByEmployeeIdAndShiftDate(employeeId, shiftDate);
        
        for (ShiftAssignment existing : existingAssignments) {
            if (hasTimeOverlap(existing.getShiftTemplate(), newShift)) {
                conflicts.add(String.format(
                    "Shift overlaps with existing shift '%s' on %s",
                    existing.getShiftTemplate().getName(),
                    shiftDate
                ));
            }
        }
        
        // Check for consecutive shifts with insufficient rest period
        LocalDate previousDay = shiftDate.minusDays(1);
        List<ShiftAssignment> previousDayAssignments = shiftAssignmentRepository
            .findByEmployeeIdAndShiftDate(employeeId, previousDay);
        
        for (ShiftAssignment previous : previousDayAssignments) {
            if (!hasAdequateRestPeriod(previous.getShiftTemplate(), newShift)) {
                conflicts.add(String.format(
                    "Insufficient rest period between shifts. Previous shift ended at %s",
                    previous.getShiftTemplate().getEndTime()
                ));
            }
        }
        
        return conflicts;
    }
    
    private boolean hasTimeOverlap(ShiftTemplate shift1, ShiftTemplate shift2) {
        LocalTime start1 = shift1.getStartTime();
        LocalTime end1 = shift1.getEndTime();
        LocalTime start2 = shift2.getStartTime();
        LocalTime end2 = shift2.getEndTime();
        
        // Handle overnight shifts
        if (shift1.isOvernightShift() || shift2.isOvernightShift()) {
            return true; // Simplified - would need more complex logic for overnight shifts
        }
        
        return !(end1.isBefore(start2) || end2.isBefore(start1));
    }
    
    private boolean hasAdequateRestPeriod(ShiftTemplate previousShift, ShiftTemplate newShift) {
        // Minimum 8 hours rest period required
        LocalTime previousEnd = previousShift.getEndTime();
        LocalTime newStart = newShift.getStartTime();
        
        // Calculate hours between shifts
        int hoursBetween = newStart.getHour() - previousEnd.getHour();
        if (hoursBetween < 0) {
            hoursBetween += 24;
        }
        
        return hoursBetween >= 8;
    }
    
    public boolean canAssignShift(UUID employeeId, LocalDate shiftDate, ShiftTemplate shift) {
        List<String> conflicts = detectConflicts(employeeId, shiftDate, shift);
        return conflicts.isEmpty();
    }
}
```

---

## CROSS-CUTTING CONCERNS

### Section: Exception Handling

**Description:**
Centralized exception handling with consistent error responses.

**Sample Implementation:**

```java
// Common: Global Exception Handler
package com.warehouse.employeemanagement.common.exception;

import com.warehouse.employeemanagement.common.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException ex, WebRequest request) {
        log.error("Resource not found: {}", ex.getMessage());
        
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.NOT_FOUND.value())
            .error("Not Found")
            .message(ex.getMessage())
            .path(request.getDescription(false).replace("uri=", ""))
            .build();
        
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }
    
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException ex, WebRequest request) {
        log.error("Business exception: {}", ex.getMessage());
        
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.BAD_REQUEST.value())
            .error("Business Rule Violation")
            .message(ex.getMessage())
            .path(request.getDescription(false).replace("uri=", ""))
            .build();
        
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex, WebRequest request) {
        log.error("Validation failed: {}", ex.getMessage());
        
        Map<String, String> validationErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            validationErrors.put(fieldName, errorMessage);
        });
        
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.BAD_REQUEST.value())
            .error("Validation Failed")
            .message("Input validation failed")
            .path(request.getDescription(false).replace("uri=", ""))
            .validationErrors(validationErrors)
            .build();
        
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(
            AccessDeniedException ex, WebRequest request) {
        log.error("Access denied: {}", ex.getMessage());
        
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.FORBIDDEN.value())
            .error("Forbidden")
            .message("You do not have permission to access this resource")
            .path(request.getDescription(false).replace("uri=", ""))
            .build();
        
        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }
    
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(
            BadCredentialsException ex, WebRequest request) {
        log.error("Authentication failed: {}", ex.getMessage());
        
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.UNAUTHORIZED.value())
            .error("Unauthorized")
            .message("Invalid credentials")
            .path(request.getDescription(false).replace("uri=", ""))
            .build();
        
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(
            Exception ex, WebRequest request) {
        log.error("Unexpected error occurred", ex);
        
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .error("Internal Server Error")
            .message("An unexpected error occurred. Please try again later.")
            .path(request.getDescription(false).replace("uri=", ""))
            .build();
        
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
```

---

## DATABASE SCHEMA

### Section: Complete Database Schema

**Description:**
Comprehensive database schema covering all modules with proper indexing, constraints, and relationships.

**Sample Implementation:**

```sql
-- Complete Database Schema for Warehouse Employee Management System

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Employees Table (Core)
CREATE TABLE employees (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    badge_id VARCHAR(50) UNIQUE NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    phone VARCHAR(20),
    role VARCHAR(50) NOT NULL,
    department VARCHAR(100),
    shift_group VARCHAR(50),
    hire_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    version INTEGER NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_employees_badge_id ON employees(badge_id);
CREATE INDEX idx_employees_status ON employees(status);
CREATE INDEX idx_employees_department ON employees(department);
CREATE INDEX idx_employees_email ON employees(email);

-- Attendance Events
CREATE TABLE attendance_events (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    employee_id UUID NOT NULL REFERENCES employees(id),
    event_type VARCHAR(20) NOT NULL,
    event_time TIMESTAMP NOT NULL,
    shift_date DATE NOT NULL,
    latitude DECIMAL(10, 7),
    longitude DECIMAL(10, 7),
    device_id VARCHAR(100),
    device_type VARCHAR(50),
    ip_address VARCHAR(45),
    is_geofence_valid BOOLEAN DEFAULT TRUE,
    notes VARCHAR(500),
    is_correction BOOLEAN DEFAULT FALSE,
    corrected_by UUID,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_attendance_employee_id ON attendance_events(employee_id);
CREATE INDEX idx_attendance_event_time ON attendance_events(event_time);
CREATE INDEX idx_attendance_shift_date ON attendance_events(shift_date);

-- Shift Templates
CREATE TABLE shift_templates (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    duration_hours DECIMAL(5, 2) NOT NULL,
    is_overnight BOOLEAN DEFAULT FALSE,
    shift_code VARCHAR(20) UNIQUE,
    department VARCHAR(100),
    required_certifications VARCHAR(500),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

-- Shift Assignments
CREATE TABLE shift_assignments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    employee_id UUID NOT NULL REFERENCES employees(id),
    shift_template_id UUID NOT NULL REFERENCES shift_templates(id),
    shift_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED',
    notes VARCHAR(1000),
    is_overtime BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    version INTEGER NOT NULL DEFAULT 0
);

CREATE INDEX idx_shift_assignments_employee ON shift_assignments(employee_id);
CREATE INDEX idx_shift_assignments_date ON shift_assignments(shift_date);
CREATE INDEX idx_shift_assignments_status ON shift_assignments(status);

-- Leave Requests
CREATE TABLE leave_requests (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    employee_id UUID NOT NULL REFERENCES employees(id),
    leave_type VARCHAR(20) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    total_days DECIMAL(5, 2) NOT NULL,
    reason VARCHAR(1000),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    requested_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reviewed_by UUID,
    reviewed_at TIMESTAMP,
    review_notes VARCHAR(1000),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_leave_requests_employee ON leave_requests(employee_id);
CREATE INDEX idx_leave_requests_status ON leave_requests(status);
CREATE INDEX idx_leave_requests_dates ON leave_requests(start_date, end_date);

-- Certifications
CREATE TABLE certifications (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    employee_id UUID NOT NULL REFERENCES employees(id),
    certification_type VARCHAR(100) NOT NULL,
    certification_name VARCHAR(200) NOT NULL,
    issue_date DATE NOT NULL,
    expiry_date DATE NOT NULL,
    issuing_authority VARCHAR(200),
    certificate_number VARCHAR(100),
    document_url VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_certifications_employee ON certifications(employee_id);
CREATE INDEX idx_certifications_expiry ON certifications(expiry_date);
CREATE INDEX idx_certifications_status ON certifications(status);

-- Safety Incidents
CREATE TABLE safety_incidents (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    incident_number VARCHAR(50) UNIQUE NOT NULL,
    incident_date TIMESTAMP NOT NULL,
    location VARCHAR(200) NOT NULL,
    severity VARCHAR(20) NOT NULL,
    incident_type VARCHAR(50) NOT NULL,
    description TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    reported_by UUID NOT NULL REFERENCES employees(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_safety_incidents_date ON safety_incidents(incident_date);
CREATE INDEX idx_safety_incidents_status ON safety_incidents(status);
CREATE INDEX idx_safety_incidents_severity ON safety_incidents(severity);

-- Audit Logs
CREATE TABLE audit_logs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    entity_type VARCHAR(100) NOT NULL,
    entity_id UUID NOT NULL,
    action VARCHAR(50) NOT NULL,
    actor_id UUID,
    actor_name VARCHAR(200),
    before_state JSONB,
    after_state JSONB,
    ip_address VARCHAR(45),
    user_agent VARCHAR(500),
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_audit_logs_entity ON audit_logs(entity_type, entity_id);
CREATE INDEX idx_audit_logs_timestamp ON audit_logs(timestamp);
CREATE INDEX idx_audit_logs_actor ON audit_logs(actor_id);
```

---

## API DOCUMENTATION STANDARDS

### Section: OpenAPI Documentation Guidelines

**Description:**
Standardized approach to API documentation using OpenAPI 3.0 specifications.

**Design Specification:**

- All endpoints must have @Operation annotations
- Request/Response examples required
- Error responses documented
- Security requirements specified
- Parameter descriptions mandatory

---

## CONCLUSION

This comprehensive low-level technical design document provides detailed specifications for implementing the Warehouse Employee Management System using Spring Boot best practices. The document covers:

1. **Complete Architecture**: Layered architecture with clear separation of concerns
2. **Domain Models**: Detailed entity designs with JPA mappings
3. **Service Layer**: Business logic implementation with transaction management
4. **Security**: Comprehensive RBAC with JWT authentication
5. **API Design**: RESTful endpoints with OpenAPI documentation
6. **Database Schema**: Complete schema with proper indexing
7. **Cross-Cutting Concerns**: Exception handling, logging, caching

**Implementation Priority:**
1. E01: Project Scaffolding (Foundation)
2. E02: Employee Master Data (Core)
3. E03: Security & RBAC (Critical)
4. E04: Attendance Tracking (High Priority)
5. E05: Shift Scheduling (High Priority)
6. Remaining epics based on business priority

**Next Steps:**
1. Review and approve technical design
2. Set up development environment
3. Implement core modules (E01-E03)
4. Develop attendance and scheduling modules
5. Integrate external systems
6. Comprehensive testing
7. Deployment and monitoring setup

---

**Document Version**: 1.0
**Last Updated**: 2024-01-28
**Status**: Ready for Implementation
