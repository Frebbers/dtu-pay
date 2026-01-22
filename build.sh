#!/bin/bash

set -e

pushd utilities 
pushd messaging-utilities
./build.sh
popd
popd

pushd account-service
mvn clean package
popd

pushd token-service
mvn clean package
popd

pushd payment-service
mvn clean package
popd

pushd reporting-service
mvn clean package
popd

pushd dtu-pay-server
mvn clean package
popd

docker compose build