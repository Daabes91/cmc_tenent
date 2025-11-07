#!/bin/bash

# Dynamic Branding Reactivity Verification Script
# Tests Requirements 2.1, 2.2, 2.3

echo "🧪 Dynamic Branding Reactivity Verification"
echo "=========================================="

# Check if applications are running
check_app_running() {
    local port=$1
    local app_name=$2
    
    if curl -s "http://localhost:$port" > /dev/null 2>&1; then
        echo "✅ $app_name is running on port $port"
        return 0
    else
        echo "❌ $app_name is not running on port $port"
        return 1
    fi
}

# Verify test files exist
echo "📁 Checking test files..."
if [ -f "apps/admin-nuxt/test/dynamic-branding-reactivity.test.js" ]; then
    echo "✅ Admin-nuxt test file exists"
else
    echo "❌ Admin-nuxt test file missing"
    exit 1
fi

if [ -f "apps/web-next/test/dynamic-branding-reactivity.test.js" ]; then
    echo "✅ Web-next test file exists"
else
    echo "❌ Web-next test file missing"
    exit 1
fi

# Check if applications are running
echo ""
echo "🔍 Checking application status..."
admin_running=false
web_running=false

if check_app_running 3000 "Admin-nuxt"; then
    admin_running=true
fi

if check_app_running 3001 "Web-next"; then
    web_running=true
fi

# Generate test instructions
echo ""
echo "📋 Test Execution Instructions:"
echo ""

if [ "$admin_running" = true ] && [ "$web_running" = true ]; then
    echo "✅ Both applications are running. You can proceed with testing."
    echo ""
    echo "Run the following command for detailed test instructions:"
    echo "node test-dynamic-branding-reactivity.js both"
    echo ""
    echo "Or open these URLs in your browser:"
    echo "- Admin: http://localhost:3000"
    echo "- Web: http://localhost:3001"
    echo ""
    echo "Then follow the manual test procedures in the test files."
    
elif [ "$admin_running" = true ]; then
    echo "⚠️  Only admin-nuxt is running. Start web-next to test both applications."
    echo "cd apps/web-next && npm run dev"
    echo ""
    echo "For admin-only testing:"
    echo "node test-dynamic-branding-reactivity.js admin"
    
elif [ "$web_running" = true ]; then
    echo "⚠️  Only web-next is running. Start admin-nuxt to test both applications."
    echo "cd apps/admin-nuxt && npm run dev"
    echo ""
    echo "For web-only testing:"
    echo "node test-dynamic-branding-reactivity.js web"
    
else
    echo "❌ Neither application is running. Start them first:"
    echo ""
    echo "Terminal 1: cd apps/admin-nuxt && npm run dev"
    echo "Terminal 2: cd apps/web-next && npm run dev"
    echo ""
    echo "Then run this script again."
    exit 1
fi

# Test specific requirements
echo ""
echo "🎯 Requirements to verify:"
echo "- 2.1: Titles update when clinic settings change"
echo "- 2.2: Favicon updates when logo is changed"
echo "- 2.3: No page refresh required for updates"
echo ""

# Generate test report template
echo "📊 Generate test report template:"
echo "node test-dynamic-branding-reactivity.js --report-template > test-report.md"
echo ""

echo "🚀 Ready to test dynamic branding reactivity!"