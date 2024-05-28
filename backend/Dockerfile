# Use official OpenJDK 19 image as base
FROM amazoncorretto:17-alpine3.17-jdk

# Set the working directory in the container
WORKDIR /usr/app

# Copy the entire project into the container
COPY . .

# Build the project inside the container
RUN ./gradlew clean build

# Expose the port the app runs on
EXPOSE 8080

# Run the JAR file
CMD ["java", "-Xmx2g", "-Xms1g", "-jar", "build/libs/lodh24.backend-0.0.1-SNAPSHOT.jar"]
