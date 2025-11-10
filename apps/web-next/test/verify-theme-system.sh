#!/bin/bash

# Theme Integration Verification Script
# This script performs basic verification of the theme system

set -e

echo "🧪 Theme Integration System Verification"
echo "========================================"
echo ""

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

BASE_URL="http://localhost:3001"
API_URL="$BASE_URL/api"

# Function to check if server is running
check_server() {
    echo "Checking if development server is running..."
    if curl -s -o /dev/null -w "%{http_code}" "$BASE_URL" | grep -q "200\|301\|302"; then
        echo -e "${GREEN}✅ Server is running${NC}"
        return 0
    else
        echo -e "${RED}❌ Server is not running${NC}"
        echo "Please start the development server with: pnpm dev"
        exit 1
    fi
}

# Function to test themes API
test_themes_api() {
    echo ""
    echo "Test 1: GET /api/themes"
    echo "------------------------"
    
    RESPONSE=$(curl -s "$API_URL/themes")
    
    if echo "$RESPONSE" | jq -e '. | length > 0' > /dev/null 2>&1; then
        echo -e "${GREEN}✅ Themes API working${NC}"
        echo "Available themes:"
        echo "$RESPONSE" | jq -r '.[] | "  - \(.name) (\(.key))"'
        return 0
    else
        echo -e "${RED}❌ Themes API failed${NC}"
        echo "Response: $RESPONSE"
        return 1
    fi
}

# Function to test tenant API
test_tenant_api() {
    echo ""
    echo "Test 2: GET /api/internal/tenant"
    echo "---------------------------------"
    
    RESPONSE=$(curl -s -H "x-tenant-slug: clinic-a" "$API_URL/internal/tenant")
    
    if echo "$RESPONSE" | jq -e '.slug == "clinic-a"' > /dev/null 2>&1; then
        echo -e "${GREEN}✅ Tenant API working${NC}"
        echo "Tenant info:"
        echo "$RESPONSE" | jq '.'
        return 0
    else
        echo -e "${RED}❌ Tenant API failed${NC}"
        echo "Response: $RESPONSE"
        return 1
    fi
}

# Function to test theme update validation
test_theme_update_validation() {
    echo ""
    echo "Test 3: POST /api/tenants/[slug]/theme (validation)"
    echo "----------------------------------------------------"
    
    RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$API_URL/tenants/clinic-a/theme" \
        -H "Content-Type: application/json" \
        -d '{"themeId": "invalid-theme-id"}')
    
    HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
    BODY=$(echo "$RESPONSE" | sed '$d')
    
    if [ "$HTTP_CODE" = "404" ]; then
        echo -e "${GREEN}✅ Theme validation working${NC}"
        echo "Invalid theme ID correctly rejected"
        return 0
    else
        echo -e "${RED}❌ Theme validation not working as expected${NC}"
        echo "HTTP Code: $HTTP_CODE"
        echo "Response: $BODY"
        return 1
    fi
}

# Function to check database
check_database() {
    echo ""
    echo "Database Verification"
    echo "---------------------"
    
    if command -v psql > /dev/null 2>&1; then
        echo "Checking themes in database..."
        # This would require database credentials
        echo -e "${YELLOW}⚠️  Manual database check recommended${NC}"
        echo "Run: pnpm prisma:studio"
    else
        echo -e "${YELLOW}⚠️  psql not found, skipping database check${NC}"
        echo "Verify themes exist using: pnpm prisma:studio"
    fi
}

# Function to check hosts file
check_hosts_file() {
    echo ""
    echo "Hosts File Verification"
    echo "-----------------------"
    
    HOSTS_FILE="/etc/hosts"
    
    if [ -f "$HOSTS_FILE" ]; then
        if grep -q "clinic-a.localhost" "$HOSTS_FILE" && \
           grep -q "clinic-b.localhost" "$HOSTS_FILE"; then
            echo -e "${GREEN}✅ Test subdomains configured in hosts file${NC}"
        else
            echo -e "${YELLOW}⚠️  Test subdomains not found in hosts file${NC}"
            echo "Add these lines to $HOSTS_FILE:"
            echo "  127.0.0.1 clinic-a.localhost"
            echo "  127.0.0.1 clinic-b.localhost"
            echo "  127.0.0.1 custom.localhost"
        fi
    else
        echo -e "${YELLOW}⚠️  Hosts file not found at $HOSTS_FILE${NC}"
    fi
}

# Function to check theme files
check_theme_files() {
    echo ""
    echo "Theme Files Verification"
    echo "------------------------"
    
    THEMES_DIR="themes"
    
    if [ -d "$THEMES_DIR" ]; then
        echo "Checking theme directories..."
        
        for theme in clinic barber default; do
            if [ -f "$THEMES_DIR/$theme/layout.tsx" ]; then
                echo -e "${GREEN}✅ $theme theme exists${NC}"
            else
                echo -e "${RED}❌ $theme theme missing${NC}"
            fi
        done
    else
        echo -e "${RED}❌ Themes directory not found${NC}"
    fi
}

# Main execution
main() {
    # Check if jq is installed
    if ! command -v jq > /dev/null 2>&1; then
        echo -e "${RED}❌ jq is not installed${NC}"
        echo "Please install jq to run this script"
        echo "  macOS: brew install jq"
        echo "  Ubuntu: sudo apt-get install jq"
        exit 1
    fi
    
    # Run checks
    check_server
    check_theme_files
    check_hosts_file
    
    # Run API tests
    PASSED=0
    FAILED=0
    
    if test_themes_api; then
        ((PASSED++))
    else
        ((FAILED++))
    fi
    
    if test_tenant_api; then
        ((PASSED++))
    else
        ((FAILED++))
    fi
    
    if test_theme_update_validation; then
        ((PASSED++))
    else
        ((FAILED++))
    fi
    
    check_database
    
    # Summary
    echo ""
    echo "========================================"
    echo "Test Summary"
    echo "========================================"
    echo -e "Passed: ${GREEN}$PASSED${NC}"
    echo -e "Failed: ${RED}$FAILED${NC}"
    echo ""
    
    if [ $FAILED -eq 0 ]; then
        echo -e "${GREEN}✅ All tests passed!${NC}"
        echo ""
        echo "Next steps:"
        echo "1. Test in browser: http://clinic-a.localhost:3001"
        echo "2. Test theme switching: http://clinic-a.localhost:3001/en/dashboard/settings/theme"
        echo "3. Review full test guide: test/THEME_INTEGRATION_TEST_GUIDE.md"
        exit 0
    else
        echo -e "${RED}❌ Some tests failed${NC}"
        echo ""
        echo "Troubleshooting:"
        echo "1. Ensure database is seeded: pnpm prisma:seed"
        echo "2. Check server logs for errors"
        echo "3. Review test guide: test/THEME_INTEGRATION_TEST_GUIDE.md"
        exit 1
    fi
}

# Run main function
main
