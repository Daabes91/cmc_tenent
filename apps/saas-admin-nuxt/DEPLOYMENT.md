# SAAS Manager Admin Panel - Deployment Guide

This guide covers the deployment of the SAAS Manager Admin Panel for production and development environments.

## Table of Contents

- [Prerequisites](#prerequisites)
- [Environment Configuration](#environment-configuration)
- [Local Development](#local-development)
- [Docker Deployment](#docker-deployment)
- [Production Deployment](#production-deployment)
- [Nginx Configuration](#nginx-configuration)
- [Monitoring and Maintenance](#monitoring-and-maintenance)
- [Troubleshooting](#troubleshooting)

## Prerequisites

### Required Software

- **Node.js**: v20.x or higher
- **pnpm**: v8.x or higher (for package management)
- **Docker**: v24.x or higher (for containerized deployment)
- **Docker Compose**: v2.x or higher
- **Nginx**: v1.24.x or higher (for reverse proxy in production)

### System Requirements

**Development:**
- CPU: 2 cores minimum
- RAM: 4GB minimum
- Disk: 2GB free space

**Production:**
- CPU: 4 cores recommended
- RAM: 8GB recommended
- Disk: 10GB free space
- Network: Stable internet connection with HTTPS support

## Environment Configuration

### Environment Variables

Create a `.env` file in the `apps/saas-admin-nuxt` directory based on `.env.example`:

```bash
# API Configuration
NUXT_PUBLIC_API_BASE=https://api.example.com
NUXT_PUBLIC_SAAS_API_BASE=https://api.example.com/saas

# Application Configuration
NUXT_PUBLIC_APP_NAME=SAAS Manager Panel
NUXT_PUBLIC_APP_URL=https://saas-admin.example.com

# Feature Flags
NUXT_PUBLIC_ENABLE_ANALYTICS=true
NUXT_PUBLIC_ENABLE_AUDIT_LOGS=true
```

### Configuration by Environment

#### Development
```bash
NUXT_PUBLIC_API_BASE=http://localhost:8080
NUXT_PUBLIC_SAAS_API_BASE=http://localhost:8080/saas
NUXT_PUBLIC_APP_URL=http://localhost:3002
```

#### Staging
```bash
NUXT_PUBLIC_API_BASE=https://api-staging.example.com
NUXT_PUBLIC_SAAS_API_BASE=https://api-staging.example.com/saas
NUXT_PUBLIC_APP_URL=https://saas-admin-staging.example.com
```

#### Production
```bash
NUXT_PUBLIC_API_BASE=https://api.example.com
NUXT_PUBLIC_SAAS_API_BASE=https://api.example.com/saas
NUXT_PUBLIC_APP_URL=https://saas-admin.example.com
```

## Local Development

### Setup

1. **Install dependencies:**
   ```bash
   cd apps/saas-admin-nuxt
   pnpm install
   ```

2. **Configure environment:**
   ```bash
   cp .env.example .env
   # Edit .env with your local configuration
   ```

3. **Start development server:**
   ```bash
   pnpm dev
   ```

   The application will be available at `http://localhost:3002`

### Development with Docker

Use Docker Compose for a complete development environment:

```bash
# From the saas-admin-nuxt directory
docker-compose -f docker-compose.dev.yml up -d

# View logs
docker-compose -f docker-compose.dev.yml logs -f saas-admin

# Stop services
docker-compose -f docker-compose.dev.yml down
```

## Docker Deployment

### Building the Docker Image

#### Build for Development
```bash
# From the project root
docker build \
  --build-arg NUXT_PUBLIC_API_BASE=http://localhost:8080 \
  --build-arg NUXT_PUBLIC_SAAS_API_BASE=http://localhost:8080/saas \
  --build-arg NUXT_PUBLIC_APP_NAME="SAAS Manager Panel" \
  --build-arg NUXT_PUBLIC_APP_URL=http://localhost:3002 \
  --build-arg NUXT_PUBLIC_ENABLE_ANALYTICS=true \
  --build-arg NUXT_PUBLIC_ENABLE_AUDIT_LOGS=true \
  -f apps/saas-admin-nuxt/Dockerfile \
  -t clinic-saas-admin:dev .
```

#### Build for Production
```bash
# From the project root
docker build \
  --build-arg NUXT_PUBLIC_API_BASE=https://api.example.com \
  --build-arg NUXT_PUBLIC_SAAS_API_BASE=https://api.example.com/saas \
  --build-arg NUXT_PUBLIC_APP_NAME="SAAS Manager Panel" \
  --build-arg NUXT_PUBLIC_APP_URL=https://saas-admin.example.com \
  --build-arg NUXT_PUBLIC_ENABLE_ANALYTICS=true \
  --build-arg NUXT_PUBLIC_ENABLE_AUDIT_LOGS=true \
  -f apps/saas-admin-nuxt/Dockerfile \
  -t clinic-saas-admin:latest .
```

### Running the Container

```bash
docker run -d \
  --name clinic-saas-admin \
  -p 3002:3002 \
  -e NUXT_PUBLIC_API_BASE=https://api.example.com \
  -e NUXT_PUBLIC_SAAS_API_BASE=https://api.example.com/saas \
  -e NUXT_PUBLIC_APP_URL=https://saas-admin.example.com \
  --restart unless-stopped \
  clinic-saas-admin:latest
```

### Docker Compose Integration

Add the SAAS Admin service to your main `docker-compose.yml`:

```yaml
services:
  saas-admin:
    build:
      context: .
      dockerfile: apps/saas-admin-nuxt/Dockerfile
      args:
        NUXT_PUBLIC_API_BASE: https://api.example.com
        NUXT_PUBLIC_SAAS_API_BASE: https://api.example.com/saas
        NUXT_PUBLIC_APP_NAME: SAAS Manager Panel
        NUXT_PUBLIC_APP_URL: https://saas-admin.example.com
        NUXT_PUBLIC_ENABLE_ANALYTICS: "true"
        NUXT_PUBLIC_ENABLE_AUDIT_LOGS: "true"
    container_name: clinic-saas-admin
    restart: unless-stopped
    environment:
      NUXT_PUBLIC_API_BASE: https://api.example.com
      NUXT_PUBLIC_SAAS_API_BASE: https://api.example.com/saas
      NUXT_PUBLIC_APP_URL: https://saas-admin.example.com
      NODE_ENV: production
    ports:
      - "3002:3002"
    networks:
      - clinic-net
    depends_on:
      api:
        condition: service_healthy
    healthcheck:
      test: ["CMD-SHELL", "wget --no-verbose --tries=1 --spider http://localhost:3002/saas-admin || exit 1"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 40s
```

## Production Deployment

### Deployment Checklist

- [ ] Environment variables configured correctly
- [ ] SSL certificates obtained and configured
- [ ] Nginx reverse proxy configured
- [ ] Firewall rules configured
- [ ] Database connection tested
- [ ] API endpoints accessible
- [ ] Backup strategy in place
- [ ] Monitoring configured
- [ ] Log aggregation configured

### Deployment Steps

1. **Prepare the server:**
   ```bash
   # Update system packages
   sudo apt update && sudo apt upgrade -y
   
   # Install Docker and Docker Compose
   curl -fsSL https://get.docker.com -o get-docker.sh
   sudo sh get-docker.sh
   sudo usermod -aG docker $USER
   
   # Install Nginx
   sudo apt install nginx -y
   ```

2. **Clone the repository:**
   ```bash
   git clone https://github.com/your-org/clinic-management.git
   cd clinic-management
   ```

3. **Configure environment:**
   ```bash
   cd apps/saas-admin-nuxt
   cp .env.example .env
   # Edit .env with production values
   ```

4. **Build and deploy:**
   ```bash
   # Build the Docker image
   docker build \
     --build-arg NUXT_PUBLIC_API_BASE=$NUXT_PUBLIC_API_BASE \
     --build-arg NUXT_PUBLIC_SAAS_API_BASE=$NUXT_PUBLIC_SAAS_API_BASE \
     --build-arg NUXT_PUBLIC_APP_NAME="$NUXT_PUBLIC_APP_NAME" \
     --build-arg NUXT_PUBLIC_APP_URL=$NUXT_PUBLIC_APP_URL \
     --build-arg NUXT_PUBLIC_ENABLE_ANALYTICS=true \
     --build-arg NUXT_PUBLIC_ENABLE_AUDIT_LOGS=true \
     -f apps/saas-admin-nuxt/Dockerfile \
     -t clinic-saas-admin:latest .
   
   # Run the container
   docker run -d \
     --name clinic-saas-admin \
     -p 3002:3002 \
     --env-file apps/saas-admin-nuxt/.env \
     --restart unless-stopped \
     clinic-saas-admin:latest
   ```

5. **Configure Nginx (see next section)**

6. **Verify deployment:**
   ```bash
   # Check container status
   docker ps | grep clinic-saas-admin
   
   # Check logs
   docker logs clinic-saas-admin
   
   # Test endpoint
   curl http://localhost:3002/saas-admin
   ```

### SSL Certificate Setup

Using Let's Encrypt with Certbot:

```bash
# Install Certbot
sudo apt install certbot python3-certbot-nginx -y

# Obtain certificate
sudo certbot --nginx -d saas-admin.example.com

# Auto-renewal is configured automatically
# Test renewal
sudo certbot renew --dry-run
```

## Nginx Configuration

### Basic Setup

1. **Copy the configuration:**
   ```bash
   sudo cp apps/saas-admin-nuxt/nginx.conf /etc/nginx/sites-available/saas-admin
   ```

2. **Edit the configuration:**
   ```bash
   sudo nano /etc/nginx/sites-available/saas-admin
   ```
   
   Update the following:
   - Replace `saas-admin.example.com` with your domain
   - Uncomment HTTPS server block for production
   - Update SSL certificate paths
   - Adjust rate limiting as needed

3. **Enable the site:**
   ```bash
   sudo ln -s /etc/nginx/sites-available/saas-admin /etc/nginx/sites-enabled/
   ```

4. **Test and reload:**
   ```bash
   sudo nginx -t
   sudo systemctl reload nginx
   ```

### Advanced Configuration

#### Rate Limiting

Adjust rate limits in `nginx.conf`:

```nginx
# More restrictive for login
limit_req_zone $binary_remote_addr zone=saas_admin_login:10m rate=3r/m;

# More permissive for API
limit_req_zone $binary_remote_addr zone=saas_admin_api:10m rate=200r/m;
```

#### Caching

Configure caching for static assets:

```nginx
location ~* \.(jpg|jpeg|png|gif|ico|css|js|svg|woff|woff2|ttf|eot)$ {
    proxy_pass http://saas_admin_backend;
    proxy_cache saas_admin_cache;
    proxy_cache_valid 200 30d;
    add_header Cache-Control "public, immutable";
    expires 30d;
}
```

#### IP Whitelisting (Optional)

Restrict access to specific IPs:

```nginx
location / {
    allow 203.0.113.0/24;  # Your office IP range
    allow 198.51.100.0/24; # VPN IP range
    deny all;
    
    proxy_pass http://saas_admin_backend;
    # ... rest of proxy configuration
}
```

## Monitoring and Maintenance

### Health Checks

The application exposes a health check endpoint:

```bash
# Check application health
curl http://localhost:3002/saas-admin

# Expected response: 200 OK
```

### Logging

#### View Application Logs

```bash
# Docker logs
docker logs -f clinic-saas-admin

# Last 100 lines
docker logs --tail 100 clinic-saas-admin

# Logs since 1 hour ago
docker logs --since 1h clinic-saas-admin
```

#### Nginx Logs

```bash
# Access logs
sudo tail -f /var/log/nginx/access.log

# Error logs
sudo tail -f /var/log/nginx/error.log
```

### Performance Monitoring

Monitor key metrics:

- **Response Time**: Should be < 500ms for most requests
- **Memory Usage**: Should stay below 512MB
- **CPU Usage**: Should stay below 50% under normal load
- **Error Rate**: Should be < 1%

```bash
# Monitor container resources
docker stats clinic-saas-admin

# Check memory usage
docker exec clinic-saas-admin node -e "console.log(process.memoryUsage())"
```

### Backup Strategy

1. **Application Configuration:**
   ```bash
   # Backup environment files
   tar -czf saas-admin-config-$(date +%Y%m%d).tar.gz \
     apps/saas-admin-nuxt/.env \
     apps/saas-admin-nuxt/nginx.conf
   ```

2. **Docker Images:**
   ```bash
   # Save Docker image
   docker save clinic-saas-admin:latest | gzip > saas-admin-image-$(date +%Y%m%d).tar.gz
   ```

### Updates and Rollbacks

#### Update Deployment

```bash
# Pull latest code
git pull origin main

# Rebuild image
docker build -f apps/saas-admin-nuxt/Dockerfile -t clinic-saas-admin:latest .

# Stop old container
docker stop clinic-saas-admin
docker rm clinic-saas-admin

# Start new container
docker run -d \
  --name clinic-saas-admin \
  -p 3002:3002 \
  --env-file apps/saas-admin-nuxt/.env \
  --restart unless-stopped \
  clinic-saas-admin:latest
```

#### Rollback

```bash
# Tag current version before update
docker tag clinic-saas-admin:latest clinic-saas-admin:backup

# If rollback needed
docker stop clinic-saas-admin
docker rm clinic-saas-admin
docker run -d \
  --name clinic-saas-admin \
  -p 3002:3002 \
  --env-file apps/saas-admin-nuxt/.env \
  --restart unless-stopped \
  clinic-saas-admin:backup
```

## Troubleshooting

### Common Issues

#### Container Won't Start

```bash
# Check logs
docker logs clinic-saas-admin

# Common causes:
# - Port 3002 already in use
# - Missing environment variables
# - API not accessible
```

#### 502 Bad Gateway

```bash
# Check if container is running
docker ps | grep clinic-saas-admin

# Check Nginx configuration
sudo nginx -t

# Check Nginx error logs
sudo tail -f /var/log/nginx/error.log
```

#### Slow Performance

```bash
# Check container resources
docker stats clinic-saas-admin

# Check API response times
curl -w "@curl-format.txt" -o /dev/null -s http://localhost:3002/saas-admin

# Increase container resources if needed
docker update --memory 1g --cpus 2 clinic-saas-admin
```

#### Authentication Issues

```bash
# Verify API connectivity
curl -v http://localhost:8080/saas/auth/login

# Check CORS headers
curl -H "Origin: http://localhost:3002" \
  -H "Access-Control-Request-Method: POST" \
  -H "Access-Control-Request-Headers: Content-Type" \
  -X OPTIONS \
  http://localhost:8080/saas/auth/login
```

### Debug Mode

Enable debug logging:

```bash
# Set environment variable
docker run -d \
  --name clinic-saas-admin \
  -p 3002:3002 \
  -e NODE_ENV=development \
  -e DEBUG=nuxt:* \
  --env-file apps/saas-admin-nuxt/.env \
  clinic-saas-admin:latest
```

### Getting Help

If you encounter issues:

1. Check the logs (application and Nginx)
2. Verify environment variables
3. Test API connectivity
4. Review the [README.md](./README.md) for additional information
5. Contact the development team with:
   - Error messages
   - Log excerpts
   - Steps to reproduce
   - Environment details

## Security Considerations

### Production Security Checklist

- [ ] HTTPS enabled with valid SSL certificate
- [ ] Strong authentication credentials
- [ ] Rate limiting configured
- [ ] Security headers configured in Nginx
- [ ] Firewall rules configured
- [ ] Regular security updates applied
- [ ] Logs monitored for suspicious activity
- [ ] Backup and disaster recovery plan in place
- [ ] Access restricted to authorized IPs (if applicable)
- [ ] Environment variables secured (not in version control)

### Security Best Practices

1. **Keep software updated:**
   ```bash
   # Update system packages
   sudo apt update && sudo apt upgrade -y
   
   # Update Docker images
   docker pull node:20-alpine
   docker pull nginx:latest
   ```

2. **Monitor logs regularly:**
   ```bash
   # Set up log rotation
   sudo nano /etc/logrotate.d/saas-admin
   ```

3. **Use strong passwords:**
   - Minimum 16 characters
   - Mix of uppercase, lowercase, numbers, symbols
   - Unique for each service

4. **Regular backups:**
   - Daily automated backups
   - Test restore procedures monthly
   - Store backups securely off-site

## Performance Optimization

### Production Optimizations

1. **Enable Nginx caching:**
   - Static assets cached for 30 days
   - API responses cached appropriately
   - Gzip compression enabled

2. **Container resource limits:**
   ```bash
   docker update --memory 1g --cpus 2 clinic-saas-admin
   ```

3. **Database connection pooling:**
   - Configured in API backend
   - Monitor connection usage

4. **CDN integration (optional):**
   - Serve static assets from CDN
   - Reduce server load
   - Improve global performance

## Conclusion

This deployment guide covers the essential steps for deploying the SAAS Manager Admin Panel. For additional information, refer to:

- [README.md](./README.md) - Application overview and features
- [docs/](./docs/) - Detailed documentation
- API documentation - Backend API reference

For support, contact the development team or open an issue in the project repository.
