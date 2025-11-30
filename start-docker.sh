#!/bin/bash

# Script to start all services with Docker Compose

echo "🐳 Starting Docker services..."
echo ""

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker is not running!"
    echo ""
    echo "Please start Docker Desktop and try again."
    echo ""
    echo "On macOS:"
    echo "  1. Open Docker Desktop application"
    echo "  2. Wait for Docker to start (whale icon in menu bar)"
    echo "  3. Run this script again"
    exit 1
fi

echo "✅ Docker is running"
echo ""

# Stop any existing containers
echo "🛑 Stopping existing containers..."
docker-compose down

echo ""
echo "🏗️  Building and starting all services..."
echo ""
echo "Services that will be started:"
echo "  - PostgreSQL Database (port 5442)"
echo "  - API Backend (port 8080)"
echo "  - Patient Portal (port 3001)"
echo "  - Admin Panel (port 3000)"
echo "  - SaaS Admin (port 3002)"
echo "  - SaaS Landing (port 3003)"
echo ""

# Build and start services
docker-compose up --build

# Note: Use Ctrl+C to stop all services
