#!/bin/bash
set -e
chmod +x mvnw
./mvnw compile 
mvn package -Dmaven.test.skip
java -jar target/quarkus-app/quarkus-run.jar &
QUARKUS_PID=$!
./mvnw -q test
echo "Stopping Quarkus application with PID $QUARKUS_PID"
kill $QUARKUS_PID

