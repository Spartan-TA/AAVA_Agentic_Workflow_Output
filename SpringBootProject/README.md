# User Management Platform

## Overview
A production-ready Spring Boot application for user management, featuring registration, login, profile management, password reset, email verification, MFA, JWT authentication, admin controls, and personalized dashboards.

## Build & Run

### Prerequisites
- Java 17+
- Maven 3.6+
- MySQL/PostgreSQL (or H2 for testing)

### Build
```
mvn clean install
```

### Run
```
mvn spring-boot:run
```

### Test
```
mvn test
```

## Configuration
Edit `src/main/resources/application.properties` for DB, email, JWT, and MFA settings.

## API Documentation

### AuthController
- `POST /api/auth/register` - Register user
- `POST /api/auth/login` - Login
- `POST /api/auth/logout` - Logout
- `GET /api/auth/verify-email` - Email verification
- `POST /api/auth/reset-password` - Password reset
- `POST /api/auth/mfa/enable` - Enable MFA
- `POST /api/auth/mfa/verify` - Verify MFA

### UserController
- `GET /api/user/profile` - Get profile
- `PUT /api/user/profile` - Update profile
- `GET /api/user/dashboard` - Get dashboard data

### AdminController
- `GET /api/admin/users` - List users
- `POST /api/admin/users/{id}/deactivate` - Deactivate user
- `POST /api/admin/users/{id}/activate` - Activate user

### MfaController
- `POST /api/mfa/enable` - Enable MFA
- `POST /api/mfa/verify` - Verify MFA
- `POST /api/mfa/disable` - Disable MFA

### MfaAuthController
- `POST /api/mfa/auth` - MFA validation during login

## Exception Handling
Global exception handler returns structured error responses for all errors.

## Security
JWT-based authentication, Spring Security configuration, MFA support.

## Contact
For support, contact dev@example.com
