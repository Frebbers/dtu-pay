#!/bin/bash
set -e

./install-libraries.sh
./compile-and-build-images.sh
./deploy-and-run-tests.sh
