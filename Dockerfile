# Stage 1: Build the application using Maven
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY pom.xml .
# Download dependencies (caching)
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn package -DskipTests

# Stage 2: Create the runtime container
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/resumeiq-backend-0.0.1-SNAPSHOT.jar app.jar

# Setup volumes and directories
RUN mkdir -p uploads logs
VOLUME /app/uploads
VOLUME /app/logs

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
