#!/bin/bash
set -e
chmod +x mvnw
./mvnw compile quarkus:dev
./mvnw -q test

