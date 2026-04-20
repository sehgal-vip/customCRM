#!/bin/bash
set -e
cd "$(dirname "$0")/../frontend"
echo "Installing dependencies..."
npm ci
echo "Building frontend..."
npm run build
echo ""
echo "Frontend built successfully at: frontend/dist/"
ls -lh dist/index.html
