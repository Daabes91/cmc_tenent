#!/bin/bash

# Test script for patient profile image upload
# Usage: ./test-patient-image-upload.sh <path-to-image> <jwt-token>

if [ "$#" -ne 2 ]; then
    echo "Usage: $0 <path-to-image-file> <jwt-token>"
    echo "Example: $0 test.png 'eyJ0eXAi...'"
    exit 1
fi

IMAGE_PATH="$1"
JWT_TOKEN="$2"

if [ ! -f "$IMAGE_PATH" ]; then
    echo "Error: Image file not found at $IMAGE_PATH"
    exit 1
fi

echo "Uploading image: $IMAGE_PATH"
echo "Using JWT token: ${JWT_TOKEN:0:50}..."
echo ""

curl -v -X POST 'http://localhost:8080/public/patient/upload-profile-image' \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -H "Accept: application/json" \
  -F "file=@$IMAGE_PATH"

echo ""
echo "Upload complete!"
