# syntax=docker/dockerfile:1

# --- Build stage ---
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Copy Maven wrapper and pom.xml first for dependency caching
COPY --link pom.xml mvnw ./
COPY --link .mvn .mvn

# Download dependencies (offline mode for better caching)
RUN chmod +x mvnw && ./mvnw dependency:go-offline

# Copy the rest of the source code
COPY --link src ./src

# Build the application (skip tests for faster build)
RUN ./mvnw package -DskipTests

# --- Runtime stage ---
FROM eclipse-temurin:21-jre
WORKDIR /app

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Create a non-root user and group
RUN addgroup --system aiuser && adduser --system --ingroup aiuser aiuser

# Copy the built jar from the build stage
COPY --from=build /app/target/*.jar /app/app.jar

# Create startup script that waits for dependencies
RUN echo '#!/bin/bash\n\
echo "Waiting for discovery service to be ready..."\n\
while ! curl -s http://discovery:8761/actuator/health > /dev/null; do\n\
  echo "Discovery service not ready yet, waiting 5 seconds..."\n\
  sleep 5\n\
done\n\
echo "Discovery service is ready!"\n\
\n\
echo "Waiting for ChromaDB to be ready..."\n\
while ! curl -s http://chromadb:8000/api/v1/heartbeat > /dev/null; do\n\
  echo "ChromaDB not ready yet, waiting 5 seconds..."\n\
  sleep 5\n\
done\n\
echo "ChromaDB is ready!"\n\
\n\
echo "Waiting for Ollama to be ready..."\n\
while ! curl -s http://ollama:11434/api/tags > /dev/null; do\n\
  echo "Ollama not ready yet, waiting 5 seconds..."\n\
  sleep 5\n\
done\n\
echo "Ollama is ready!"\n\
\n\
echo "All dependencies are ready! Starting AI Service..."\n\
exec java -XX:MaxRAMPercentage=80.0 -jar /app/app.jar "$@"' > /start.sh && \
chmod +x /start.sh

USER aiuser

# Expose the port the service runs on
EXPOSE 8083

# Use the startup script
ENTRYPOINT ["/start.sh"]