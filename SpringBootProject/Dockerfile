# Multi-stage Docker build for Warehouse Employee Management System
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app
COPY . .
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
VOLUME /tmp
COPY --from=build /app/target/employee-management-*.jar app.jar
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=10s --start-period=30s CMD wget --spider -q http://localhost:8080/actuator/health || exit 1
ENTRYPOINT ["java","-jar","/app.jar"]
