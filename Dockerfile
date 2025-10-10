FROM maven:3.9.9-eclipse-temurin-17

WORKDIR /app

ENV JAVA_OPTS=""
ENV SPRING_PROFILES_ACTIVE=dev
ENV SERVER_PORT=8081
ENV LOG_LEVEL=DEBUG

# Copy pom for Maven context (no prefetch step)
COPY pom.xml .

# Copy source
COPY src ./src

# Expose default dev port
EXPOSE 8081

# Run Spring Boot in dev mode using Maven
# Pass server.port via run arguments and JVM opts via spring-boot.run.jvmArguments
ENTRYPOINT ["sh", "-c", "mvn -B -DskipTests spring-boot:run -Dspring-boot.run.arguments=\"--server.port=${SERVER_PORT}\""]
