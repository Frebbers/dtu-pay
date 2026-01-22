#!/bin/bash

set -e

./build.sh

docker compose up -d

docker image prune -f
