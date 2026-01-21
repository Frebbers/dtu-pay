#!/bin/bash
set -e

echo "Running end-to-end tests with docker-compose..."
mvn clean docker-compose:up test -f dtu-pay-E2ETest
