FROM eclipse-temurin:17-jdk-alpine

# Install Maven
RUN apk add --no-cache maven

WORKDIR /app

# Copy pom.xml first for better dependency caching
COPY pom.xml .

# Download dependencies
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Expose port
EXPOSE 8082

# Run the application
CMD ["java", "-jar", "target/*.jar"]
