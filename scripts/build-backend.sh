#!/bin/bash
set -e
cd "$(dirname "$0")/.."
echo "Building backend JAR..."
./mvnw clean package -DskipTests
echo ""
echo "Backend JAR built successfully:"
ls -lh target/crm-*.jar
