#!/bin/bash
set -e
mvn clean package
docker-compose build payment-service

cd ../
docker image prune -f
docker system prune -f
docker-compose up -d
