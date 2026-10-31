# Transactions

## What is this project

A [Spring Boot](https://spring.io/projects/spring-boot) service for recording financial transactions, backed by [PostgreSQL](https://www.postgresql.org/) with schema migrations managed by [Flyway](https://flywaydb.org/).

## Versions

| Tool | Version |
| --- | --- |
| Java | 25 (OpenJDK 25.0.2) |
| Spring Boot | 4.1.2 |
| Gradle | 9.8.2 |
| PostgreSQL | 18 |

## Bootstrap the dev environment

Tools are pinned in [`mise.toml`](mise.toml) and managed by [mise](https://mise.jdx.dev/). Install mise following the official instructions, then install the project tools:

```sh
mise install
```

This installs the Java and Gradle versions declared in `mise.toml`.

[Docker](https://www.docker.com/) and [Docker Compose](https://docs.docker.com/compose/) are required to run the database, the tests (Testcontainers) and the containers.

## Run

Start the database:

```sh
docker compose up -d postgres
```

Start the application:

```sh
./gradlew bootRun
```

The service listens on `http://localhost:8080` and exposes the health endpoint at `http://localhost:8080/actuator/health`.

Configuration is read from the following environment variables (defaults in parentheses):

| Variable | Default |
| --- | --- |
| `DATABASE_HOST` | `localhost` |
| `DATABASE_PORT` | `5432` |
| `DATABASE_NAME` | `transactions` |
| `DATABASE_USER` | `transactions_usr` |
| `DATABASE_PASSWORD` | `local_pass` |

## Test

```sh
./gradlew test
```

Tests use [Testcontainers](https://testcontainers.com/), so a running Docker daemon is required.

## Build

```sh
./gradlew build
```

The executable JAR is generated at `build/libs/`. To build only the JAR:

```sh
./gradlew bootJar
```

## Build the Docker image

The `Dockerfile` performs a multi-stage build and produces a distroless image:

```sh
docker build -t transactions:latest .
```

## Run with Docker

With a PostgreSQL instance reachable from the container:

```sh
docker run --rm -p 8080:8080 \
  -e DATABASE_HOST=<host> \
  -e DATABASE_PORT=5432 \
  -e DATABASE_NAME=transactions \
  -e DATABASE_USER=transactions_usr \
  -e DATABASE_PASSWORD=local_pass \
  transactions:latest
```

## Run with Docker Compose

`docker-compose.yaml` starts PostgreSQL and builds/runs the application image:

```sh
docker compose -f docker-compose.yaml up --build
```
