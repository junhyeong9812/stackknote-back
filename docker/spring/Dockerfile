# Multi-stage build for Spring Boot application

# Stage 1: Build
FROM gradle:8.7-jdk17 AS build

WORKDIR /app

# Copy gradle files first for better caching
COPY build.gradle settings.gradle ./
COPY gradle ./gradle

# Download dependencies
RUN gradle dependencies --no-daemon

# Copy source code
COPY src ./src

# Build application
RUN gradle build --no-daemon -x test

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Install wget for health check
RUN apk add --no-cache wget

# Create non-root user for security
RUN addgroup -g 1001 -S stacknote && \
    adduser -S stacknote -u 1001 -G stacknote

# Create directories
RUN mkdir -p /app/images /app/logs && \
    chown -R stacknote:stacknote /app

# Copy built jar from build stage
COPY --from=build /app/build/libs/stacknote-api.jar app.jar

# Change ownership to non-root user
RUN chown stacknote:stacknote app.jar

# Switch to non-root user
USER stacknote

# Environment variables
ENV JAVA_OPTS="-Xms512m -Xmx1024m"
ENV SPRING_PROFILES_ACTIVE=prod

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Start application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]