#!/bin/bash

# Production Verification Script
# Verifies that the deployed site is working correctly

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

print_info() {
    echo -e "${BLUE}ℹ ${1}${NC}"
}

print_success() {
    echo -e "${GREEN}✓ ${1}${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠ ${1}${NC}"
}

print_error() {
    echo -e "${RED}✗ ${1}${NC}"
}

# Check if URL is provided
if [ -z "$1" ]; then
    print_error "Usage: ./verify-production.sh <site-url>"
    echo "Example: ./verify-production.sh https://yourdomain.com"
    exit 1
fi

SITE_URL="$1"
FAILED_CHECKS=0

echo ""
echo "╔════════════════════════════════════════════════════════════╗"
echo "║                                                            ║"
echo "║        Production Verification - SaaS Landing Page         ║"
echo "║                                                            ║"
echo "╚════════════════════════════════════════════════════════════╝"
echo ""
print_info "Verifying: $SITE_URL"
echo ""

# Function to check HTTP status
check_url() {
    local url=$1
    local expected_status=${2:-200}
    local description=$3
    
    print_info "Checking: $description"
    
    status_code=$(curl -s -o /dev/null -w "%{http_code}" "$url" || echo "000")
    
    if [ "$status_code" = "$expected_status" ]; then
        print_success "$description - Status: $status_code"
        return 0
    else
        print_error "$description - Expected: $expected_status, Got: $status_code"
        FAILED_CHECKS=$((FAILED_CHECKS + 1))
        return 1
    fi
}

# Function to check if content exists
check_content() {
    local url=$1
    local search_string=$2
    local description=$3
    
    print_info "Checking: $description"
    
    content=$(curl -s "$url")
    
    if echo "$content" | grep -q "$search_string"; then
        print_success "$description - Content found"
        return 0
    else
        print_error "$description - Content not found: $search_string"
        FAILED_CHECKS=$((FAILED_CHECKS + 1))
        return 1
    fi
}

# 1. Check Homepage
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "1. Site Accessibility"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
check_url "$SITE_URL" 200 "Homepage"
check_url "$SITE_URL/blog" 200 "Blog Listing Page"
check_url "$SITE_URL/signup" 200 "Signup Page"
echo ""

# 2. Check SEO Elements
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "2. SEO Elements"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
check_url "$SITE_URL/sitemap.xml" 200 "Sitemap"
check_url "$SITE_URL/robots.txt" 200 "Robots.txt"
check_content "$SITE_URL" "google-site-verification" "Google Search Console Verification Tag"
check_content "$SITE_URL" "og:title" "Open Graph Meta Tags"
check_content "$SITE_URL" "twitter:card" "Twitter Card Meta Tags"
echo ""

# 3. Check Healthcare Content
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "3. Healthcare Content"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
check_content "$SITE_URL" "clinic" "Healthcare Terminology - Clinic"
check_content "$SITE_URL" "patient" "Healthcare Terminology - Patient"
check_content "$SITE_URL" "appointment" "Healthcare Terminology - Appointment"
echo ""

# 4. Check Analytics
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "4. Analytics Configuration"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
check_content "$SITE_URL" "googletagmanager.com/gtag/js" "Google Analytics Script"
check_content "$SITE_URL" "G-" "GA4 Measurement ID"
echo ""

# 5. Check Structured Data
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "5. Structured Data"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
check_content "$SITE_URL" "application/ld+json" "JSON-LD Structured Data"
check_content "$SITE_URL" "Organization" "Organization Schema"
echo ""

# 6. Check Blog Functionality
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "6. Blog Functionality"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

# Get first blog post slug from sitemap
FIRST_POST=$(curl -s "$SITE_URL/sitemap.xml" | grep -o '<loc>[^<]*blog/[^<]*</loc>' | head -1 | sed 's/<loc>//;s/<\/loc>//')

if [ -n "$FIRST_POST" ]; then
    check_url "$FIRST_POST" 200 "First Blog Post"
    check_content "$FIRST_POST" "BlogPosting" "Blog Post Schema"
else
    print_warning "No blog posts found in sitemap"
fi
echo ""

# 7. Performance Check
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "7. Performance"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

# Measure page load time
print_info "Measuring page load time..."
LOAD_TIME=$(curl -o /dev/null -s -w '%{time_total}' "$SITE_URL")
LOAD_TIME_MS=$(echo "$LOAD_TIME * 1000" | bc)

if (( $(echo "$LOAD_TIME < 3.0" | bc -l) )); then
    print_success "Page load time: ${LOAD_TIME_MS}ms (Good)"
else
    print_warning "Page load time: ${LOAD_TIME_MS}ms (Consider optimization)"
fi
echo ""

# 8. Security Headers
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "8. Security Headers"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

HEADERS=$(curl -s -I "$SITE_URL")

check_header() {
    local header=$1
    local description=$2
    
    if echo "$HEADERS" | grep -qi "$header"; then
        print_success "$description present"
    else
        print_warning "$description missing (recommended)"
    fi
}

check_header "X-Frame-Options" "X-Frame-Options"
check_header "X-Content-Type-Options" "X-Content-Type-Options"
check_header "Strict-Transport-Security" "HSTS"
echo ""

# Summary
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "Verification Summary"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

if [ $FAILED_CHECKS -eq 0 ]; then
    print_success "All checks passed! ✨"
    echo ""
    print_info "Next Steps:"
    echo "  1. Submit sitemap to Google Search Console: $SITE_URL/sitemap.xml"
    echo "  2. Monitor Google Analytics real-time data"
    echo "  3. Test critical user flows manually"
    echo "  4. Set up uptime monitoring"
    echo ""
    exit 0
else
    print_error "$FAILED_CHECKS check(s) failed"
    echo ""
    print_info "Please review the failed checks and fix any issues"
    echo ""
    exit 1
fi
