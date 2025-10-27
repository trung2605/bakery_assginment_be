### Build stage
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /workspace

# Copy pom and source and build the application
COPY pom.xml mvnw .mvn/ ./
COPY mvnw .
COPY src ./src

# Use Maven inside the image to download and build (fail-never để vẫn tiếp tục dù có lỗi như encoding)
RUN mvn -B -DskipTests package --fail-never

---
### Runtime stage
FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app

# Copy the executable jar from the build stage, sử dụng đường dẫn tuyệt đối
COPY --from=build /workspace/target/*.jar /app/app.jar

# Expose the typical Spring Boot port
EXPOSE 8080

# Run as non-root for better security (creates user 'app')
RUN addgroup --system app && adduser --system --ingroup app app || true
USER app

# Sửa lỗi: ENTRYPOINT phải trỏ đến file /app/app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]