# User Management System

A comprehensive Spring Boot application implementing advanced user management features including registration, login, RBAC, notifications, reporting, and more.

## Features
- User Registration with Email Verification
- Secure Login with Account Lockout and CAPTCHA
- Password Reset with Token Expiration
- Profile Management with Audit Trail
- Role-Based Access Control (Admin, Manager, User)
- Data Visualization Dashboard
- Export Reports (PDF, CSV)
- In-App and Email Notifications
- Audit Logging
- Bulk User Import
- Two-Factor Authentication (TOTP, SMS)
- User Search and Filtering
- Scheduled Reports

## Prerequisites
- Java 11+
- Maven 3.6+
- PostgreSQL (or H2 for development)

## Setup
1. Clone the repository
2. Configure `application.yml` for your database and email settings
3. Run `mvn clean install`
4. Start the application: `mvn spring-boot:run`

## Project Structure
- `config/` - Security, WebSocket, and other configurations
- `controller/` - REST controllers
- `dto/` - Data Transfer Objects
- `entity/` - JPA entities
- `exception/` - Exception handling
- `repository/` - Spring Data JPA repositories
- `security/` - JWT and authentication
- `service/` - Business logic
- `util/` - Helper classes

## API Documentation
See [API.md](API.md) for endpoint details.

## Testing
Run all tests with `mvn test`.
