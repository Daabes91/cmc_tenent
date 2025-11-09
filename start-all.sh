#!/bin/bash

# Start All Services Script
# This script starts all components of the multi-tenant clinic management system

set -e

echo "🚀 Starting Multi-Tenant Clinic Management System"
echo "=================================================="
echo ""

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker is not running. Please start Docker Desktop first."
    exit 1
fi

echo "✅ Docker is running"
echo ""

# Clean up old containers and volumes
echo "🧹 Cleaning up old containers..."
docker compose down -v 2>/dev/null || true
echo ""

# Build and start all services
echo "🏗️  Building and starting all services..."
echo "This may take a few minutes on first run..."
echo ""

docker compose up --build -d

echo ""
echo "⏳ Waiting for services to be healthy..."
echo ""

# Wait for services to be healthy
max_attempts=60
attempt=0

while [ $attempt -lt $max_attempts ]; do
    if docker compose ps | grep -q "healthy"; then
        echo "✅ Services are starting up..."
        break
    fi
    attempt=$((attempt + 1))
    sleep 2
    echo -n "."
done

echo ""
echo ""
echo "🎉 All services are starting!"
echo ""
echo "📍 Access the applications:"
echo "   • SAAS Admin Panel: http://localhost:3002"
echo "   • Tenant Admin Panel: http://localhost:3000"
echo "   • Public Web App: http://localhost:3001"
echo "   • API: http://localhost:8080"
echo "   • API Docs: http://localhost:8080/swagger-ui.html"
echo ""
echo "📊 View logs:"
echo "   docker compose logs -f"
echo ""
echo "🛑 Stop all services:"
echo "   docker compose down"
echo ""
echo "🔍 Check service status:"
echo "   docker compose ps"
echo ""
