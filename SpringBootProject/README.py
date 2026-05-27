"""
Spring Boot User Login Microservice
===================================

This project implements a secure user login feature using Spring Boot 3.x and Spring Security 6.x, following a layered architecture and JWT-based authentication.

Project Structure:
------------------
com.example.auth
âââ controller
â   âââ AuthController.java
âââ service
â   âââ AuthService.java
âââ repository
â   âââ UserRepository.java
âââ model
â   âââ User.java
â   âââ LoginRequest.java
â   âââ LoginResponse.java
âââ config
â   âââ SecurityConfig.java
â   âââ JwtFilter.java
â   âââ JwtUtil.java
âââ exception
    âââ AuthException.java

Build & Run Instructions:
------------------------
1. Ensure Java 17+ and Maven are installed.
2. Place all Java files in the correct package structure under src/main/java/com/example/auth/.
3. Add dependencies for Spring Boot, Spring Security, JWT, and JPA in pom.xml.
4. Run `mvn clean install` to build.
5. Start with `mvn spring-boot:run`.

API Endpoint:
-------------
POST /api/auth/login
Request: {"username": "user", "password": "pass"}
Response: {"token": "jwt-token", "message": "Login successful"}

Testing:
--------
- Unit tests can be added under src/test/java/com/example/auth/.
- Use JUnit and Mockito for service and controller tests.

Security Notes:
---------------
- Passwords are hashed with BCrypt.
- JWT tokens are used for stateless session management.
- Only /api/auth/login is publicly accessible; all other endpoints require authentication.

"""