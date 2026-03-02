# Use Eclipse Temurin for Java 17
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY target/employee-management-1.0.0.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","/app/app.jar"]
