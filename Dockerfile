FROM eclipse-temurin:17-jdk-alpine

# Install Maven
RUN apk add --no-cache maven

WORKDIR /app

# Copy pom.xml first for better dependency caching
COPY auth-quiz-service/pom.xml .

# Download dependencies
RUN mvn dependency:go-offline -B

# Copy source code
COPY auth-quiz-service/src ./src

# Build the application
RUN mvn clean package -DskipTests

# Expose port
EXPOSE 8080

# Run the application
CMD ["java", "-jar", "target/auth-quiz-service-1.0.0.jar"]
