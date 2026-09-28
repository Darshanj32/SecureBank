# --- Build stage ---
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app
COPY src ./src
RUN mkdir -p out && javac -d out $(find src -name "*.java")

# --- Runtime stage ---
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/out ./out
# Data directory persists inside the container; mount a volume to persist across runs.
VOLUME ["/app/data"]
# -it is required at `docker run` time for interactive console input.
CMD ["java", "-cp", "out", "Main"]
