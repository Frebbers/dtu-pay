#!/bin/bash

set -e

pushd dtu-pay-E2ETest
mvn clean test
popd
