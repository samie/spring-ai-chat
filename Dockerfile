FROM openjdk:17 AS build

# Update package lists and Install Maven
RUN microdnf update -y && \
    microdnf install -y maven && \
    microdnf clean all

WORKDIR /usr/src/app

# Copy rest of the dependencies from pom.xml.
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy the source code
COPY . .

# Do the build using production profile
RUN mvn clean install -Pproduction

# Second stage: Lightweight jdk-slim image
FROM openjdk:17-jdk-slim
RUN useradd -m appuser

RUN mkdir /app && \
    chown -R appuser /app

USER appuser

WORKDIR /app

# Copy the native binary from the build stage
COPY --from=build /usr/src/app/target/*.jar /app/app.jar

# Run the application
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
