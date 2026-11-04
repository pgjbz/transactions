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

Start the infrastructure (PostgreSQL, Redis and OpenObserve):

```sh
docker compose up -d postgres redis openbserver
```

Start the application:

```sh
./gradlew bootRun
```

The service listens on `http://localhost:8080` and exposes:

| Endpoint | URL |
| --- | --- |
| Swagger UI | `http://localhost:8080/swagger` |
| OpenAPI JSON | `http://localhost:8080/api-docs` |
| Health | `http://localhost:8080/actuator/health` |
| OpenObserve UI | `http://localhost:5080` |

Configuration is read from the following environment variables (defaults in parentheses):

| Variable | Default |
| --- | --- |
| `DATABASE_HOST` | `localhost` |
| `DATABASE_PORT` | `5432` |
| `DATABASE_NAME` | `transactions` |
| `DATABASE_USER` | `transactions_usr` |
| `DATABASE_PASSWORD` | `local_pass` |
| `REDIS_HOST` | `localhost` |
| `REDIS_PORT` | `6379` |
| `OTEL_AUTH_TOKEN` | _empty_ |
| `OTEL_METRICS_URL` | `http://localhost:5080/api/pgjbz/v1/metrics` |
| `OTEL_TRACE_URL` | `http://localhost:5080/api/pgjbz/v1/traces` |
| `OTEL_LOG_URL` | `http://localhost:5080/api/pgjbz/v1/logs` |

## API documentation (Swagger)

The API is documented with [springdoc-openapi](https://springdoc.org/) and exposed through Swagger UI:

- Swagger UI: `http://localhost:8080/swagger`
- OpenAPI specification (JSON): `http://localhost:8080/api-docs`

Use the Swagger UI to browse the available endpoints and try requests directly from the browser.

## Observability with OpenObserve

[OpenObserve](https://openobserve.ai/) runs as the `openbserver` service in [`docker-compose.yaml`](docker-compose.yaml) and receives the application's metrics, traces and logs via OpenTelemetry (OTLP).

Start it with:

```sh
docker compose up -d openbserver
```

Then open the UI in your browser at `http://localhost:5080` and sign in with the local development credentials:

| Field | Value |
| --- | --- |
| Email | `root@example.com` |
| Password | `Complexpass#123` |

The data is organized in the `pgjbz` organization:

- Metrics: `http://localhost:5080/api/pgjbz/v1/metrics`
- Traces: `http://localhost:5080/api/pgjbz/v1/traces`
- Logs: `http://localhost:5080/api/pgjbz/v1/logs` (stream `transactions`)

When running the application outside Docker, export `OTEL_AUTH_TOKEN` so the OTLP exporters can authenticate against the local OpenObserve instance:

```sh
export OTEL_AUTH_TOKEN="Basic cm9vdEBleGFtcGxlLmNvbTpDb21wbGV4cGFzcyMxMjM="
./gradlew bootRun
```

From the OpenObserve UI, use the **Streams** page to explore logs and traces, and **Dashboards** to build panels over the collected metrics.

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

`docker-compose.yaml` starts PostgreSQL, Redis and OpenObserve, then builds/runs the application image:

```sh
docker compose -f docker-compose.yaml up --build
```

Once the stack is up:

- Application: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger`
- OpenAPI JSON: `http://localhost:8080/api-docs`
- OpenObserve UI: `http://localhost:5080` (`root@example.com` / `Complexpass#123`)
