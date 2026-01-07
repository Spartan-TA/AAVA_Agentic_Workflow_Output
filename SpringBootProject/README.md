# SpringBootProject

A production-ready Spring Boot application implementing:

1. User Login with Email and Password (with account locking after 5 failed attempts)
2. Edit User Profile Information (with email verification)
3. Display Personalized Dashboard Metrics (with real-time WebSocket updates)
4. Export User Data to CSV
5. Real-Time Notification Delivery (in-app and email)
6. Password Reset via Email (with token expiration)
7. Delete User Data Permanently (GDPR-compliant)

## Project Structure

- `src/main/java/com/example/auth/entity` - JPA Entities
- `src/main/java/com/example/auth/repository` - Spring Data JPA Repositories
- `src/main/java/com/example/auth/service` - Service Interfaces
- `src/main/java/com/example/auth/service/impl` - Service Implementations
- `src/main/java/com/example/auth/controller` - REST Controllers
- `src/main/java/com/example/config` - Security and WebSocket Configuration
- `src/main/java/com/example/exception` - Global Exception Handling
- `src/main/resources` - Application Properties

## Build & Run

1. **Build the project:**
   ```bash
   mvn clean install
   ```
2. **Run the application:**
   ```bash
   mvn spring-boot:run
   ```
3. **Access H2 Console:**
   - URL: `http://localhost:8080/h2-console`
   - JDBC URL: `jdbc:h2:mem:testdb`

## API Endpoints

- `POST /api/auth/login` - User login
- `POST /api/auth/register` - User registration
- `PUT /api/auth/profile` - Update profile
- `GET /api/auth/profile` - Get profile
- `POST /api/auth/password-reset/initiate` - Initiate password reset
- `POST /api/auth/password-reset/complete` - Complete password reset
- `DELETE /api/auth/delete` - Delete user (GDPR)

## WebSocket

- Endpoint: `/ws`
- Topics: `/topic/dashboard`, `/topic/notifications`

## Testing

- Unit and integration tests can be added under `src/test/java`

## Notes

- Update `application.properties` for mail and security settings.
- Email verification, password reset, and notification delivery require further implementation (see TODOs in code).
