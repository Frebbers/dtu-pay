# DTU-Pay
Group 25

## Requirements
- Docker (with `docker compose`)
- Java 21
- Maven
- `JAVA_HOME` set to your Java 21 installation

## Project layout
- Services live in their respective folders (account, payment, reporting, token, server).
- End-to-end tests live in `dtu-pay-E2ETest`.

## Installation and setup
1. Verify Java and Maven:
```bash
java -version
mvn -version
```
2. Verify Docker:
```bash
docker version
docker compose version
```

## Build and run (recommended)
Builds all services, builds Docker images, and starts the stack:
```bash
./build_and_run.sh
```
What it does:
- `./build.sh` (Maven build + Docker image build)
- `docker compose up -d`
- `docker image prune -f`

Stop the stack with:
```bash
docker compose down
```

## Build only
Build all services and Docker images:
```bash
./build.sh
```
Optional overrides:
- `MVN_THREADS=8 ./build.sh` to change Maven parallelism (default: 5)
- `MVN_ARGS="-DskipTests" ./build.sh` to pass extra Maven args

## Tests
Run E2E tests (builds and runs the stack first):
```bash
./build_and_test.sh
```

Run E2E tests only:
```bash
./test.sh
```
