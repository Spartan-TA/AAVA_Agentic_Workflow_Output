# Use OpenJDK 17 on Alpine Linux for a lightweight image
FROM openjdk:17-jdk-alpine

# Create app directory
WORKDIR /app

# Copy the built JAR file into the container
COPY target/ems-1.0.0.jar /app/ems.jar

# Expose the application port
EXPOSE 8080

# Run the Spring Boot application
ENTRYPOINT ["java", "-jar", "/app/ems.jar"]
