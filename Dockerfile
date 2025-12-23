# Multi-stage Dockerfile for Stake API Service (Java 21, Spring Boot 3.5)
# Build stage
FROM eclipse-temurin:21-jdk-alpine AS build

# Install necessary build dependencies
RUN apk update && apk upgrade --no-cache \
    && apk add --no-cache bash 'libexpat>=2.7.2-r0'

WORKDIR /workspace

# Copy Gradle wrapper and build files first to leverage layer caching
COPY gradlew gradlew.bat /workspace/
COPY gradle /workspace/gradle
COPY build.gradle settings.gradle /workspace/

# Pre-fetch dependencies to warm the Gradle cache
RUN chmod +x ./gradlew \
    && ./gradlew --no-daemon dependencies > /dev/null 2>&1 || true

# Copy the source code
COPY src /workspace/src

# Build the application (skip tests here; tests should run in CI prior to build)
RUN ./gradlew clean bootJar -x test --no-daemon

# Run stage - minimal JRE image
FROM eclipse-temurin:21-jre-alpine

# Install minimal runtime dependencies
RUN apk update && apk upgrade --no-cache \
    && apk add --no-cache 'libexpat>=2.7.2-r0' curl wget

# Create the logs directory with -p flag
RUN mkdir -p /app/logs

# Security hardening: Create non-root user with specific UID/GID matching host permissions
RUN addgroup -g 1000 -S app \
    && adduser -u 1000 -S app -G app

# Set appropriate permissions
RUN chmod -R 755 /app/logs \
    && chown -R app:app /app/logs

WORKDIR /app

# Copy fat jar
COPY --from=build /workspace/build/libs/*.jar /app/app.jar

# Expose HTTP port
EXPOSE 9012

# Volume for logs
VOLUME ["/app/logs"]

# JVM runtime options for container environments
ENV JAVA_TOOL_OPTIONS="-XX:+UseG1GC -XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError -Djava.security.egd=file:/dev/./urandom"

# Drop capabilities: use non-root user
USER app

# Healthcheck (expects actuator health)
HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
  CMD wget -qO- http://127.0.0.1:9012/actuator/health | grep 'UP' || exit 1

# Use exec form to get proper signal handling
ENTRYPOINT ["java","-jar","/app/app.jar"]