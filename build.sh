#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Number of threads to use for Maven builds.
MVN_THREADS="${MVN_THREADS:-5}"

# Example: MVN_ARGS="-DskipTests" ./build.sh
MVN_ARGS="${MVN_ARGS:-}"
(
  cd "${ROOT_DIR}"
  mvn -T "${MVN_THREADS}" -pl '!dtu-pay-E2ETest' -am clean package ${MVN_ARGS}
)
mvn -f "${ROOT_DIR}/dtu-pay-E2ETest/pom.xml" clean package -DskipTests

# Only build images if the Maven build succeeded.
(
  cd "${ROOT_DIR}"
  docker compose build
)
