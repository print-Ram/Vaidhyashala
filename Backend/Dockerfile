# =============================================
# Stage 1: Build the JAR using Maven
# =============================================
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app

# Copy Maven wrapper and pom first (for layer caching)
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Fix execute permission (lost on Windows filesystems)
RUN chmod +x mvnw

# Download dependencies (cached layer)
RUN ./mvnw dependency:go-offline -q

# Copy source and build
COPY src ./src
RUN ./mvnw package -DskipTests -q

# =============================================
# Stage 2: Run with slim JRE image (~90MB)
# =============================================
FROM eclipse-temurin:17-jre-alpine AS runtime

WORKDIR /app

# Create non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring

# Copy JAR from build stage
COPY --from=builder /app/target/*.jar app.jar

# Switch to non-root
USER spring:spring

# Cloud Run injects PORT env var — Spring reads it via server.port
EXPOSE 8080

ENTRYPOINT ["java", "-jar", \
  "-Dspring.profiles.active=cloud-run", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "app.jar"]
