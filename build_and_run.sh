#!/bin/bash
set -e

pushd dtu-pay-server

docker compose -f compose.yml down || true

mvn clean package
# Create a new docker image if necessary.
docker compose build
# Restarts the container with the new image if necessary
docker compose up -d
# The server stays running.
# To terminate the server run docker-compose down in the
# simple-rest directory
# clean up images
docker image prune -f 
popd

# Give the Web server a chance to finish start up
sleep 2 

pushd dtu-pay-E2ETest 
mvn clean test
popd
