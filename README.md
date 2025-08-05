# aegis_backend

## Running with Docker

This project uses Docker to containerize its microservices. Each service has its own Dockerfile and is orchestrated via Docker Compose. The following instructions are specific to this project:

### Requirements
- Docker and Docker Compose installed on your system.
- Uses Eclipse Temurin JDK 21 for building and running Java services (as specified in the Dockerfiles).

### Services and Ports
- **Discovery Service (Eureka):** Exposes port `8761` (http://localhost:8761)
- **API Gateway:** Exposes port `8082` (http://localhost:8082)
- **AI Service:** Exposes port `8083` (http://localhost:8083)

### Build and Run
1. Ensure you are in the project root directory (where the `docker-compose.yaml` file is located).
2. Build and start all services:
   ```sh
   docker compose up --build
   ```
   This will build the images for each service using their respective Dockerfiles and start the containers.

### Configuration
- All services are connected via a custom Docker network `aegis-net`.
- No environment variables are required by default, but you can use a `.env` file for additional configuration if needed (see commented `env_file` lines in the compose file).
- The `Discovery` service must be running before the `ApiGateway` and `AIService` services start. The compose file handles this with `depends_on`.

### Notes
- If you need to customize ports or environment variables, edit the `docker-compose.yaml` file accordingly.
- The build process uses Maven Wrapper (`mvnw`) and skips tests for faster builds.
