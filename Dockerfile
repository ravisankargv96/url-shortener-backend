# Use a lightweight OpenJDK image with Maven preinstalled
FROM eclipse-temurin:17-jdk-jammy

# Set working directory
WORKDIR /app

# Copy the entire backend source code and Maven wrapper into the container
COPY . .

# Make sure the Maven wrapper is executable (especially important on Linux)
RUN chmod +x mvnw

# Build the Spring Boot application (skip tests for faster deploys)
RUN ./mvnw clean package -DskipTests

# Expose the port your Spring Boot app runs on
EXPOSE 8080

# Run the generated JAR file
CMD ["java", "-jar", "target/url-shortener-sb-0.0.1-SNAPSHOT.jar"]
