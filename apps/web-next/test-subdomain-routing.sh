#!/bin/bash

# Web App Subdomain Routing Test Script
# This script helps verify the subdomain routing setup and provides quick checks

set -e

echo "🧪 Web App Subdomain Routing Test Script"
echo "=========================================="
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check if web-next is running
echo "1. Checking if web-next is running on port 3001..."
if curl -s http://localhost:3001 > /dev/null; then
    echo -e "${GREEN}✓${NC} Web-next is running on port 3001"
else
    echo -e "${RED}✗${NC} Web-next is NOT running on port 3001"
    echo "   Please start it with: cd apps/web-next && pnpm dev"
    exit 1
fi
echo ""

# Check if API is running
echo "2. Checking if API is running on port 8080..."
if curl -s http://localhost:8080/public/health > /dev/null 2>&1 || curl -s http://localhost:8080 > /dev/null 2>&1; then
    echo -e "${GREEN}✓${NC} API is running on port 8080"
else
    echo -e "${YELLOW}⚠${NC} API might not be running on port 8080"
    echo "   Please verify API is running"
fi
echo ""

# Check hosts file configuration
echo "3. Checking hosts file configuration..."
if grep -q "tenant-a.localhost" /etc/hosts && grep -q "tenant-b.localhost" /etc/hosts; then
    echo -e "${GREEN}✓${NC} Hosts file is configured with tenant subdomains"
else
    echo -e "${RED}✗${NC} Hosts file is NOT configured"
    echo "   Please add these lines to /etc/hosts:"
    echo "   127.0.0.1 tenant-a.localhost"
    echo "   127.0.0.1 tenant-b.localhost"
    echo ""
    echo "   Run: sudo nano /etc/hosts"
    exit 1
fi
echo ""

# Test subdomain resolution
echo "4. Testing subdomain DNS resolution..."
if ping -c 1 tenant-a.localhost > /dev/null 2>&1; then
    echo -e "${GREEN}✓${NC} tenant-a.localhost resolves correctly"
else
    echo -e "${RED}✗${NC} tenant-a.localhost does NOT resolve"
    echo "   Try flushing DNS cache:"
    echo "   macOS: sudo dscacheutil -flushcache"
    echo "   Linux: sudo systemd-resolve --flush-caches"
fi

if ping -c 1 tenant-b.localhost > /dev/null 2>&1; then
    echo -e "${GREEN}✓${NC} tenant-b.localhost resolves correctly"
else
    echo -e "${RED}✗${NC} tenant-b.localhost does NOT resolve"
fi
echo ""

# Test tenant-a subdomain
echo "5. Testing tenant-a.localhost:3001..."
RESPONSE_A=$(curl -s -I http://tenant-a.localhost:3001 2>&1)
if echo "$RESPONSE_A" | grep -q "200\|301\|302"; then
    echo -e "${GREEN}✓${NC} tenant-a.localhost:3001 is accessible"
    
    # Check for Set-Cookie header
    if echo "$RESPONSE_A" | grep -qi "set-cookie.*tenantSlug"; then
        echo -e "${GREEN}✓${NC} Set-Cookie header is present"
    else
        echo -e "${YELLOW}⚠${NC} Set-Cookie header not found in response"
    fi
else
    echo -e "${RED}✗${NC} tenant-a.localhost:3001 is NOT accessible"
fi
echo ""

# Test tenant-b subdomain
echo "6. Testing tenant-b.localhost:3001..."
RESPONSE_B=$(curl -s -I http://tenant-b.localhost:3001 2>&1)
if echo "$RESPONSE_B" | grep -q "200\|301\|302"; then
    echo -e "${GREEN}✓${NC} tenant-b.localhost:3001 is accessible"
    
    # Check for Set-Cookie header
    if echo "$RESPONSE_B" | grep -qi "set-cookie.*tenantSlug"; then
        echo -e "${GREEN}✓${NC} Set-Cookie header is present"
    else
        echo -e "${YELLOW}⚠${NC} Set-Cookie header not found in response"
    fi
else
    echo -e "${RED}✗${NC} tenant-b.localhost:3001 is NOT accessible"
fi
echo ""

# Database check (optional - requires psql)
echo "7. Checking database for test tenants..."
if command -v psql &> /dev/null; then
    # Try to connect to database (adjust connection string as needed)
    DB_CHECK=$(psql -h localhost -U postgres -d clinic_db -t -c "SELECT slug FROM tenants WHERE slug IN ('tenant-a', 'tenant-b');" 2>&1 || echo "error")
    
    if echo "$DB_CHECK" | grep -q "tenant-a"; then
        echo -e "${GREEN}✓${NC} Tenant 'tenant-a' exists in database"
    else
        echo -e "${YELLOW}⚠${NC} Tenant 'tenant-a' NOT found in database"
        echo "   Create it with: INSERT INTO tenants (slug, name, status) VALUES ('tenant-a', 'Tenant A Clinic', 'ACTIVE');"
    fi
    
    if echo "$DB_CHECK" | grep -q "tenant-b"; then
        echo -e "${GREEN}✓${NC} Tenant 'tenant-b' exists in database"
    else
        echo -e "${YELLOW}⚠${NC} Tenant 'tenant-b' NOT found in database"
        echo "   Create it with: INSERT INTO tenants (slug, name, status) VALUES ('tenant-b', 'Tenant B Clinic', 'ACTIVE');"
    fi
else
    echo -e "${YELLOW}⚠${NC} psql not found, skipping database check"
    echo "   Please manually verify tenants exist in database"
fi
echo ""

# Summary
echo "=========================================="
echo "Setup Verification Complete!"
echo ""
echo "Next Steps:"
echo "1. Open browser and navigate to http://tenant-a.localhost:3001"
echo "2. Open DevTools (F12) > Application > Cookies"
echo "3. Verify 'tenantSlug' cookie is set to 'tenant-a'"
echo "4. Open DevTools > Network tab"
echo "5. Navigate to /en/services and check API request headers"
echo "6. Verify 'x-tenant-slug: tenant-a' header is present"
echo "7. Repeat for http://tenant-b.localhost:3001"
echo ""
echo "For detailed test instructions, see:"
echo "  apps/web-next/SUBDOMAIN_ROUTING_TEST_GUIDE.md"
echo ""
echo "To run browser-based tests, open console and run:"
echo "  window.SubdomainRoutingTests.verifyTenantContext()"
echo ""
