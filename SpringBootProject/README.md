# Warehouse Employee Management System

A Spring Boot application for managing warehouse employees, attendance, shifts, leaves, certifications, safety incidents, assets, performance reviews, and authentication with JWT security.

## Features
- Employee CRUD operations
- Attendance tracking (clock-in/clock-out)
- Shift assignment and management
- Leave requests and approval
- Employee certifications
- Safety incident reporting
- Asset management
- Performance reviews
- Secure JWT-based authentication and authorization
- Global exception handling
- Flyway database migrations

## Technologies Used
- Java 17+
- Spring Boot 2.7+
- Spring Security (JWT)
- Spring Data JPA
- MapStruct
- Flyway
- PostgreSQL (or compatible RDBMS)
- Maven

## Project Structure
```
SpringBootProject/
âââ src/main/java/com/warehouse/employee/
â   âââ controller/
â   âââ dto/
â   âââ exception/
â   âââ mapper/
â   âââ model/
â   âââ repository/
â   âââ security/
â   âââ service/
âââ src/main/resources/db/migration/
â   âââ V1__init.sql
â   âââ V2__add_indexes.sql
â   âââ V3__add_user_table.sql
âââ README.md
```

## Getting Started

### Prerequisites
- Java 17 or higher
- Maven
- PostgreSQL (or compatible RDBMS)

### Setup
1. Clone the repository:
   ```sh
   git clone <repo-url>
   cd SpringBootProject
   ```
2. Configure your database in `src/main/resources/application.properties`:
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/warehouse
   spring.datasource.username=youruser
   spring.datasource.password=yourpassword
   spring.jpa.hibernate.ddl-auto=validate
   jwt.secret=your_jwt_secret
   jwt.expiration=86400000
   ```
3. Run Flyway migrations (auto on app start).
4. Build and run the application:
   ```sh
   mvn clean install
   mvn spring-boot:run
   ```

## API Endpoints

- `/api/auth/login` - Authenticate and receive JWT
- `/api/employees` - Employee CRUD
- `/api/attendance` - Attendance endpoints
- `/api/shifts` - Shift assignment endpoints
- `/api/leaves` - Leave requests
- `/api/certifications` - Certification endpoints
- `/api/safety-incidents` - Safety incident endpoints
- `/api/assets` - Asset endpoints
- `/api/performance-reviews` - Performance review endpoints

## Security
- JWT-based authentication
- Secure endpoints except `/api/auth/**`
- Add JWT token as `Authorization: Bearer <token>` header

## Exception Handling
- All errors return a structured `ErrorResponse` JSON

## Contributing
Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

## License
[MIT](LICENSE)
