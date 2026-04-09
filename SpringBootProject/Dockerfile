FROM eclipse-temurin:17-jre-alpine
LABEL maintainer="Warehouse EMS Team"

WORKDIR /app

COPY target/ems-1.0.0.jar app.jar

EXPOSE 8080

ENV JAVA_OPTS=""

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
