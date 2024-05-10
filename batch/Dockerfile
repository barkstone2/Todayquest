# Use the official OpenJDK image as the base image
FROM amazoncorretto:17

# Set the working directory
WORKDIR /app

# Copy the built JAR file into the image
COPY ./build/libs/batch.jar app.jar

# Run the JAR file
ENTRYPOINT ["java", "-jar", "app.jar"]