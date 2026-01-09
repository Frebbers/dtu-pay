#!/bin/bash
set -e
chmod +x mvnw
./mvnw compile quarkus:dev
QUARKUS_PID=$!
./mvnw -q test
echo "Stopping Quarkus application with PID $QUARKUS_PID"
kill $QUARKUS_PID

