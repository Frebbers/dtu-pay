#!/bin/bash
set -e

# Package
mvn compile
mvn package -Dmaven.test.skip
# Docker build
docker build -t dtu-pay:latest .

# Docker compose
docker compose up -d

