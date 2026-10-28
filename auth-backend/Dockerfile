# ─────────────────────────────────────────────────────────────────────────────
# Stage 1 – Build the Spring Boot JAR with Maven
# ─────────────────────────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jdk-alpine AS build

WORKDIR /workspace

# Copy Maven wrapper and POM first for layer caching
COPY .mvn/ .mvn/
COPY mvnw mvnw
COPY pom.xml pom.xml

RUN chmod +x ./mvnw

# Download all dependencies (cached unless pom.xml changes)
RUN ./mvnw dependency:go-offline -B

# Copy source and build
COPY src/ src/
RUN ./mvnw clean package -DskipTests -B

# ─────────────────────────────────────────────────────────────────────────────
# Stage 2 – Minimal JRE runtime image
# ─────────────────────────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Non-root user for security
RUN addgroup -S rims && adduser -S rims -G rims
USER rims

# Copy the fat JAR from the build stage
COPY --from=build /workspace/target/auth-backend-*.jar app.jar

# Expose the application port
EXPOSE 8080

# JVM tuning for containerised environments
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
