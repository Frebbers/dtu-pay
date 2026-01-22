#!/bin/bash
set -e

echo "Building Maven modules and Docker images..."
mvn clean package docker:build -f pom.xml
