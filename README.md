# Spring Boot User Management Application

## Overview
This application implements a complete user management system with authentication, authorization, activity logging, admin panel, notifications, search, and data export features. It is built using Spring Boot and follows best practices for security and modularity.

## Features
1. **User Registration with Email Verification**
2. **User Login with Account Lockout after 5 Failed Attempts**
3. **Password Reset with 24-hour Expiry**
4. **Profile Management with Email Re-verification**
5. **Dashboard with 30-day Activity Log**
6. **Admin Panel with Role-based Access**
7. **Email Notifications with User Preferences**
8. **Search Functionality with Partial Matches**
9. **Mobile Responsiveness (Backend API Support)**
10. **Data Export in CSV Format**

## Structure
- `src/main/java/com/example/app/`
    - `controller/` (REST Controllers)
    - `service/` (Business Logic)
    - `security/` (Security Configurations)
    - `dto/` (Data Transfer Objects)
    - `exception/` (Custom Exceptions)
    - `util/` (Utility Classes)
    - `entity/` (JPA Entities)
    - `repository/` (Spring Data Repositories)

## Getting Started
1. Clone the repository
2. Configure `application.properties` for your environment
3. Build and run with `mvn spring-boot:run`

## API Endpoints
- `/api/auth/register` - Register user
- `/api/auth/login` - Login
- `/api/auth/reset-password` - Password reset
- `/api/user/profile` - Profile management
- `/api/dashboard/activity` - Activity log
- `/api/admin/users` - Admin user management
- `/api/notifications` - Email notifications
- `/api/search` - Search users
- `/api/export` - Data export

## Security
- JWT-based authentication
- Account lockout after failed attempts
- Role-based access control

## Data Export
- CSV format supported via `/api/export`

## License
MIT
