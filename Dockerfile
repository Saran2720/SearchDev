# # Build stage
# FROM maven:3.9.9-eclipse-temurin-21 AS builder
# WORKDIR /app

# # Copy Maven descriptor first to leverage cache
# COPY pom.xml .
# RUN mvn -q -DskipTests dependency:go-offline

# # Copy source and build
# COPY src ./src
# RUN mvn -q -DskipTests clean package

# # Runtime stage
# FROM eclipse-temurin:21-jre
# WORKDIR /app

# # Copy built jar from builder stage
# COPY --from=builder /app/target/*.jar app.jar

# # Expose Spring Boot default port
# EXPOSE 8080

# # Run the application
# ENTRYPOINT ["java", "-jar", "/app/app.jar"]

FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

COPY target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]