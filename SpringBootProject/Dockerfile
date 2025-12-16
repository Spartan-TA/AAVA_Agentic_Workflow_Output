# Use official OpenJDK image
FROM eclipse-temurin:17-jre-alpine

# Set working directory
WORKDIR /app

# Copy built jar from target directory
COPY target/warehouse-employee-management-1.0.0.jar app.jar

# Expose port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
