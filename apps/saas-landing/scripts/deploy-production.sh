#!/bin/bash

# Production Deployment Script for SaaS Landing Page
# This script automates the deployment process with safety checks

set -e  # Exit on any error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
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

# Function to check if command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Banner
echo ""
echo "╔════════════════════════════════════════════════════════════╗"
echo "║                                                            ║"
echo "║        SaaS Landing Page - Production Deployment          ║"
echo "║                                                            ║"
echo "╚════════════════════════════════════════════════════════════╝"
echo ""

# Check if we're in the correct directory
if [ ! -f "package.json" ]; then
    print_error "package.json not found. Please run this script from apps/saas-landing directory"
    exit 1
fi

print_info "Starting pre-deployment checks..."

# Check for required commands
print_info "Checking required tools..."
if ! command_exists pnpm; then
    print_error "pnpm is not installed. Please install it first: npm install -g pnpm"
    exit 1
fi
print_success "pnpm is installed"

# Check for .env.production file
if [ ! -f ".env.production" ]; then
    print_warning ".env.production file not found"
    print_info "Creating from template..."
    if [ -f ".env.production.template" ]; then
        cp .env.production.template .env.production
        print_warning "Please edit .env.production with your actual values before continuing"
        exit 1
    else
        print_error ".env.production.template not found"
        exit 1
    fi
fi
print_success ".env.production file exists"

# Load environment variables
set -a
source .env.production
set +a

# Validate required environment variables
print_info "Validating environment variables..."
REQUIRED_VARS=(
    "NEXT_PUBLIC_API_BASE_URL"
    "NEXT_PUBLIC_APP_URL"
    "NEXT_PUBLIC_PAYPAL_CLIENT_ID"
    "NEXT_PUBLIC_GA_MEASUREMENT_ID"
    "NEXT_PUBLIC_GSC_VERIFICATION"
    "NEXT_PUBLIC_SITE_URL"
    "NEXT_PUBLIC_SITE_NAME"
)

MISSING_VARS=()
for var in "${REQUIRED_VARS[@]}"; do
    if [ -z "${!var}" ]; then
        MISSING_VARS+=("$var")
    fi
done

if [ ${#MISSING_VARS[@]} -ne 0 ]; then
    print_error "Missing required environment variables:"
    for var in "${MISSING_VARS[@]}"; do
        echo "  - $var"
    done
    exit 1
fi
print_success "All required environment variables are set"

# Check Git status
print_info "Checking Git status..."
if [ -n "$(git status --porcelain)" ]; then
    print_warning "You have uncommitted changes"
    read -p "Do you want to continue? (y/n) " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        print_info "Deployment cancelled"
        exit 0
    fi
fi
print_success "Git status checked"

# Run tests
print_info "Running tests..."
if pnpm test:run; then
    print_success "All tests passed"
else
    print_error "Tests failed. Please fix the issues before deploying"
    exit 1
fi

# Build the application
print_info "Building application..."
if pnpm build; then
    print_success "Build completed successfully"
else
    print_error "Build failed. Please fix the issues before deploying"
    exit 1
fi

# Verify build output
if [ ! -d ".next" ]; then
    print_error ".next directory not found. Build may have failed"
    exit 1
fi
print_success "Build output verified"

# Ask for deployment confirmation
echo ""
print_warning "Ready to deploy to production!"
echo ""
echo "Deployment Details:"
echo "  Site URL: $NEXT_PUBLIC_SITE_URL"
echo "  API URL: $NEXT_PUBLIC_API_BASE_URL"
echo "  GA Measurement ID: $NEXT_PUBLIC_GA_MEASUREMENT_ID"
echo ""
read -p "Do you want to proceed with deployment? (y/n) " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    print_info "Deployment cancelled"
    exit 0
fi

# Deployment method selection
echo ""
print_info "Select deployment method:"
echo "  1) Vercel"
echo "  2) Docker"
echo "  3) Manual (build only)"
echo ""
read -p "Enter choice (1-3): " -n 1 -r DEPLOY_METHOD
echo ""

case $DEPLOY_METHOD in
    1)
        print_info "Deploying to Vercel..."
        if ! command_exists vercel; then
            print_error "Vercel CLI is not installed. Install it with: npm i -g vercel"
            exit 1
        fi
        
        print_info "Running Vercel deployment..."
        vercel --prod
        
        if [ $? -eq 0 ]; then
            print_success "Deployment to Vercel completed successfully!"
        else
            print_error "Vercel deployment failed"
            exit 1
        fi
        ;;
    
    2)
        print_info "Building Docker image..."
        if ! command_exists docker; then
            print_error "Docker is not installed"
            exit 1
        fi
        
        IMAGE_NAME="saas-landing"
        IMAGE_TAG="$(date +%Y%m%d-%H%M%S)"
        
        docker build \
            --build-arg NEXT_PUBLIC_API_BASE_URL="$NEXT_PUBLIC_API_BASE_URL" \
            --build-arg NEXT_PUBLIC_APP_URL="$NEXT_PUBLIC_APP_URL" \
            --build-arg NEXT_PUBLIC_PAYPAL_CLIENT_ID="$NEXT_PUBLIC_PAYPAL_CLIENT_ID" \
            -t "$IMAGE_NAME:$IMAGE_TAG" \
            -t "$IMAGE_NAME:latest" \
            .
        
        if [ $? -eq 0 ]; then
            print_success "Docker image built successfully: $IMAGE_NAME:$IMAGE_TAG"
            print_info "To run the container:"
            echo "  docker run -d -p 3003:3003 --name saas-landing \\"
            echo "    -e NEXT_PUBLIC_GA_MEASUREMENT_ID=\"$NEXT_PUBLIC_GA_MEASUREMENT_ID\" \\"
            echo "    -e NEXT_PUBLIC_GSC_VERIFICATION=\"$NEXT_PUBLIC_GSC_VERIFICATION\" \\"
            echo "    -e NEXT_PUBLIC_SITE_URL=\"$NEXT_PUBLIC_SITE_URL\" \\"
            echo "    -e NEXT_PUBLIC_SITE_NAME=\"$NEXT_PUBLIC_SITE_NAME\" \\"
            echo "    $IMAGE_NAME:$IMAGE_TAG"
        else
            print_error "Docker build failed"
            exit 1
        fi
        ;;
    
    3)
        print_success "Build completed. Manual deployment required."
        print_info "Next steps:"
        echo "  1. Upload .next directory to your server"
        echo "  2. Upload public directory to your server"
        echo "  3. Upload package.json and next.config.js"
        echo "  4. Run: pnpm install --prod"
        echo "  5. Run: pnpm start -p 3003"
        ;;
    
    *)
        print_error "Invalid choice"
        exit 1
        ;;
esac

# Post-deployment instructions
echo ""
print_success "Deployment process completed!"
echo ""
print_info "Post-Deployment Checklist:"
echo "  1. Verify site is accessible at: $NEXT_PUBLIC_SITE_URL"
echo "  2. Check Google Analytics real-time data"
echo "  3. Submit sitemap to Google Search Console: $NEXT_PUBLIC_SITE_URL/sitemap.xml"
echo "  4. Test critical user flows"
echo "  5. Monitor error logs for the first 24 hours"
echo ""
print_info "For detailed monitoring instructions, see:"
echo "  docs/PRODUCTION_DEPLOYMENT_GUIDE.md"
echo ""
