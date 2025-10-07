#!/bin/bash

# Script to sync images from backend to image service
# This script should be run after both services are deployed

echo "🔄 Starting image synchronization..."

# Get pod names
BACKEND_POD=$(kubectl get pods -n microsponsoring -l app=backend -o jsonpath='{.items[0].metadata.name}')
IMAGE_POD=$(kubectl get pods -n microsponsoring -l app=image-service -o jsonpath='{.items[0].metadata.name}')

echo "Backend pod: $BACKEND_POD"
echo "Image service pod: $IMAGE_POD"

# Check if pods exist
if [ -z "$BACKEND_POD" ]; then
    echo "❌ Backend pod not found!"
    exit 1
fi

if [ -z "$IMAGE_POD" ]; then
    echo "❌ Image service pod not found!"
    exit 1
fi

# Create images directory in image service if it doesn't exist
kubectl exec -it $IMAGE_POD -n microsponsoring -- mkdir -p /usr/share/nginx/html/images

# Get list of images from backend
echo "📋 Getting list of images from backend..."
kubectl exec -it $BACKEND_POD -n microsponsoring -- ls /app/images/ > /tmp/backend_images.txt

# Copy each image to the image service
echo "📤 Copying images to image service..."
while IFS= read -r image; do
    if [ -n "$image" ] && [ "$image" != "total" ] && [ "$image" != "drwx" ]; then
        echo "Copying $image..."
        kubectl exec -it $BACKEND_POD -n microsponsoring -- sh -c "base64 /app/images/$image" > /tmp/${image}.b64
        kubectl cp /tmp/${image}.b64 microsponsoring/$IMAGE_POD:/tmp/${image}.b64
        kubectl exec -it $IMAGE_POD -n microsponsoring -- sh -c "base64 -d /tmp/${image}.b64 > /usr/share/nginx/html/images/$image"
        rm -f /tmp/${image}.b64
        kubectl exec -it $IMAGE_POD -n microsponsoring -- rm -f /tmp/${image}.b64
    fi
done < /tmp/backend_images.txt

# Clean up
rm -f /tmp/backend_images.txt

# Verify images were copied
echo "✅ Verifying images in image service..."
kubectl exec -it $IMAGE_POD -n microsponsoring -- ls -la /usr/share/nginx/html/images/

echo "🎉 Image synchronization completed!"
