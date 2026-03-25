# Warehouse EMS (Employee Management System)

A production-ready Spring Boot application for comprehensive warehouse employee management, covering 20 functional epics including Employee CRUD, RBAC, Attendance, Shift Management, Leave, Certifications, Safety, Assets, Payroll, Notifications, Integrations, Audit, Reporting, Mobile/PWA, and more.

## Tech Stack
- Java 17+
- Spring Boot 3.x
- Spring Data JPA (PostgreSQL)
- Spring Security (RBAC)
- Flyway (DB migrations)
- OpenAPI/Swagger
- Bean Validation
- Actuator

## Build & Run

### Prerequisites
- Java 17+
- Maven 3.8+
- PostgreSQL (running and accessible)

### Steps
1. **Clone the repository:**
   ```bash
   git clone <repo-url>
   cd SpringBootProject
   ```
2. **Configure Database:**
   Edit `src/main/resources/application.yml` with your PostgreSQL credentials.

3. **Build the project:**
   ```bash
   mvn clean install
   ```
4. **Run the application:**
   ```bash
   mvn spring-boot:run
   ```
   The app will start on [http://localhost:8080](http://localhost:8080)

5. **API Documentation:**
   Visit [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

6. **Actuator Health Check:**
   Visit [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)

## Directory Structure
```
SpringBootProject/
âââ pom.xml
âââ README.md
âââ src/main/java/com/warehouse/ems/
â   âââ WarehouseEmsApplication.java
â   âââ config/
â   âââ controller/
â   âââ dto/
â   âââ entity/
â   âââ exception/
â   âââ repository/
â   âââ security/
â   âââ service/
â   âââ util/
âââ src/main/resources/
    âââ application.yml
    âââ db/migration/
```

## License
MIT
