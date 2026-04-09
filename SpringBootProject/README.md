# Warehouse Employee Management System (EMS)

A production-grade, modular Spring Boot application for managing warehouse employees, attendance, scheduling, assets, safety, payroll, notifications, integrations, and more.

## Features
- Employee CRUD & RBAC (ADMIN, HR, SUPERVISOR, WORKER)
- Attendance (Clock In/Out, Geofencing)
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
- Localization & Multi-Warehouse
- Observability & Monitoring (Prometheus, Zipkin)
- Docker & Kubernetes ready
- CI/CD with GitHub Actions

## Build & Run

### Prerequisites
- Java 17+
- Maven 3.8+
- PostgreSQL

### Build
```bash
mvn clean package
```

### Run (Dev)
```bash
SPRING_PROFILES_ACTIVE=dev mvn spring-boot:run
```

### Run (Prod)
```bash
SPRING_PROFILES_ACTIVE=prod java -jar target/ems-1.0.0.jar
```

### Database Migration
Flyway will auto-run migrations on startup. See `src/main/resources/db/migration/`.

## Docker
```bash
docker build -t warehouse-ems:latest .
docker run -e SPRING_PROFILES_ACTIVE=prod -p 8080:8080 warehouse-ems:latest
```

## Kubernetes
```bash
kubectl apply -f k8s/configmap.yml
kubectl apply -f k8s/deployment.yml
kubectl apply -f k8s/service.yml
```

## API Documentation
- Swagger UI: `/swagger-ui.html`
- OpenAPI: `/v3/api-docs`

## Localization
- English (default)
- Spanish (`Accept-Language: es`)

## CI/CD
- See `.github/workflows/ci.yml` for build/test pipeline

## Observability
- Prometheus metrics: `/actuator/prometheus`
- Zipkin tracing enabled

## License
MIT
