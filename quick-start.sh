#!/bin/bash

# Microsponsoring Application Quick Start Script
# This script helps you quickly deploy the application

set -e

echo "🚀 Starting Microsponsoring Application Deployment..."

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    print_error "Docker is not running. Please start Docker and try again."
    exit 1
fi

# Check if Docker Compose is available
if ! command -v docker-compose &> /dev/null; then
    print_error "Docker Compose is not installed. Please install it and try again."
    exit 1
fi

print_status "Building and starting services..."

# Build and start all services
docker-compose up --build -d

print_status "Waiting for services to be ready..."

# Wait for MySQL to be ready
print_status "Waiting for MySQL to be ready..."
until docker-compose exec -T mysql mysqladmin ping -h"localhost" --silent; do
    sleep 2
done

# Wait for backend to be ready
print_status "Waiting for backend to be ready..."
until curl -f http://localhost:8080/actuator/health > /dev/null 2>&1; do
    sleep 5
done

# Wait for frontend to be ready
print_status "Waiting for frontend to be ready..."
until curl -f http://localhost/health > /dev/null 2>&1; do
    sleep 5
done

print_status "All services are ready!"

echo ""
echo "🎉 Application successfully deployed!"
echo ""
echo "📱 Frontend: http://localhost"
echo "🔧 Backend API: http://localhost:8080"
echo "📊 Grafana Dashboard: http://localhost:3000 (admin/admin123)"
echo "📈 Prometheus: http://localhost:9090"
echo "🗄️  MySQL: localhost:3306"
echo "🔴 Redis: localhost:6379"
echo ""
echo "📋 Useful commands:"
echo "  - View logs: docker-compose logs -f [service-name]"
echo "  - Stop services: docker-compose down"
echo "  - Restart services: docker-compose restart"
echo "  - View running containers: docker-compose ps"
echo ""
echo "🔍 Health checks:"
echo "  - Backend health: curl http://localhost:8080/actuator/health"
echo "  - Frontend health: curl http://localhost/health"
echo ""

# Check if services are healthy
print_status "Performing health checks..."

# Backend health check
if curl -f http://localhost:8080/actuator/health > /dev/null 2>&1; then
    echo -e "✅ Backend: ${GREEN}Healthy${NC}"
else
    echo -e "❌ Backend: ${RED}Unhealthy${NC}"
fi

# Frontend health check
if curl -f http://localhost/health > /dev/null 2>&1; then
    echo -e "✅ Frontend: ${GREEN}Healthy${NC}"
else
    echo -e "❌ Frontend: ${RED}Unhealthy${NC}"
fi

# MySQL health check
if docker-compose exec -T mysql mysqladmin ping -h"localhost" --silent; then
    echo -e "✅ MySQL: ${GREEN}Healthy${NC}"
else
    echo -e "❌ MySQL: ${RED}Unhealthy${NC}"
fi

echo ""
print_status "Deployment completed successfully!"
print_status "You can now access your application at the URLs shown above." 