#!/bin/bash
set -e

echo "Building DTU Pay Server..."
sh build.sh
# Docker compose
echo "Starting Quarkus server..."
docker compose up -d

