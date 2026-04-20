#!/bin/bash
set -e
cd "$(dirname "$0")/.."

JAR=$(ls target/crm-*.jar 2>/dev/null | head -1)
if [ -z "$JAR" ]; then
  echo "No JAR found. Run ./scripts/build-backend.sh first."
  exit 1
fi

PROFILE="${1:-}"

echo "Starting TurnoCRM backend..."
echo "JAR: $JAR"
if [ -n "$PROFILE" ]; then
  echo "Profile: $PROFILE"
  java -jar "$JAR" --spring.profiles.active="$PROFILE"
else
  java -jar "$JAR"
fi
