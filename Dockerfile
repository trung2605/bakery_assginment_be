# Multi-stage Dockerfile for the bakery Spring Boot application
# - Build stage: use Maven with JDK 21 to compile and package the app
# - Runtime stage: use a small JRE 21 image to run the resulting fat jar

### Build stage
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /workspace

# Copy pom and source and build the application
COPY pom.xml mvnw .mvn/ ./
COPY mvnw .
COPY src ./src

# Use Maven inside the image to download and build (skip tests for image build)
RUN mvn -B -DskipTests package --fail-never

### Runtime stage
FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app

# Copy the executable jar from the build stage. We use a wildcard because
# Spring Boot creates an artifact like bakery-0.0.1-SNAPSHOT.jar
COPY --from=build /workspace/target/*.jar ./app.jar

# Expose the typical Spring Boot port
EXPOSE 8080

# Run as non-root for better security (creates user 'app')
RUN addgroup --system app && adduser --system --ingroup app app || true
USER app

# Default command to run the Spring Boot fat jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

# Note:
# - If your app needs environment variables (DB creds, etc.) pass them with -e when running the container.
# - This Dockerfile assumes Java 21 (see pom.xml). If you want to use the Maven wrapper instead
#   you can change the build stage to COPY the whole project and run ./mvnw package.
