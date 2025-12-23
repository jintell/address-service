

#!/bin/bash
set -e

# Deployment script for address-service
echo "Starting deployment of address-service"
echo "Using image: $IMAGE_NAME"

# Pull the latest image
echo "Pulling Docker image..."
docker pull $IMAGE_NAME

# Ensure directory for logs exists
mkdir -p ~/app/logs

# Stop and remove existing container if it exists
echo "Removing existing container..."
docker rm -f address-service || true

# Create a new container with Docker run (bypassing docker-compose)
echo "Creating new container with Docker directly..."
docker run -d \
  --name address-service \
  --restart unless-stopped \
  --network host \
  -e SPRING_PROFILE=${SPRING_PROFILE} \
  -e CONFIG_SERVER_HOST=${CONFIG_SERVER_HOST:-config-server} \
  -e DB_HOST=${DB_HOST:-localhost} \
  -e DB_USERNAME=${DB_USERNAME} \
  -e DB_PASSWORD=${DB_PASSWORD} \
  -e REDIS_HOST=${REDIS_HOST:-localhost} \
  -e REDIS_PASSWORD=${REDIS_PASSWORD} \
  -e ROOT_LOG_LEVEL=INFO \
  -e "JAVA_TOOL_OPTIONS=${JAVA_TOOL_OPTIONS:- -XX:+UseG1GC -XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError}" \
  -v ~/app/logs:/app/logs:rw \
  --tmpfs /tmp \
  --user 1000:1000 \
  --security-opt no-new-privileges:true \
  --read-only \
  ${IMAGE_NAME}

# Check if the container is running
if docker ps | grep -q address-service; then
  echo "Deployment successful!"
else
  echo "Deployment failed. Container not running."
  docker logs address-service
  exit 1
fi

echo "Deployment completed successfully"