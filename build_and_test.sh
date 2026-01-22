#!/bin/bash
set -e

echo "Building project and running service tests in parallel..."
./build_and_run.sh

pushd dtu-pay-E2ETest
echo "Running E2E tests..."
mvn test