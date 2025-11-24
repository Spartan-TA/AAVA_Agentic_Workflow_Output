# Dockerfile for Warehouse Employee Management System
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY target/warehouse-employee-mgmt-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
