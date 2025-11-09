#!/bin/bash

# SAAS Manager Admin Panel - Deployment Script
# This script automates the deployment process

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Configuration
APP_NAME="clinic-saas-admin"
IMAGE_NAME="clinic-saas-admin"
CONTAINER_PORT=3002
HOST_PORT=3002

# Functions
print_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

check_prerequisites() {
    print_info "Checking prerequisites..."
    
    # Check Docker
    if ! command -v docker &> /dev/null; then
        print_error "Docker is not installed. Please install Docker first."
        exit 1
    fi
    
    # Check if .env file exists
    if [ ! -f .env ]; then
        print_warning ".env file not found. Creating from .env.example..."
        if [ -f .env.example ]; then
            cp .env.example .env
            print_warning "Please edit .env file with your configuration before deploying."
            exit 1
        else
            print_error ".env.example not found. Cannot create .env file."
            exit 1
        fi
    fi
    
    print_info "Prerequisites check passed."
}

load_env() {
    print_info "Loading environment variables..."
    if [ -f .env ]; then
        export $(cat .env | grep -v '^#' | xargs)
    fi
}

build_image() {
    print_info "Building Docker image..."
    
    cd ../..
    
    docker build \
        --build-arg NUXT_PUBLIC_API_BASE="${NUXT_PUBLIC_API_BASE}" \
        --build-arg NUXT_PUBLIC_SAAS_API_BASE="${NUXT_PUBLIC_SAAS_API_BASE}" \
        --build-arg NUXT_PUBLIC_APP_NAME="${NUXT_PUBLIC_APP_NAME}" \
        --build-arg NUXT_PUBLIC_APP_URL="${NUXT_PUBLIC_APP_URL}" \
        --build-arg NUXT_PUBLIC_ENABLE_ANALYTICS="${NUXT_PUBLIC_ENABLE_ANALYTICS}" \
        --build-arg NUXT_PUBLIC_ENABLE_AUDIT_LOGS="${NUXT_PUBLIC_ENABLE_AUDIT_LOGS}" \
        -f apps/saas-admin-nuxt/Dockerfile \
        -t ${IMAGE_NAME}:latest \
        -t ${IMAGE_NAME}:$(date +%Y%m%d-%H%M%S) \
        .
    
    cd apps/saas-admin-nuxt
    
    print_info "Docker image built successfully."
}

stop_container() {
    print_info "Stopping existing container..."
    
    if docker ps -a | grep -q ${APP_NAME}; then
        docker stop ${APP_NAME} || true
        docker rm ${APP_NAME} || true
        print_info "Existing container stopped and removed."
    else
        print_info "No existing container found."
    fi
}

start_container() {
    print_info "Starting new container..."
    
    docker run -d \
        --name ${APP_NAME} \
        -p ${HOST_PORT}:${CONTAINER_PORT} \
        --env-file .env \
        --restart unless-stopped \
        ${IMAGE_NAME}:latest
    
    print_info "Container started successfully."
}

verify_deployment() {
    print_info "Verifying deployment..."
    
    # Wait for container to start
    sleep 5
    
    # Check if container is running
    if docker ps | grep -q ${APP_NAME}; then
        print_info "Container is running."
        
        # Check if application is responding
        if curl -f -s http://localhost:${HOST_PORT}/saas-admin > /dev/null; then
            print_info "Application is responding."
            print_info "Deployment successful! 🎉"
            print_info "Access the application at: http://localhost:${HOST_PORT}/saas-admin"
        else
            print_warning "Container is running but application is not responding yet."
            print_info "Check logs with: docker logs ${APP_NAME}"
        fi
    else
        print_error "Container is not running."
        print_info "Check logs with: docker logs ${APP_NAME}"
        exit 1
    fi
}

show_logs() {
    print_info "Showing container logs..."
    docker logs -f ${APP_NAME}
}

# Main deployment flow
main() {
    print_info "Starting deployment of SAAS Manager Admin Panel..."
    
    check_prerequisites
    load_env
    build_image
    stop_container
    start_container
    verify_deployment
    
    # Ask if user wants to see logs
    read -p "Do you want to view the logs? (y/n) " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        show_logs
    fi
}

# Handle script arguments
case "${1:-}" in
    build)
        check_prerequisites
        load_env
        build_image
        ;;
    start)
        check_prerequisites
        load_env
        start_container
        verify_deployment
        ;;
    stop)
        stop_container
        ;;
    restart)
        stop_container
        start_container
        verify_deployment
        ;;
    logs)
        show_logs
        ;;
    *)
        main
        ;;
esac
