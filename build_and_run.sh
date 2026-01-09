#!/bin/bash
set -e

# Package
./mvnw -q package

# Docker build
docker build -t dtu-pay:latest .

# Docker compose
docker compose up -d

