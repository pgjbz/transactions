FROM docker.io/gradle:9.8.2-jdk25-alpine AS builder
WORKDIR /app
COPY --chown=gradle:gradle build.gradle.kts .
RUN gradle buildEnvironment --no-daemon
COPY --chown=gradle:gradle ./src ./src
COPY --chown=gradle:gradle settings.gradle.kts .
RUN gradle clean bootJar --no-daemon -x test

FROM gcr.io/distroless/java25-debian13 as RUNNER
WORKDIR /app
COPY --chown=nonroot:nonroot --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=10s --retries=3 --start-period=40s \
    CMD curl -f http://localhost:8080/actuator/health || exit 1
USER nonroot
ENTRYPOINT ["java", "-jar", "/app/app.jar"]