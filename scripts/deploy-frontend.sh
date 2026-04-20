#!/bin/bash
set -e
cd "$(dirname "$0")/../frontend"

S3_BUCKET="${S3_BUCKET:?Set S3_BUCKET}"
CLOUDFRONT_DIST_ID="${CLOUDFRONT_DIST_ID:?Set CLOUDFRONT_DIST_ID}"
API_BASE_URL="${API_BASE_URL:?Set API_BASE_URL (e.g. https://api.yourdomain.com/api/v1)}"

# Build with production API URL
echo "Building frontend with API_BASE_URL=$API_BASE_URL..."
npm ci
VITE_API_BASE_URL="$API_BASE_URL" npm run build

# Sync to S3
echo "Deploying to s3://$S3_BUCKET..."
aws s3 sync dist/ "s3://$S3_BUCKET/" --delete

# Invalidate CloudFront cache
echo "Invalidating CloudFront cache..."
aws cloudfront create-invalidation --distribution-id "$CLOUDFRONT_DIST_ID" --paths "/*"

echo "Frontend deployed successfully."
