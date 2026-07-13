# syntax=docker/dockerfile:1
FROM eclipse-temurin:21.0.8_9-jdk-alpine AS build
WORKDIR /workspace

COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle
RUN chmod +x gradlew

# Warm the Gradle dependency cache in its own layer so source-only
# changes don't force a re-download.
RUN ./gradlew --no-daemon dependencies > /dev/null 2>&1 || true

COPY src ./src
RUN ./gradlew --no-daemon bootJar

FROM eclipse-temurin:21.0.8_9-jre-alpine
WORKDIR /app
RUN addgroup -S planner && adduser -S planner -G planner
COPY --from=build /workspace/build/libs/*-SNAPSHOT.jar app.jar
USER planner:planner

EXPOSE 8080

HEALTHCHECK --interval=10s --timeout=5s --start-period=40s --retries=5 \
  CMD wget -q --spider http://localhost:8080/api/v1/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
