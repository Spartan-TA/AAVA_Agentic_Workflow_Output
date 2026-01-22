FROM openjdk:17-jdk-alpine
VOLUME /tmp
COPY target/warehouse-employee-management.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]