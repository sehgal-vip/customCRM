#!/bin/bash
set -e
cd "$(dirname "$0")/.."

EC2_HOST="${EC2_HOST:?Set EC2_HOST (e.g. ec2-user@your-ip)}"
EC2_USER="${EC2_USER:-ec2-user}"
REMOTE_DIR="/opt/turnocrm"

# Build JAR
echo "Building backend JAR..."
./scripts/build-backend.sh

JAR=$(ls target/crm-*.jar 2>/dev/null | head -1)
if [ -z "$JAR" ]; then
  echo "No JAR found after build."
  exit 1
fi

# Upload to EC2
echo "Uploading JAR to $EC2_USER@$EC2_HOST:$REMOTE_DIR/crm.jar..."
scp "$JAR" "$EC2_USER@$EC2_HOST:$REMOTE_DIR/crm.jar"

# Restart service
echo "Restarting turnocrm service..."
ssh "$EC2_USER@$EC2_HOST" "sudo systemctl restart turnocrm"

echo "Backend deployed successfully."
