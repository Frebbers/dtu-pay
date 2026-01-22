#!/bin/bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
LOG_DIR="${ROOT_DIR}/.build-logs"

BUILD_START_EPOCH="$(date +%s)"

print_total_time() {
  local end
  end="$(date +%s)"
  local elapsed=$(( end - BUILD_START_EPOCH ))
  local mins=$(( elapsed / 60 ))
  local secs=$(( elapsed % 60 ))
  echo
  echo "Total time to build and test: ${mins}m ${secs}s"
}

# Max number of concurrent Maven builds.
PARALLEL_DEFAULT="$(getconf _NPROCESSORS_ONLN 2>/dev/null || true)"
PARALLEL="${PARALLEL:-${PARALLEL_DEFAULT:-6}}"

# Extra Maven args ONLY for the E2E project.
E2E_MAVEN_ARGS="${E2E_MAVEN_ARGS:-}"
read -r -a E2E_MAVEN_ARGS_ARR <<<"${E2E_MAVEN_ARGS}"

mkdir -p "${LOG_DIR}"

JOB_NAMES=()
JOB_PIDS=()
JOB_LOGS=()
JOB_FAILED=0

# Start a command in the background, capturing its complete output in a log file.
# Usage: start_job <name> <workdir> <command...>
start_job() {
  local name="$1"; shift
  local workdir="$1"; shift
  local logfile="${LOG_DIR}/${name}.log"

  {
    echo "============================================================"
    echo "[${name}] start: $(date -Iseconds)"
    echo "[${name}] workdir: ${workdir}"
    echo "[${name}] command: $*"
    echo "============================================================"
  } >"${logfile}"

  (
    cd "${workdir}"
    "$@"
  ) >>"${logfile}" 2>&1 &

  local pid=$!
  JOB_NAMES+=("${name}")
  JOB_PIDS+=("${pid}")
  JOB_LOGS+=("${logfile}")
}

# Throttle background jobs to PARALLEL.
throttle() {
  while (( $(jobs -pr | wc -l) >= PARALLEL )); do
    sleep 0.1
  done
}

print_log_chunk() {
  local name="$1"
  local logfile="$2"
  echo
  echo "==================== ${name} (log: ${logfile}) ===================="
  cat "${logfile}"
  echo "==================== end: ${name} ===================="
}

cleanup() {
  local p
  for p in "${JOB_PIDS[@]:-}"; do
    if kill -0 "${p}" 2>/dev/null; then
      kill "${p}" 2>/dev/null || true
    fi
  done

  # Always print build duration when exiting (success, failure, or interrupt).
  print_total_time
}
trap cleanup INT TERM EXIT

# -----------------------------------------------------------------------------
# 1) Build utilities first (can produce artifacts used by services)
# -----------------------------------------------------------------------------
(
  cd "${ROOT_DIR}/utilities/messaging-utilities"
  ./build.sh
)

# -----------------------------------------------------------------------------
# 2) Build Maven services in parallel
#    - Services run tests normally (always).
#    - E2E project is built with -DskipTests (only there).
# -----------------------------------------------------------------------------
MODULES=(
  "account-service"
  "token-service"
  "payment-service"
  "reporting-service"
  "dtu-pay-server"
)

for m in "${MODULES[@]}"; do
  throttle
  start_job "${m}" "${ROOT_DIR}/${m}" mvn clean package
done

# Build E2E project too, but always skip its tests.
throttle
start_job "dtu-pay-E2ETest" "${ROOT_DIR}/dtu-pay-E2ETest" mvn clean package -DskipTests "${E2E_MAVEN_ARGS_ARR[@]}"

# Wait for completion, collect status, and print grouped logs.
for i in "${!JOB_PIDS[@]}"; do
  name="${JOB_NAMES[$i]}"
  pid="${JOB_PIDS[$i]}"

  if ! wait "${pid}"; then
    JOB_FAILED=1
  fi

  print_log_chunk "${name}" "${JOB_LOGS[$i]}"
done

if (( JOB_FAILED != 0 )); then
  echo
  echo "One or more Maven builds failed. Skipping docker compose build."
  exit 1
fi

# -----------------------------------------------------------------------------
# 3) Build Docker images only if everything succeeded
# -----------------------------------------------------------------------------
docker compose build
