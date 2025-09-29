# ---------- Build stage ----------
FROM maven:3.9.9-eclipse-temurin-17 AS builder

WORKDIR /app

# Cache Maven dependencies first
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 mvn -B -e -q -DskipTests dependency:go-offline

# Copy source and build
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -B -DskipTests clean package

# ---------- Runtime stage ----------
FROM eclipse-temurin:17-jre

ENV JAVA_OPTS=""
ENV SPRING_PROFILES_ACTIVE=dev
ENV SERVER_PORT=8080

WORKDIR /app

# Copy jar from builder stage
COPY --from=builder /app/target/*.jar /app/app.jar

# Expose port configured by SERVER_PORT
EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dserver.port=${SERVER_PORT} -jar /app/app.jar"]
