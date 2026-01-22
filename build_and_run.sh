#!/bin/bash

set -e

./build.sh

./deploy.sh

sleep 15

./test.sh

docker image prune -f
