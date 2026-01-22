#!/bin/bash
set -e

echo "Installing shared libraries..."
mvn clean install -f utilities/messaging-utilities
