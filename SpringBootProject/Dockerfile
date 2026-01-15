# Multi-stage Dockerfile for Spring Boot Warehouse Management System
# Stage 1: Build
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy parent POM and module POMs
COPY pom.xml .
COPY common/pom.xml common/
COPY employee/pom.xml employee/
COPY security/pom.xml security/
COPY attendance/pom.xml attendance/
COPY scheduling/pom.xml scheduling/
COPY leave/pom.xml leave/
COPY training/pom.xml training/
COPY safety/pom.xml safety/
COPY equipment/pom.xml equipment/
COPY performance/pom.xml performance/
COPY payroll/pom.xml payroll/
COPY notifications/pom.xml notifications/
COPY integration/pom.xml integration/
COPY audit/pom.xml audit/
COPY reporting/pom.xml reporting/

# Download dependencies
RUN mvn dependency:go-offline -B

# Copy source code
COPY common/src common/src
COPY employee/src employee/src
COPY security/src security/src
COPY attendance/src attendance/src
COPY scheduling/src scheduling/src
COPY leave/src leave/src
COPY training/src training/src
COPY safety/src safety/src
COPY equipment/src equipment/src
COPY performance/src performance/src
COPY payroll/src payroll/src
COPY notifications/src notifications/src
COPY integration/src integration/src
COPY audit/src audit/src
COPY reporting/src reporting/src

# Build the application
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Create non-root user
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy JAR from build stage
COPY --from=build /app/employee/target/*.jar app.jar

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3   CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]