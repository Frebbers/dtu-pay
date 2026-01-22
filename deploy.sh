#!/bin/bash

set -e

docker image prune -f
docker compose up -d rabbitmq
sleep 20
docker compose up -d account-service token-service payment-service reporting-service dtu-pay