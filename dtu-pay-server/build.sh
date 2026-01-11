#!/bin/bash
set -e

# Package
mvn compile
mvn package -Dmaven.test.skip