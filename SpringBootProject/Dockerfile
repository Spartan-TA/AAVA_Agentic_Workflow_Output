# Dockerfile for Warehouse Employee Management System
FROM eclipse-temurin:11-jre-alpine
VOLUME /tmp
COPY target/WarehouseEmployeeMgmt-1.0.0.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
