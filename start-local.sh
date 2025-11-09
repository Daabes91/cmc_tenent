#!/bin/bash

# Start Local Development (No Docker for apps)
# This script starts the database in Docker and apps locally

set -e

echo "🚀 Starting Local Development Environment"
echo "=========================================="
echo ""

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker is not running. Please start Docker Desktop first."
    exit 1
fi

echo "✅ Docker is running"
echo ""

# Start PostgreSQL in Docker
echo "🐘 Starting PostgreSQL database..."
docker run -d --name clinic-postgres \
  -e POSTGRES_DB=clinic_multi_tenant \
  -e POSTGRES_USER=clinic \
  -e POSTGRES_PASSWORD=clinic_password \
  -p 5442:5432 \
  postgres:15 2>/dev/null || docker start clinic-postgres

echo "✅ PostgreSQL is running on port 5442"
echo ""

echo "📝 Next steps:"
echo ""
echo "1. Start the API (in a new terminal):"
echo "   cd apps/api"
echo "   ./mvnw spring-boot:run"
echo ""
echo "2. Start SAAS Admin Panel (in a new terminal):"
echo "   cd apps/saas-admin-nuxt"
echo "   npm install"
echo "   npm run dev"
echo ""
echo "3. Start Tenant Admin Panel (optional, in a new terminal):"
echo "   cd apps/admin-nuxt"
echo "   npm install"
echo "   npm run dev"
echo ""
echo "4. Start Public Web App (optional, in a new terminal):"
echo "   cd apps/web-next"
echo "   npm install"
echo "   npm run dev"
echo ""
echo "📍 Once started, access:"
echo "   • SAAS Admin Panel: http://localhost:3002"
echo "   • Tenant Admin Panel: http://localhost:3001"
echo "   • Public Web App: http://localhost:3000"
echo "   • API: http://localhost:8080"
echo ""
echo "🛑 To stop PostgreSQL:"
echo "   docker stop clinic-postgres"
echo ""
