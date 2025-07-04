# Use a lightweight OpenJDK 17 base image
FROM openjdk:17-jdk-slim

# Set the working directory inside the container
WORKDIR /app

# Copy the Maven wrapper and pom.xml
# Make sure you have mvnw and .mvn folder in your project root.
# If not, run 'mvn wrapper:wrapper' in your terminal within wishlist-backend
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Copy the source code
COPY src src

# Build the application
# We use -DskipTests to skip tests during the build, which is common for CI/CD
RUN ./mvnw clean package -DskipTests

# Expose the port your Spring Boot app runs on (default is 8080)
EXPOSE 8080

# Command to run the application
# *** IMPORTANT: Replace 'wishlist-backend-0.0.1-SNAPSHOT.jar' with your actual JAR file name ***
# You can find the exact JAR name in your target/ directory after a local build,
# or look at the <artifactId> and <version> tags in your pom.xml.
ENTRYPOINT ["java", "-jar", "target/wishlist-backend-0.0.1-SNAPSHOT.jar"]
