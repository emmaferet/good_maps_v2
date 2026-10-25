# ==============================================================================
# Good Maps Backend - Multi-stage Production Dockerfile
# Stage 1: Build & Package with Maven & Eclipse Temurin 21 JDK
# ==============================================================================
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder

WORKDIR /app

# Cache Maven dependencies by copying pom.xml first
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build production fat-jar
COPY src ./src
RUN mvn clean package -DskipTests -B

# ==============================================================================
# Stage 2: Minimal Runtime with Eclipse Temurin 21 JRE Alpine
# ==============================================================================
FROM eclipse-temurin:21-jre-alpine AS runner

WORKDIR /app

# Create unprivileged application user for maximum security
RUN addgroup -S goodmaps && adduser -S goodmaps -G goodmaps

# Copy compiled fat-JAR from builder stage
COPY --from=builder --chown=goodmaps:goodmaps /app/target/good-maps-backend-*.jar app.jar

USER goodmaps

# Production JVM flags (container memory limits, G1GC, exit on OOM)
ENV JAVA_OPTS="-XX:+UseG1GC -XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError"
ENV SERVER_PORT=8080

EXPOSE 8080

# Healthcheck targeting Actuator health endpoint
HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
